# nyoom — Motorbike Trips Tracking App

A minimal, high-performance Android app for tracking motorbike rides in real-time with GPS, speed, and itinerary logging.

## Features

- **Riding Screen**: Start/pause/stop ride tracking with live stats (time, distance, speed, avg speed, max speed)
- **Diary Screen**: Historical view of all completed trips with summary statistics
- **GPS Tracking**: Continuous location updates every 5-10 seconds using Fused Location Provider
- **Trip Storage**: SQLite database (Room ORM) persisting trips and coordinates
- **Map Visualization**: Display completed ride routes as polylines on Google Maps
- **Minimal Design**: No code duplication, clean architecture, zero unnecessary comments

## Tech Stack

- **Language**: Kotlin 1.9.22
- **UI Framework**: Jetpack Compose (Material 3)
- **Navigation**: Jetpack Navigation Compose
- **Database**: Room ORM with SQLite
- **Location**: Google Play Services (Fused Location Provider)
- **Maps**: Google Maps API with maps-compose
- **Async**: Coroutines + Flow for reactive data
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## Project Structure

```
app/src/main/
├── kotlin/com/nyoom/
│   ├── MainActivity.kt                    # Entry point, nav setup
│   ├── NyoomApp.kt                        # Application class, DI setup
│   ├── data/
│   │   ├── model/
│   │   │   ├── Trip.kt                    # Room entity: trips table
│   │   │   └── Coordinate.kt              # Room entity: coordinates table
│   │   ├── dao/
│   │   │   ├── TripDao.kt                 # Trip CRUD operations
│   │   │   └── CoordinateDao.kt           # Coordinate batch operations
│   │   ├── db/
│   │   │   └── TripDatabase.kt            # Room database singleton
│   │   └── repository/
│   │       └── TripRepository.kt          # Repository pattern for DB abstraction
│   ├── service/
│   │   └── LocationTracker.kt             # Wraps FusedLocationProviderClient
│   ├── ui/
│   │   ├── CompositionLocals.kt           # Dependency injection via Compose
│   │   ├── ViewModelFactory.kt            # ViewModel factory for DI
│   │   ├── riding/
│   │   │   ├── RidingScreen.kt            # Tracking UI with start/pause/stop
│   │   │   └── RidingViewModel.kt         # Ride state, location collection, DB saves
│   │   ├── diary/
│   │   │   ├── DiaryScreen.kt             # Trip list with delete action
│   │   │   └── DiaryViewModel.kt          # Load and manage trips
│   │   └── map/
│   │       ├── MapScreen.kt               # Google Maps polyline display
│   │       └── MapViewModel.kt            # Load coordinates for selected trip
│   └── util/
│       ├── GeoUtils.kt                    # Haversine distance, bearing calculations
│       └── PermissionUtils.kt             # Permission helpers
└── res/
    └── values/
        ├── strings.xml                    # App name, screen labels
        └── themes.xml                     # Material 3 theme
```

## Build & Run

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34+ with build-tools
- Gradle 8.2+
- Google Play Services enabled on test device

### Build Steps

1. **Clone and open in Android Studio**

   ```bash
   git clone <repo>
   cd nyoom
   open -a "Android Studio" .
   ```

2. **Sync Gradle** (Android Studio will prompt)

