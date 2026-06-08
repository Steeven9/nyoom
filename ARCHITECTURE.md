# nyoom Architecture Guide

## Overview

**nyoom** follows a clean, layered architecture with clear separation of concerns:

```
UI Layer (Compose)
    ↓
ViewModel Layer (State Management)
    ↓
Repository Layer (Data Abstraction)
    ↓
Service Layer (Location Tracking)
    ↓
Data Layer (Room ORM + SQLite)
```

## Layer Breakdown

### 1. Data Layer (`data/`)

Manages all data persistence and retrieval.

#### Models (`data/model/`)

- **Trip.kt**: Represents a completed ride (id, startTime, endTime, distance, speeds)
- **Coordinate.kt**: Represents a GPS point (latitude, longitude, timestamp, accuracy)

#### DAOs (`data/dao/`)

- **TripDao.kt**: CRUD + queries for trips table
  - `insert()`: Add new trip
  - `update()`: Update trip with final stats
  - `getAllTrips()`: Flow of all trips (reactive)
  - `getTripById()`: Fetch specific trip

- **CoordinateDao.kt**: Batch insertion + queries for coordinates
  - `insert()`: Add single coordinate during ride
  - `insertAll()`: Batch insert after tracking
  - `getByTripId()`: Fetch route polyline
  - `observeByTripId()`: Flow of coordinates (reactive)

#### Database (`data/db/`)

- **TripDatabase.kt**: Room database singleton with table definitions

#### Repository (`data/repository/`)

- **TripRepository.kt**: Facade abstracting DAO operations
  - Centralizes all DB access logic
  - Used by ViewModels to decouple from Room implementation
  - Enables easy testing via mocking

### 2. Service Layer (`service/`)

Handles device sensors and external APIs.

#### LocationTracker (`service/LocationTracker.kt`)

- Wraps `FusedLocationProviderClient` (Google Play Services)
- Manages GPS lifecycle (start/stop/pause)
- Exposes StateFlows:
  - `locations`: Raw location objects
  - `totalDistance`: Accumulated distance (km)
  - `currentSpeed`: Real-time speed (m/s → km/h in ViewModel)
- **Key logic**: Haversine distance calculation between consecutive points

### 3. ViewModel Layer (`ui/*/`)

Manages UI state and business logic using coroutines.

#### RidingViewModel (`ui/riding/`)

- State: `RidingUiState` (isRunning, isPaused, stats)
- Lifecycle: start() → pause/resume loop → stop()
- On each location update:
  - Saves coordinate to DB
  - Updates running distance/speed
  - Calculates avg speed = distance / elapsed time
  - Tracks max speed from location events
- On stop():
  - Persists Trip with final stats
  - Clears LocationTracker
  - Resets UI state

#### DiaryViewModel (`ui/diary/`)

- Loads all trips via `repository.getAllTrips()` (Flow)
- Provides delete action for trip removal
- No state machine; reactive data from repository

#### MapViewModel (`ui/map/`)

- Loads coordinates for selected trip via `observeCoordinatesByTripId(tripId)`
- Hands off to Compose Google Maps for rendering

### 4. UI Layer (`ui/`)

Jetpack Compose screens with Material 3.

#### Navigation

- `MainActivity.kt`: Sets up Scaffold + NavigationBar + NavHost
- Two routes: "riding" → RidingScreen, "diary" → DiaryScreen
- BottomNavigationBar with two tabs

#### Composition Locals (`CompositionLocals.kt`)

- `LocalTripRepository`: Provides repository to all composables
- `LocalLocationTracker`: Provides location service to all composables
- Set in MainActivity via `CompositionLocalProvider`

#### Screens

- **RidingScreen.kt**:
  - Large timer display (HH:MM:SS)
  - Stats cards: distance, current speed, avg speed, max speed
  - Start button → permission request + GPS start
  - Pause/Resume buttons (only when running)
  - Stop button saves trip and returns to idle state

- **DiaryScreen.kt**:
  - LazyColumn of trips ordered by date DESC
  - Each card shows: date, distance, duration, avg/max speed
  - Delete button with cascade deletion

- **MapScreen.kt** (Future):
  - Google Maps with polyline of coordinates
  - Auto-zoom to fit route bounds
  - Start/end markers

### 5. Utilities (`util/`)

- **GeoUtils.kt**: Haversine distance, bearing calculations
- **PermissionUtils.kt**: Permission checking helpers

