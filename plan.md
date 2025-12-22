# Run Tracker App - Implementation Plan

## Overview
A GPS-based activity tracking app for running/walking/cycling with real-time map display, history, statistics, and Firebase backup.

## Tech Stack
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Database**: Room
- **Maps**: Google Maps Compose
- **Location**: FusedLocationProviderClient
- **Charts**: Vico
- **Auth/Sync**: Firebase Auth (Google Sign-In) + Firestore
- **Sensors**: Step Counter

---

## Package Structure

```
com.example.mobile_final/
├── data/
│   ├── local/           # Room DB, DAOs, Entities
│   ├── repository/      # Repository implementations
│   └── firebase/        # Firebase sync logic
├── domain/
│   ├── model/           # Domain models
│   └── repository/      # Repository interfaces
├── service/             # TrackingService, SensorManager
├── ui/
│   ├── navigation/      # NavHost, Routes
│   ├── screen/
│   │   ├── home/        # Home/Dashboard
│   │   ├── tracking/    # Active tracking screen
│   │   ├── history/     # Activity list
│   │   ├── detail/      # Activity detail + route
│   │   ├── stats/       # Statistics charts
│   │   └── settings/    # User settings
│   └── component/       # Shared composables
├── di/                  # Hilt modules
└── util/                # Extensions, formatters
```

---

## Database Schema

### Entity: Activity
```kotlin
@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,              // "running", "walking", "cycling"
    val startTime: Long,
    val endTime: Long?,
    val distanceMeters: Double,
    val durationSeconds: Long,
    val caloriesBurned: Int,
    val avgPaceSecondsPerKm: Int,
    val stepCount: Int,
    val isSynced: Boolean = false
)
```

### Entity: LocationPoint
```kotlin
@Entity(tableName = "location_points",
    foreignKeys = [ForeignKey(entity = ActivityEntity::class,
        parentColumns = ["id"], childColumns = ["activityId"],
        onDelete = ForeignKey.CASCADE)])
data class LocationPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val timestamp: Long,
    val speedMps: Float?
)
```

### Entity: UserSettings
```kotlin
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val preferredActivityType: String = "running",
    val useMetricUnits: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val weight: Float = 70f
)
```

---

## Core Components

### TrackingService (Foreground Service)
- Runs as foreground service with persistent notification
- Uses FusedLocationProviderClient (high accuracy, 3-5s intervals)
- Registers step counter sensor
- Exposes StateFlow for real-time updates
- Calculates distance, pace, calories in real-time

### Repositories
| Repository | Responsibility |
|------------|----------------|
| ActivityRepository | CRUD activities + location points |
| SettingsRepository | User preferences |
| SyncRepository | Firebase backup/restore |

---

## Screen Breakdown

