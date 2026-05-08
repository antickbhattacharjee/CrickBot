package com.example.crickbot.model

import com.google.gson.annotations.SerializedName

data class LiveMatchesResponse(
    @SerializedName("type") val type: String?,
    @SerializedName("typeMatches") val typeMatches: List<TypeMatch>?
)

data class TypeMatch(
    @SerializedName("matchType") val matchType: String?,
    @SerializedName("seriesMatches") val seriesMatches: List<SeriesMatch>?
)

data class SeriesMatch(
    @SerializedName("seriesAdWrapper") val seriesAdWrapper: SeriesAdWrapper?
)

data class SeriesAdWrapper(
    @SerializedName("seriesName") val seriesName: String?,
    @SerializedName("matches") val matches: List<Match>?
)

data class Match(
    @SerializedName("matchInfo") val matchInfo: MatchInfo?,
    @SerializedName("matchScore") val matchScore: MatchScore?
)

data class MatchInfo(
    @SerializedName("matchId") val matchId: Int?,
    @SerializedName("seriesName") val seriesName: String?,
    @SerializedName("matchDesc") val matchDesc: String?,
    @SerializedName("team1") val team1: Team?,
    @SerializedName("team2") val team2: Team?,
    @SerializedName("status") val status: String?
)

data class Team(
    @SerializedName("teamName") val teamName: String?,
    @SerializedName("teamSName") val teamSName: String?
)

data class MatchScore(
    @SerializedName("team1Score") val team1Score: TeamScore?,
    @SerializedName("team2Score") val team2Score: TeamScore?
)

data class TeamScore(
    @SerializedName("inngs1") val inngs1: Innings?,
    @SerializedName("inngs2") val inngs2: Innings?
)

data class Innings(
    @SerializedName("runs") val runs: Int?,
    @SerializedName("wickets") val wickets: Int?,
    @SerializedName("overs") val overs: Double?
)
