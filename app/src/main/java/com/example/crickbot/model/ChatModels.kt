package com.example.crickbot.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val matchData: Match? = null,
    val scorecardData: ScorecardData? = null,
    val commentaryData: CommentaryData? = null,
    val newsStories: List<StoryDetail>? = null,
    val isError: Boolean = false,
    val errorAction: (() -> Unit)? = null
)

data class ScorecardData(
    val matchId: Int,
    val innings: List<InningsScorecard>
)

data class CommentaryData(
    val matchId: Int,
    val commentary: List<Commentary>
)

data class ScorecardResponse(
    // Changed from "scoreCard" to "scorecard" to match API
    @SerializedName("scorecard") val scoreCard: List<InningsScorecard>?
)

data class InningsScorecard(
    @SerializedName("batTeamName") val batTeamName: String? = null,
    @SerializedName("batteamname") val batteamname: String? = null,
    @SerializedName("runs") val runs: Int? = null,
    @SerializedName("score") val score: Int? = null,
    @SerializedName("wickets") val wickets: Int? = null,
    @SerializedName("overs") val overs: Any? = null,
    
    // Updated to match the "batsman", "bowler", "fow" structure in your JSON
    @SerializedName("batsman") val batsmen: List<Batsman>? = null,
    @SerializedName("bowler") val bowlers: List<Bowler>? = null,
    @SerializedName("fow") val fowData: FowWrapper? = null,
    @SerializedName("partnership") val partnershipData: PartnershipWrapper? = null
) {
    val teamNameDisplay: String get() = batTeamName ?: batteamname ?: "Unknown"
    val runsDisplay: Int get() = runs ?: score ?: 0
    val oversDisplay: String get() = overs?.toString() ?: "0.0"
}

data class FowWrapper(
    @SerializedName("fow") val fowList: List<FallOfWicket>? = null
)

data class PartnershipWrapper(
    @SerializedName("partnership") val partnershipList: List<Partnership>? = null
)

data class FallOfWicket(
    @SerializedName("batsmanname") val name: String? = null,
    @SerializedName("runs") val runs: Int? = null,
    @SerializedName("overnbr") val over: Double? = null,
    @SerializedName("batsmanid") val id: Int? = null
)

data class Partnership(
    @SerializedName("bat1name") val batter1Name: String? = null,
    @SerializedName("bat1runs") val batter1Runs: Int? = null,
    @SerializedName("bat1balls") val batter1Balls: Int? = null,
    @SerializedName("bat2name") val batter2Name: String? = null,
    @SerializedName("bat2runs") val batter2Runs: Int? = null,
    @SerializedName("bat2balls") val batter2Balls: Int? = null,
    @SerializedName("totalruns") val totalRuns: Int? = null,
    @SerializedName("totalballs") val totalBalls: Int? = null
)

data class Batsman(
    @SerializedName("name") val name: String? = null,
    @SerializedName("runs") val runs: Int? = null,
    @SerializedName("balls") val balls: Int? = null,
    @SerializedName("fours") val fours: Int? = null,
    @SerializedName("sixes") val sixes: Int? = null,
    @SerializedName("strkrate") val strikeRate: Any? = null,
    @SerializedName("outdec") val outDesc: String? = null
) {
    val srDisplay: String get() = strikeRate?.toString() ?: "0.0"
}

data class Bowler(
    @SerializedName("name") val name: String? = null,
    @SerializedName("overs") val overs: Any? = null,
    @SerializedName("maidens") val maidens: Int? = null,
    @SerializedName("runs") val runs: Int? = null,
    @SerializedName("wickets") val wickets: Int? = null,
    @SerializedName("economy") val economy: Any? = null
) {
    val oversDisplay: String get() = overs?.toString() ?: "0.0"
    val economyDisplay: String get() = economy?.toString() ?: "0.0"
}

// Commentary Models
data class CommentaryResponse(
    @SerializedName("commentaryList") val commentaryList: List<Commentary>?
)

data class Commentary(
    @SerializedName("commText") val commText: String?,
    @SerializedName("overNumber") val overNumber: Double?,
    @SerializedName("timestamp") val timestamp: Long?,
    @SerializedName("overSep") val overSep: OverSeparator?
)

data class OverSeparator(
    @SerializedName("score") val score: Int?,
    @SerializedName("wickets") val wickets: Int?,
    @SerializedName("overs") val overs: Double?
)

// News Models
data class NewsResponse(
    @SerializedName("storyList") val storyList: List<StoryWrapper>?
)

data class StoryWrapper(
    @SerializedName("story") val story: StoryDetail?
)

data class StoryDetail(
    @SerializedName("id") val id: Int?,
    @SerializedName("hline") val headline: String?,
    @SerializedName("intro") val intro: String?,
    @SerializedName("pubTime") val pubTime: String?
)
