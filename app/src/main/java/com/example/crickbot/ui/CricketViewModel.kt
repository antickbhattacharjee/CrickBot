package com.example.crickbot.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crickbot.api.RetrofitInstance
import com.example.crickbot.model.Match
import com.example.crickbot.model.Message
import com.example.crickbot.model.ScorecardData
import com.example.crickbot.model.LiveMatchesResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response

class CricketViewModel : ViewModel() {
    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> = _messages

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private var allMatches = mutableListOf<Match>()
    
    private val commonTeams = mapOf(
        "india" to listOf("india", "ind"),
        "pakistan" to listOf("pakistan", "pak"),
        "australia" to listOf("australia", "aus"),
        "england" to listOf("england", "eng"),
        "south africa" to listOf("south africa", "rsa", "sa"),
        "new zealand" to listOf("new zealand", "nz"),
        "west indies" to listOf("west indies", "wi"),
        "sri lanka" to listOf("sri lanka", "sl"),
        "bangladesh" to listOf("bangladesh", "ban"),
        "afghanistan" to listOf("afghanistan", "afg"),
        "csk" to listOf("csk", "chennai", "super kings"),
        "mi" to listOf("mi", "mumbai", "indians"),
        "rcb" to listOf("rcb", "bangalore", "royal challengers"),
        "dc" to listOf("dc", "delhi", "capitals"),
        "kkr" to listOf("kkr", "kolkata", "knight riders"),
        "lsg" to listOf("lsg", "lucknow", "supergiants"),
        "gt" to listOf("gt", "gujarat", "titans"),
        "pbks" to listOf("pbks", "punjab", "kings"),
        "rr" to listOf("rr", "rajasthan", "royals"),
        "srh" to listOf("srh", "hyderabad", "sunrisers")
    )

    init {
        _messages.add(Message(text = "Hello! I'm CrickBot. How can I help you today? You can ask about live or recent matches.", isUser = false))
        viewModelScope.launch { fetchAllData() }
    }

