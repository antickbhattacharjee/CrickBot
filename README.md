# CrickBot 🏏

CrickBot is a conversational Android application that acts as your personal cricket assistant. It provides real-time information on live matches, recent results, and upcoming schedules, along with detailed match scorecards and integrated news.

## 🚀 Features

- **Real-time Scores**: Get live updates for ongoing matches across the globe.
- **AI-Powered Chat**: Ask about teams, specific matchups, or general cricket trivia using the integrated Groq LLM.
- **Dedicated News Tab**: Stay updated with the latest trending cricket news stories.
- **Detailed Scorecards**: View comprehensive match details including batting/bowling stats and fall of wickets.
- **Contextual Intelligence**: CrickBot uses live news and match data to provide smarter, more accurate answers.
- **Weather Integration**: Check weather conditions for match venues.

## 🛠 Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for a modern, reactive user interface.
- **Language**: Kotlin.
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & OkHttp for API communication.
- **AI Engine**: [Groq Cloud API](https://groq.com/) for lightning-fast LLM responses.
- **Architecture**: MVVM (Model-View-ViewModel) with State-driven UI.
- **Navigation**: Modern Compose Navigation with Tabbed interface.

## 🔌 API Integration

- **Cricbuzz API**: Real-time match data, scores, and commentary.
- **Groq API**: Natural language processing for the cricket assistant.
- **Weather API**: Real-time weather data for match locations.

## 📦 Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/CrickBot.git
   ```

2. **API Configuration**:
   Create a `local.properties` file in the root directory and add your API keys:
   ```properties
   GROQ_API_KEY=your_groq_api_key_here
   WEATHER_API_KEY=your_weather_api_key_here
   ```
   *Note: The RapidAPI key is managed within the service configuration.*

3. **Build & Run**:
   - Open the project in **Android Studio (Ladybug or newer)**.
   - Sync Gradle.
   - Run on an emulator or physical device.

---
Developed with ❤️ for Cricket fans by Antick Bhattacharjee
