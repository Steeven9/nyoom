# nyoom

![Vibe-coded](https://img.shields.io/badge/vibe--coded-yes-ff69b4)

A minimal, high-performance Android app for tracking motorbike rides in real-time with GPS, speed, and itinerary logging.

## Features

- **Riding Screen**: Start/pause/stop ride tracking with live stats (time, distance, speed, avg speed, max speed)
- **Diary Screen**: Historical view of all completed trips with summary statistics
- **GPS Tracking**: Continuous location updates every 5-10 seconds using Fused Location Provider
- **Trip Storage**: SQLite database (Room ORM) persisting trips and coordinates
- **Map Visualization**: Display completed ride routes as polylines on OpenStreetMap (no API key required)
- **Minimal Design**: No code duplication, clean architecture, zero unnecessary comments

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Navigation**: Jetpack Navigation Compose
- **Database**: Room ORM with SQLite
- **Location**: Google Play Services (Fused Location Provider)
- **Maps**: OpenStreetMap (Osmdroid) - no API key required
- **Async**: Coroutines + Flow for reactive data
- **Target SDK**: 37

## Build & Run

### Prerequisites

- Android Studio
- Android SDK 34+ with build-tools
- Gradle 8.2+

### Build Steps

1. **Clone and open in Android Studio**

   ```bash
   git clone <repo>
   cd nyoom
   open -a "Android Studio" .
   ```

2. **Sync Gradle** (Android Studio will prompt)

3. **Build APK**

   ```bash
   ./gradlew assembleDebug
   ```

4. **Install and run**

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

### Map View

- Tap a trip card → shows polyline route on OpenStreetMap
- Map is centered on the trip route and allows interactive panning/zooming

## Key Design Decisions

1. **No Background Tracking (MVP)**: App pauses GPS when backgrounded to simplify permissions and reduce battery drain
2. **Fused Location Provider**: Automatically selects best source (GPS/WiFi/cellular); better than raw LocationManager
3. **5-10s GPS Intervals**: Balances accuracy for motorbike speeds (100+ km/h) vs. battery drain
4. **Room ORM**: Type-safe, compile-time checked queries; avoids SQL injection
5. **CompositionLocals for DI**: Cleaner than passing dependencies through Compose parameters
6. **Separate Trip/Coordinate Tables**: Normalizes schema; long rides don't duplicate trip metadata
7. **No Real-Time Map (MVP)**: Polyline display only post-ride; drawing polylines with map animation requires performance optimization deferred to v2

## Troubleshooting

**"Permission denied for location"**

- Grant `ACCESS_FINE_LOCATION` at runtime on Android 6+
- Verify in `AndroidManifest.xml` and `app/build.gradle.kts`

**"No GPS signal indoors"**

- Use Android Emulator's mock location feature or GPX playback
- Real devices need clear sky view for GPS lock

**"Database migration error"**

- Increment `@Database(version = X)` when schema changes
- Write migration using `addMigrations(MIGRATION_X_Y)`

**Database**

- Query `adb shell sqlite3 /data/data/com.nyoom/databases/trip_database` to verify trip/coordinate inserts
