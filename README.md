# CrickBot 🏏

CrickBot is a conversational Android application that acts as your personal cricket assistant. It provides real-time information on live matches, recent results, and upcoming schedules, along with detailed match scorecards.

## 🚀 Features

- **Real-time Scores**: Get live updates for ongoing matches across the globe.
- **Conversational Search**: Ask about teams (e.g., "CSK", "India") or specific matchups (e.g., "India vs Pakistan").
- **Detailed Scorecards**: View comprehensive match details including:
  - Batting and Bowling statistics.
  - Fall of Wickets (FOW) chips for quick visualization.
  - Key partnerships.
- **Multi-Day Match Support**: Properly displays scores for Test matches (1st and 2nd innings).
- **Match discovery**: Easily find upcoming games and recent results.

## 🛠 Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for a modern, reactive user interface.
- **Language**: Kotlin.
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & OkHttp for API communication.
- **JSON Parsing**: GSON with custom `@SerializedName` mappings for robust data handling.
- **Architecture**: MVVM (Model-View-ViewModel) with State-driven UI.
- **Asynchronous Flow**: Kotlin Coroutines and Flow for efficient background tasks.

## 🔌 API Integration

The app uses the **Cricbuzz API** via RapidAPI. 
Endpoints integrated:
- `matches/v1/live`
- `matches/v1/recent`
- `matches/v1/upcoming`
- `mcenter/v1/{matchId}/scard`

## 📦 Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/CrickBot.git
   ```

2. **API Configuration**:
   The app currently uses a hardcoded RapidAPI key in `CricketApiService.kt`. For production, replace this with your own key from [RapidAPI Cricbuzz](https://rapidapi.com/cricket-api-cricket-api-default/api/cricbuzz-cricket/).

3. **Build & Run**:
   - Open the project in **Android Studio (Ladybug or newer)**.
   - Sync Gradle.
   - Run on an emulator or physical device.


---
Developed with ❤️ for Cricket fans by Antick Bhattacharjee
