package com.example.crickbot.model

import com.google.gson.annotations.SerializedName

// Groq Models
data class GroqRequest(
    val model: String = "llama-3.3-70b-versatile",
    val messages: List<GroqMessage>,
    val max_tokens: Int = 500
)

data class GroqMessage(
    val role: String,
    val content: String
)

data class GroqResponse(
    val choices: List<GroqChoice>
)

data class GroqChoice(
    val message: GroqMessage
)

// Weather Models
data class WeatherResponse(
    val name: String,
    val main: MainWeather?,
    val weather: List<WeatherDescription>?,
    val wind: Wind?
)

data class MainWeather(
    val temp: Double,
    val humidity: Int,
    val pressure: Int
)

data class WeatherDescription(
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double
)
