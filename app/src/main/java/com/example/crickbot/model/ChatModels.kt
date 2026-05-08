package com.example.crickbot.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val matchData: Match? = null,
    val scorecardData: ScorecardData? = null
)

data class ScorecardData(
    val matchId: Int,
    val innings: List<InningsScorecard>
)

data class ScorecardResponse(
    @SerializedName("scoreCard") val scoreCard: List<InningsScorecard>?
)

data class InningsScorecard(
    @SerializedName("batTeamName") val batTeamName: String? = null,
    @SerializedName("batteamname") val batteamname: String? = null,
    @SerializedName("runs") val runs: Int? = null,
    @SerializedName("score") val score: Int? = null,
    @SerializedName("wickets") val wickets: Int? = null,
    @SerializedName("overs") val overs: Any? = null,
    @SerializedName("batTable") val batTable: BatTable? = null,
    @SerializedName("bowlTable") val bowlTable: BowlTable? = null,
    @SerializedName("fowTable") val fowTable: FowTable? = null,
    @SerializedName("partnershipsTable") val partnershipsTable: PartnershipTable? = null
) {
    val teamNameDisplay: String get() = batTeamName ?: batteamname ?: "Unknown"
    val runsDisplay: Int get() = runs ?: score ?: 0
    val oversDisplay: String get() = overs?.toString() ?: "0.0"
}

data class BatTable(
    @SerializedName("batDiffList") val batsmen: List<Batsman>? = null
)

data class BowlTable(
    @SerializedName("bowlDiffList") val bowlers: List<Bowler>? = null
)

data class FowTable(
    @SerializedName("fowDiffList") val fowList: List<FallOfWicket>? = null
)

data class PartnershipTable(
    @SerializedName("partnershipDiffList") val partnershipList: List<Partnership>? = null
)

data class FallOfWicket(
    @SerializedName("wktName") val name: String? = null,
    @SerializedName("wktRuns") val runs: Int? = null,
    @SerializedName("wktOver") val over: Double? = null,
    @SerializedName("wktOrder") val order: Int? = null
)

data class Partnership(
    @SerializedName("bat1Name") val batter1Name: String? = null,
    @SerializedName("bat1Runs") val batter1Runs: Int? = null,
    @SerializedName("bat1Balls") val batter1Balls: Int? = null,
    @SerializedName("bat2Name") val batter2Name: String? = null,
    @SerializedName("bat2Runs") val batter2Runs: Int? = null,
    @SerializedName("bat2Balls") val batter2Balls: Int? = null,
    @SerializedName("totalRuns") val totalRuns: Int? = null,
    @SerializedName("totalBalls") val totalBalls: Int? = null
)

data class Batsman(
    @SerializedName("batName") val name: String? = null,
    @SerializedName("runs") val runs: Int? = null,
    @SerializedName("balls") val balls: Int? = null,
    @SerializedName("fours") val fours: Int? = null,
    @SerializedName("sixes") val sixes: Int? = null,
    @SerializedName("strikeRate") val strikeRate: Any? = null
) {
    val srDisplay: String get() = strikeRate?.toString() ?: "0.0"
}

data class Bowler(
    @SerializedName("bowlName") val name: String? = null,
    @SerializedName("overs") val overs: Any? = null,
    @SerializedName("maidens") val maidens: Int? = null,
    @SerializedName("runs") val runs: Int? = null,
    @SerializedName("wickets") val wickets: Int? = null,
    @SerializedName("economy") val economy: Any? = null
) {
    val oversDisplay: String get() = overs?.toString() ?: "0.0"
    val economyDisplay: String get() = economy?.toString() ?: "0.0"
}