### 1. Home Screen
- Quick stats overview (today's activity, weekly summary)
- Large "Start" button
- Navigation to History, Stats, Settings

### 2. Tracking Screen
- Google Map with live route polyline
- Real-time stats: duration, distance, pace, steps
- Start/Pause/Stop controls
- Activity type selector

### 3. History Screen
- LazyColumn with activity cards
- Filter by activity type
- Swipe to delete
- Pull to refresh (sync)

### 4. Detail Screen
- Static map with full route
- Complete stats grid
- Share functionality

### 5. Statistics Screen
- Vico bar chart: weekly distance
- Vico line chart: monthly trends
- Summary cards (total distance, time, activities)
- Week/Month toggle

### 6. Settings Screen
- Activity type preference
- Unit system (metric/imperial)
- Notifications toggle
- Google Sign-In for sync
- Manual backup/restore buttons

---

## Dependencies to Add

### libs.versions.toml
```toml
[versions]
room = "2.6.1"
hilt = "2.51.1"
navigation = "2.7.7"
mapsCompose = "4.3.3"
playServicesLocation = "21.2.0"
playServicesAuth = "21.0.0"
firebaseBom = "32.8.0"
vico = "1.14.0"

[libraries]
# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Maps & Location
maps-compose = { group = "com.google.maps.android", name = "maps-compose", version.ref = "mapsCompose" }
play-services-location = { group = "com.google.android.gms", name = "play-services-location", version.ref = "playServicesLocation" }
play-services-auth = { group = "com.google.android.gms", name = "play-services-auth", version.ref = "playServicesAuth" }

# Firebase
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-firestore = { group = "com.google.firebase", name = "firebase-firestore-ktx" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth-ktx" }

# Charts
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }

# Lifecycle
lifecycle-service = { group = "androidx.lifecycle", name = "lifecycle-service", version = "2.7.0" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version = "2.7.0" }

[plugins]
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "2.0.21-1.0.25" }
google-services = { id = "com.google.gms.google-services", version = "4.4.1" }
```

---

## AndroidManifest Permissions
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
<uses-permission android:name="android.permission.INTERNET" />
```

---

## Implementation Phases

### Phase 1: Foundation
1. Update `libs.versions.toml` with all dependencies
2. Update `build.gradle.kts` (app) - add plugins (hilt, ksp, google-services)
3. Create Hilt Application class
4. Set up Room database with entities and DAOs
5. Create repository interfaces and implementations
6. Set up Navigation with placeholder screens

**Files to create/modify:**
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `app/src/main/java/.../RunTrackerApp.kt`
- `app/src/main/java/.../data/local/RunTrackerDatabase.kt`
- `app/src/main/java/.../data/local/dao/*.kt`
- `app/src/main/java/.../data/local/entity/*.kt`
- `app/src/main/java/.../di/DatabaseModule.kt`

### Phase 2: GPS Tracking
1. Implement TrackingService (foreground service)
2. Create notification channel and notification builder
3. Implement FusedLocationProviderClient wrapper
4. Build Tracking screen with Google Map
5. Add permission request flow
6. Connect service to UI via bound service + StateFlow

**Files to create:**
- `app/src/main/java/.../service/TrackingService.kt`
- `app/src/main/java/.../service/LocationManager.kt`
- `app/src/main/java/.../ui/screen/tracking/TrackingScreen.kt`
- `app/src/main/java/.../ui/screen/tracking/TrackingViewModel.kt`

### Phase 3: History & Detail
1. Implement History screen with LazyColumn
2. Create activity card component
3. Build Detail screen with static map
4. Add route polyline rendering
5. Implement stats calculation utilities

**Files to create:**
- `app/src/main/java/.../ui/screen/history/HistoryScreen.kt`
- `app/src/main/java/.../ui/screen/history/HistoryViewModel.kt`
- `app/src/main/java/.../ui/screen/detail/DetailScreen.kt`
- `app/src/main/java/.../ui/screen/detail/DetailViewModel.kt`
- `app/src/main/java/.../ui/component/ActivityCard.kt`

### Phase 4: Statistics
1. Add Vico chart dependencies
2. Implement stats aggregation queries in DAO
3. Build Statistics screen with charts
4. Add week/month toggle logic

**Files to create:**
- `app/src/main/java/.../ui/screen/stats/StatsScreen.kt`
- `app/src/main/java/.../ui/screen/stats/StatsViewModel.kt`

### Phase 5: Sensors
1. Integrate step counter sensor in TrackingService
2. Implement calorie calculation
3. Add activity type auto-detection (optional)

**Files to modify:**
- `app/src/main/java/.../service/TrackingService.kt`

### Phase 6: Firebase Sync
1. Set up Firebase project in console
2. Add `google-services.json`
3. Implement Google Sign-In flow
4. Create Firestore sync logic
5. Add sync status indicators

**Files to create:**
- `app/google-services.json`
- `app/src/main/java/.../data/firebase/FirebaseSyncManager.kt`
- `app/src/main/java/.../ui/screen/settings/SettingsScreen.kt`
- `app/src/main/java/.../ui/screen/settings/SettingsViewModel.kt`

### Phase 7: Polish
1. Add loading/error states
2. Implement notifications (weekly summary)
3. Add onboarding flow
4. Test and optimize battery usage

---

## Critical Files Summary

| File | Purpose |
|------|---------|
| `app/build.gradle.kts` | Add Hilt, KSP, Firebase plugins + dependencies |
| `gradle/libs.versions.toml` | Define all library versions |
| `AndroidManifest.xml` | Permissions + TrackingService declaration |
| `RunTrackerApp.kt` | @HiltAndroidApp entry point |
| `RunTrackerDatabase.kt` | Room database definition |
| `TrackingService.kt` | Core GPS tracking foreground service |
| `MainActivity.kt` | NavHost setup |

---

## Topics Coverage Mapping

| Course Topic | Implementation |
|--------------|----------------|
| Kotlin basics, Classes, Functions | Entire app |
| Layouts, ConstraintLayout | Compose equivalent layouts |
| Data Binding | Compose state management |
| RecyclerView | LazyColumn in History |
| Navigation, Fragments | Navigation Compose |
| Activity/Fragment Lifecycle | ViewModel + Service lifecycle |
| Room/SQLite | Activity & LocationPoint storage |
| Retrofit + Coroutines | (Skipped weather - can add later) |
| Firebase | Firestore sync + Auth |
| Google Maps API | Route display in Tracking/Detail |
| GPS/Location | FusedLocationProviderClient |
| Background Service | TrackingService (Foreground) |
| Coroutines vs Service | Coroutines in repos, Service for GPS |
| Sensors | Step counter |
| Notifications | Tracking notification + weekly summary |
| Animation | Route drawing, button animations |
| Jetpack Compose | Statistics screen + all UI |
| Performance | Battery optimization in location updates |
