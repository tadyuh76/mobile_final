# LegIt - Fitness Activity Tracking App

A comprehensive Android fitness tracking application built with Kotlin, Jetpack Compose, and Firebase. Track your runs, walks, and cycling activities with real-time GPS tracking, weather data, and cloud sync.

## Features

### Activity Tracking
- **Real-time GPS tracking** with Mapbox integration
- **Multiple activity types**: Running, Walking, Cycling
- **Live statistics**: Distance, duration, pace, calories burned
- **Route visualization** with polyline overlay on maps
- **Step counting** using device sensors
- **Weather data capture** at activity start (temperature, humidity, wind speed)

### Cloud Sync & Backup
- **Google Sign-In** authentication
- **Automatic cloud backup** - activities sync to Firebase when signed in
- **Cross-device sync** - access your activities on any device
- **Offline-first architecture** - works without internet, syncs when available
- **Account switching** - data isolated per user account

### Social Features
- **Social feed** - view public activities from all users
- **Share activities** - make individual activities public
- **Privacy controls** - activities are private by default

### Statistics & History
- **Activity history** with detailed view
- **Weekly/Monthly charts** showing distance and trends
- **Statistics dashboard** with total distance, duration, calories

### UI/UX
- **Material 3 design** with dynamic theming
- **Dark/Light mode** support
- **Bottom navigation** (Home, Social, Statistics)
- **Pull-to-refresh** on feeds
- **Persistent tracking notification** during activities

## Architecture

```
app/
├── data/
│   ├── local/          # Room database (DAOs, entities)
│   ├── remote/         # API services (Weather)
│   └── repository/     # Repository implementations
├── di/                 # Hilt dependency injection modules
├── domain/
│   ├── model/          # Domain models
│   └── repository/     # Repository interfaces
├── service/            # Foreground tracking service
├── ui/
│   ├── component/      # Reusable Compose components
│   ├── navigation/     # Navigation graph
│   ├── screen/         # Screen composables & ViewModels
│   └── theme/          # Material 3 theming
└── util/               # Utility classes
```

### Key Technologies
- **Kotlin** - Primary language
- **Jetpack Compose** - UI framework
- **Hilt** - Dependency injection
- **Room** - Local database
- **Firebase Auth** - Google Sign-In
- **Firebase Firestore** - Cloud database
- **Mapbox SDK** - Maps and location
- **Vico** - Charts library
- **Coroutines & Flow** - Async operations

## Data Sync Flow

### Creating Activities
```
Complete activity → Save to Room DB → Background sync to Firestore → Mark as synced
```

### Sign-In Flow
```
Google Sign-In → Clear local DB → Restore user's activities from Firestore
```

### Account Switching
- Local database cleared on sign-in to prevent data mixing
- Each user's data is completely isolated
- All data backed up to cloud before clearing

## Firebase Structure

```
users/
└── {userId}/
    └── activities/
        └── {startTime}/
            ├── type, distance, duration, calories
            ├── weather data
            └── locationPoints[]

public_activities/
└── {docId}/
    ├── userId, userDisplayName
    ├── activity data
    └── locationPoints[]
```

## Setup

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17+
- Google account for Firebase

### Configuration

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd mobile_final
   ```

2. **Firebase Setup**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Enable Authentication (Google Sign-In)
   - Enable Cloud Firestore
   - Download `google-services.json` and place in `app/`

3. **Mapbox Setup**
   - Get API token from [Mapbox](https://www.mapbox.com)
   - Add to `local.properties`:
     ```
     MAPBOX_DOWNLOADS_TOKEN=your_token_here
     ```

4. **Build & Run**
   ```bash
   ./gradlew assembleDebug
   ```

### Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // User's private data
    match /users/{userId}/activities/{activityId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    // Public activities (read by anyone, write by owner)
    match /public_activities/{docId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

## Permissions

The app requires the following permissions:
- `ACCESS_FINE_LOCATION` - GPS tracking
- `ACCESS_COARSE_LOCATION` - Location services
- `FOREGROUND_SERVICE` - Background tracking
- `ACTIVITY_RECOGNITION` - Step counting
- `INTERNET` - Cloud sync
- `POST_NOTIFICATIONS` - Tracking notifications

## License

This project is for educational purposes.

## Acknowledgments

- [Mapbox](https://www.mapbox.com) for maps SDK
- [Firebase](https://firebase.google.com) for backend services
- [Vico](https://github.com/patrykandpatrick/vico) for charts
- [Open-Meteo](https://open-meteo.com) for weather API