3. **Configure Google Maps API key**
   - Get API key from [Google Cloud Console](https://console.cloud.google.com)
   - Add to `local.properties`:

     ```
     MAPS_API_KEY=<your-api-key>
     ```

   - Add to `AndroidManifest.xml` inside `<application>`:

     ```xml
     <meta-data
         android:name="com.google.android.geo.API_KEY"
         android:value="@string/google_maps_key" />
     ```

4. **Build APK**

   ```bash
   ./gradlew assembleDebug
   ```

5. **Install and run**

   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   adb shell am start -n com.nyoom/.MainActivity
   ```

## Usage

### Riding Screen

1. **Start**: Tap "Start" → grant location permission → GPS tracking begins
2. **Monitor**: Watch real-time distance, speed, avg speed, max speed
3. **Pause**: Tap "Pause" to pause tracking (battery-friendly)
4. **Resume**: Tap "Resume" to continue
5. **Stop**: Tap "Stop" → trip saved to database with all coordinates

### Diary Screen

1. **View trips**: Swipe to Diary tab to see all past rides
2. **Trip details**: Each card shows date, distance, duration, avg/max speed
3. **Delete**: Tap "Delete" to remove a trip and its coordinates

### Map View (Future Enhancement)

- Tap a trip card → shows polyline route on Google Maps
- Tap-to-expand shows start/end markers with fit-to-bounds zoom

## Database Schema

### Trips Table

```sql
CREATE TABLE trips (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    startTime LONG NOT NULL,
    endTime LONG,
    distanceKm REAL DEFAULT 0,
    avgSpeed REAL,
    maxSpeed REAL
);
```

### Coordinates Table

```sql
CREATE TABLE coordinates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tripId INTEGER NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    timestamp LONG NOT NULL,
    accuracy REAL,
    FOREIGN KEY(tripId) REFERENCES trips(id) ON DELETE CASCADE,
    INDEX(tripId)
);
```

## Key Design Decisions

1. **No Background Tracking (MVP)**: App pauses GPS when backgrounded to simplify permissions and reduce battery drain
2. **Fused Location Provider**: Automatically selects best source (GPS/WiFi/cellular); better than raw LocationManager
3. **5-10s GPS Intervals**: Balances accuracy for motorbike speeds (100+ km/h) vs. battery drain
4. **Room ORM**: Type-safe, compile-time checked queries; avoids SQL injection
5. **CompositionLocals for DI**: Cleaner than passing dependencies through Compose parameters
6. **Separate Trip/Coordinate Tables**: Normalizes schema; long rides don't duplicate trip metadata
7. **No Real-Time Map (MVP)**: Polyline display only post-ride; drawing polylines with map animation requires performance optimization deferred to v2

## Testing Checklist

- [ ] **Permissions**: Fresh install on Android 12+ device; grant location permission on first start
- [ ] **Ride Tracking**: Start → wait 2 min → pause → verify distance increments → resume → stop → trip appears in Diary
- [ ] **GPS Data**: Use Android Emulator with mock GPS or GPX playback to simulate route
- [ ] **Database**: Query `adb shell sqlite3 /data/data/com.nyoom/databases/trip_database` to verify trip/coordinate inserts
- [ ] **Map Display**: Tap a trip → polyline renders correctly on Google Maps
- [ ] **Stats Calculation**: Verify avg speed = distance / time, max speed from location events
- [ ] **Delete**: Delete a trip → coordinates cascade-deleted, Diary list updates

## Future Enhancements

1. **Gyro/Accelerometer**: Tilt angle tracking for lean angle analysis
2. **Real-Time Map**: Display polyline updating as ride progresses
3. **Background Tracking**: Foreground service for continuous tracking while backgrounded
4. **Data Export**: CSV/GPX export for trip sharing
5. **Cloud Sync**: Firebase or custom backend for backup/sync across devices
6. **Offline Maps**: OSM offline support via OsmDroid or Mapbox
7. **Replay Mode**: Animate past rides on map with speed/time overlay
8. **Weather Integration**: Overlay weather conditions during ride

## Troubleshooting

**"Permission denied for location"**

- Grant `ACCESS_FINE_LOCATION` at runtime on Android 6+
- Verify in `AndroidManifest.xml` and `app/build.gradle.kts`

**"No GPS signal indoors"**

- Use Android Emulator's mock location feature or GPX playback
- Real devices need clear sky view for GPS lock

**"Map API key error"**

- Verify key in `local.properties` and `AndroidManifest.xml`
- Check key restrictions in Google Cloud Console (restrict to Android, package `com.nyoom`)

**"Database migration error"**

- Increment `@Database(version = X)` when schema changes
- Write migration using `addMigrations(MIGRATION_X_Y)`

## License

TBD
