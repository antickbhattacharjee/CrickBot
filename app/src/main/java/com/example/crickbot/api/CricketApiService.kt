package com.example.crickbot.api

import com.example.crickbot.model.LiveMatchesResponse
import com.example.crickbot.model.NewsResponse
import com.example.crickbot.model.ScorecardResponse
import com.example.crickbot.model.CommentaryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface CricketApiService {
    @GET("matches/v1/live")
    suspend fun getLiveMatches(
        @Header("x-rapidapi-key") apiKey: String = "49463dacd9mshdb62add85a5aa9cp1aa42fjsne5fad75f70e0",
        @Header("x-rapidapi-host") host: String = "cricbuzz-cricket.p.rapidapi.com"
    ): Response<LiveMatchesResponse>

    @GET("matches/v1/recent")
    suspend fun getRecentMatches(
        @Header("x-rapidapi-key") apiKey: String = "49463dacd9mshdb62add85a5aa9cp1aa42fjsne5fad75f70e0",
        @Header("x-rapidapi-host") host: String = "cricbuzz-cricket.p.rapidapi.com"
    ): Response<LiveMatchesResponse>

    @GET("matches/v1/upcoming")
    suspend fun getUpcomingMatches(
        @Header("x-rapidapi-key") apiKey: String = "49463dacd9mshdb62add85a5aa9cp1aa42fjsne5fad75f70e0",
        @Header("x-rapidapi-host") host: String = "cricbuzz-cricket.p.rapidapi.com"
    ): Response<LiveMatchesResponse>

    @GET("mcenter/v1/{matchId}/scard")
    suspend fun getScorecard(
        @Path("matchId") matchId: Int,
        @Header("x-rapidapi-key") apiKey: String = "49463dacd9mshdb62add85a5aa9cp1aa42fjsne5fad75f70e0",
        @Header("x-rapidapi-host") host: String = "cricbuzz-cricket.p.rapidapi.com"
    ): Response<ScorecardResponse>

    @GET("mcenter/v1/{matchId}/comm")
    suspend fun getCommentary(
        @Path("matchId") matchId: Int,
        @Header("x-rapidapi-key") apiKey: String = "49463dacd9mshdb62add85a5aa9cp1aa42fjsne5fad75f70e0",
        @Header("x-rapidapi-host") host: String = "cricbuzz-cricket.p.rapidapi.com"
    ): Response<CommentaryResponse>

    @GET("news/v1/index")
    suspend fun getTrendingNews(
        @Header("x-rapidapi-key") apiKey: String = "49463dacd9mshdb62add85a5aa9cp1aa42fjsne5fad75f70e0",
        @Header("x-rapidapi-host") host: String = "cricbuzz-cricket.p.rapidapi.com"
    ): Response<NewsResponse>
}