    private suspend fun fetchAllData() {
        try {
            android.util.Log.d("CrickBot", "Fetching all matches...")
            
            kotlinx.coroutines.coroutineScope {
                val liveDeferred = async { RetrofitInstance.api.getLiveMatches() }
                val recentDeferred = async { RetrofitInstance.api.getRecentMatches() }
                val upcomingDeferred = async { RetrofitInstance.api.getUpcomingMatches() }

                val liveResponse = liveDeferred.await()
                val recentResponse = recentDeferred.await()
                val upcomingResponse = upcomingDeferred.await()

                val matchMap = mutableMapOf<Int, Match>()
                
                fun processResponse(response: Response<LiveMatchesResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.typeMatches?.forEach { typeMatch ->
                            typeMatch.seriesMatches?.forEach { seriesMatch ->
                                seriesMatch.seriesAdWrapper?.matches?.forEach { match ->
                                    match.matchInfo?.matchId?.let { matchMap[it] = match }
                                }
                            }
                        }
                    }
                }

                processResponse(liveResponse)
                processResponse(recentResponse)
                processResponse(upcomingResponse)

                allMatches.clear()
                allMatches.addAll(matchMap.values)
            }
            android.util.Log.d("CrickBot", "Total unique matches loaded: ${allMatches.size}")
            
        } catch (e: Exception) {
            android.util.Log.e("CrickBot", "Error fetching data", e)
        }
    }

    fun onSendMessage(text: String) {
        if (text.isBlank()) return

        _messages.add(Message(text = text, isUser = true))
        processUserMessage(text)
    }

    private fun processUserMessage(text: String) {
        viewModelScope.launch {
            _isLoading.value = true
            delay(600) 
            
            val query = text.lowercase().trim()
            
            if (allMatches.isEmpty()) {
                fetchAllData()
                if (allMatches.isEmpty()) {
                    addBotMessage("I'm having trouble connecting to the match service. Please check your internet or try again later.")
                    _isLoading.value = false
                    return@launch
                }
            }

            val mentionedTeams = findMentionedTeams(query)
            
            when {
                mentionedTeams.isNotEmpty() -> {
                    val filteredMatches = allMatches.filter { match ->
                        val team1Name = match.matchInfo?.team1?.teamName?.lowercase() ?: ""
                        val team1SName = match.matchInfo?.team1?.teamSName?.lowercase() ?: ""
                        val team2Name = match.matchInfo?.team2?.teamName?.lowercase() ?: ""
                        val team2SName = match.matchInfo?.team2?.teamSName?.lowercase() ?: ""
                        
                        if (mentionedTeams.size >= 2) {
                            val t1Key = mentionedTeams.find { key ->
                                val aliases = commonTeams[key] ?: listOf(key)
                                aliases.any { team1Name.contains(it) || team1SName.contains(it) }
                            }
                            val t2Key = mentionedTeams.find { key ->
                                val aliases = commonTeams[key] ?: listOf(key)
                                aliases.any { team2Name.contains(it) || team2SName.contains(it) }
                            }
                            t1Key != null && t2Key != null && t1Key != t2Key
                        } else {
                            val key = mentionedTeams.first()
                            val aliases = commonTeams[key] ?: listOf(key)
                            aliases.any { alias ->
                                team1Name.contains(alias) || team1SName.contains(alias) ||
                                team2Name.contains(alias) || team2SName.contains(alias)
                            }
                        }
                    }
                    
                    if (filteredMatches.isNotEmpty()) {
                        val teamNames = mentionedTeams.joinToString(" vs ") { it.uppercase() }
                        addBotMessage("I found these matches for $teamNames:")
                        filteredMatches.take(5).forEach { match ->
                            _messages.add(Message(text = "", isUser = false, matchData = match))
                        }
                    } else {
                        val teamNames = mentionedTeams.joinToString(" and ") { it.uppercase() }
                        addBotMessage("I couldn't find any recent or upcoming matches between $teamNames. Here are some other games:")
                        allMatches.take(3).forEach { match ->
                            _messages.add(Message(text = "", isUser = false, matchData = match))
                        }
                    }
                }
                query.contains("live") || query.contains("score") || query.contains("current") -> {
                    val liveMatches = allMatches.filter { 
                        it.matchInfo?.status?.lowercase()?.contains("live") == true || 
                        it.matchScore != null 
                    }
                    if (liveMatches.isEmpty()) {
                        addBotMessage("No live matches at the moment. Here are some recent results:")
                        allMatches.filter { it.matchScore != null }.take(3).forEach { match ->
                            _messages.add(Message(text = "", isUser = false, matchData = match))
                        }
                    } else {
                        addBotMessage("Here are the live matches:")
                        liveMatches.take(5).forEach { match ->
                            _messages.add(Message(text = "", isUser = false, matchData = match))
                        }
                    }
                }
                query.contains("recent") || query.contains("result") || query.contains("last") -> {
                    addBotMessage("Latest match results:")
                    allMatches.filter { it.matchScore != null }.take(8).forEach { match ->
                        _messages.add(Message(text = "", isUser = false, matchData = match))
                    }
                }
                else -> {
                    val searchMatches = allMatches.filter { 
                        it.matchInfo?.seriesName?.lowercase()?.contains(query) == true ||
                        it.matchInfo?.team1?.teamName?.lowercase()?.contains(query) == true ||
                        it.matchInfo?.team2?.teamName?.lowercase()?.contains(query) == true
                    }
                    
                    if (searchMatches.isNotEmpty()) {
                        addBotMessage("I found these matches for '$query':")
                        searchMatches.take(3).forEach { match ->
                            _messages.add(Message(text = "", isUser = false, matchData = match))
                        }
                    } else {
                        addBotMessage("I didn't quite catch that. Try searching for a team (like 'India' or 'CSK') or ask for 'live' scores.")
                    }
                }
            }
            _isLoading.value = false
        }
    }

    private fun findMentionedTeams(query: String): List<String> {
        val found = mutableSetOf<String>()
        for ((fullName, aliases) in commonTeams) {
            if (aliases.any { alias -> 
                query.contains(alias) 
            }) {
                found.add(fullName)
            }
        }
        return found.toList()
    }

    private fun addBotMessage(text: String) {
        _messages.add(Message(text = text, isUser = false))
    }

    fun fetchScorecard(match: Match) {
        val matchId = match.matchInfo?.matchId ?: return
        
        // Prevent multiple fetches for the same match scorecard if already loading
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                android.util.Log.d("CrickBot", "Fetching scorecard for matchId: $matchId")
                val response = RetrofitInstance.api.getScorecard(matchId)
                
                if (response.isSuccessful) {
                    val scorecardResponse = response.body()
                    val inningsList = scorecardResponse?.scoreCard
                    
                    android.util.Log.d("CrickBot", "Scorecard response successful. Innings count: ${inningsList?.size ?: 0}")

                    if (!inningsList.isNullOrEmpty()) {
                        _messages.add(
                            Message(
                                text = "Detailed scorecard for ${match.matchInfo.team1?.teamSName} vs ${match.matchInfo.team2?.teamSName}:",
                                isUser = false,
                                scorecardData = ScorecardData(
                                    matchId = matchId,
                                    innings = inningsList
                                )
                            )
                        )
                    } else {
                        android.util.Log.w("CrickBot", "Scorecard list is empty for match $matchId")
                        addBotMessage("Scorecard data is not yet available for this match.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("CrickBot", "Scorecard fetch failed: ${response.code()} - $errorBody")
                    addBotMessage("Unable to fetch scorecard (Error ${response.code()}).")
                }
            } catch (e: Exception) {
                android.util.Log.e("CrickBot", "Exception fetching scorecard", e)
                addBotMessage("Network error. Please try again.")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