## Data Flow Example: Starting a Ride

1. User taps "Start" in RidingScreen
2. Permission check (runtime, Android 6+)
3. RidingViewModel.start() called
4. Creates Trip in DB, gets tripId
5. LocationTracker.startTracking() begins GPS updates
6. FusedLocationProviderClient emits Location every ~7 seconds
7. RidingViewModel.locationCallback receives Location
8. Coordinate inserted into DB for this trip
9. Distance calculated (Haversine between last + current)
10. StateFlow updates trigger UI re-compose
11. User sees distance/speed/time increment in real-time

## Data Flow Example: Stopping a Ride

1. User taps "Stop" in RidingScreen
2. LocationTracker.stopTracking() called
3. FusedLocationProviderClient stops emitting
4. RidingViewModel compiles final stats from collected data
5. Updates Trip record in DB (endTime, distance, avgSpeed, maxSpeed)
6. UI resets to initial state
7. Trip now appears in DiaryScreen (sorted by date DESC)

## Dependency Injection Strategy

**Manual DI via CompositionLocals**:

- NyoomApp (Application class) creates singletons: LocationTracker, Repository
- MainActivity wraps entire Scaffold in CompositionLocalProvider
- Screens access via `LocalTripRepository.current`, `LocalLocationTracker.current`
- ViewModels instantiated with Factory pattern inside each screen's viewModel() call

**Advantages**:

- No external DI framework (Hilt)
- Lightweight, easy to understand
- Full control over lifecycle
- CompositionLocals are Compose-native pattern

**Tradeoff**: Manual factory creation in each screen (minor boilerplate)

## Thread Model

- **Main Thread**: UI recomposition, navigation, user input
- **IO Thread**: Room database queries (via `suspend` functions)
- **Dispatcher.Default**: Location calculation (Haversine), speed averaging
- **Dispatcher.Unconfined**: LocationCallback (events flow from Play Services)

All ViewModel logic uses `viewModelScope.launch { }` for automatic cancellation on screen destruction.

## Testing Strategy

### Unit Tests (ViewModel Logic)

- Mock LocationTracker, Repository
- Call start/pause/resume/stop
- Verify state transitions and DB calls

### Integration Tests

- Test Room DAOs against real DB
- Insert Trip + Coordinates
- Query and verify relationships

### UI Tests (Espresso)

- Grant permission
- Tap Start → verify stats update
- Tap Stop → verify Trip appears in Diary

### Manual Testing (Device)

- Real GPS vs. mock GPS
- Permission prompts on Android 12+
- Background pause/resume
- Battery monitoring during long rides

## Performance Notes

- **GPS Updates**: 5-10s intervals (configurable in LocationRequest)
- **Database**: Coordinates indexed on tripId for fast lookups
- **UI Recomposition**: Only affected states recompose (Compose's smart diffing)
- **Memory**: Fixed-size buffers for location history; no unbounded accumulation
- **Battery**: No background service; app pauses tracking when backgrounded (MVP)

## Known Limitations (MVP)

1. **No Background Tracking**: GPS stops when app backgrounded
2. **No Gyro Integration**: Tilt angle not collected (deferred to v2)
3. **No Real-Time Map**: Polyline only displays after ride completion
4. **No Cloud Sync**: Local SQLite only; trips not backed up
5. **No Offline Maps**: Requires network for Google Maps tiles
6. **No Data Export**: CSV/GPX export not yet implemented

## Extending the App

### Add Gyro Tracking

1. Extend LocationTracker to register SensorManager listener
2. Add `tiltAngle` field to Coordinate entity
3. Store angle alongside lat/lon in DB
4. Display in RidingScreen stats card

### Real-Time Map During Ride

1. Reuse MapScreen Composable in RidingScreen
2. Draw polyline incrementally as locations arrive
3. Animate camera to follow current position

### Background Tracking

1. Create LocationForegroundService
2. Request SCHEDULE_EXACT_ALARM + POST_NOTIFICATIONS permissions
3. Start service in RidingViewModel.start()
4. Stop service in RidingViewModel.stop()

### Cloud Sync (Firebase)

1. Add firebase-firestore dependency
2. Create CloudRepository wrapper around TripRepository
3. On trip completion, push to Firestore
4. Sync on app startup (if network available)

---

**Next Steps to Build**: Open in Android Studio → Configure Maps API key → Sync Gradle → Run on device/emulator
