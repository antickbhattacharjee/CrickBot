package com.example.crickbot.ui

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crickbot.api.RetrofitInstance
import com.example.crickbot.model.Match
import com.example.crickbot.model.Message
import com.example.crickbot.model.ScorecardData
import com.example.crickbot.model.LiveMatchesResponse
import com.example.crickbot.model.GroqMessage
import com.example.crickbot.model.GroqRequest
import com.example.crickbot.model.WeatherResponse
import com.example.crickbot.model.InningsScorecard
import com.example.crickbot.model.CommentaryResponse
import com.example.crickbot.model.Commentary
import com.google.gson.Gson
import com.example.crickbot.model.StoryDetail
import com.google.gson.reflect.TypeToken
import com.example.crickbot.BuildConfig
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response

class CricketViewModel(application: Application) : AndroidViewModel(application) {
    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> = _messages

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private var allMatches = mutableListOf<Match>()
    private val _trendingNews = mutableStateListOf<com.example.crickbot.model.StoryDetail>()
    val trendingNews: List<com.example.crickbot.model.StoryDetail> = _trendingNews
    
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

    private val commonTournaments = mapOf(
        "ipl" to listOf("ipl", "indian premier league"),
        "wpl" to listOf("wpl", "women's premier league"),
        "bbl" to listOf("bbl", "big bash"),
        "psl" to listOf("psl", "pakistan super league"),
        "cpl" to listOf("cpl", "caribbean premier league"),
        "world cup" to listOf("world cup", "wc", "t20wc", "odiwc"),
        "ashes" to listOf("ashes"),
        "asia cup" to listOf("asia cup"),
        "champions trophy" to listOf("champions trophy", "ct")
    )

    init {
        _messages.add(Message(text = "Hello! I'm CrickBot. How can I help you today? You can ask about live or recent matches.", isUser = false))
        viewModelScope.launch {
            loadMatchesFromCache()
            fetchAllData()
        }
    }

    private suspend fun loadMatchesFromCache() {
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                val file = File(getApplication<Application>().cacheDir, "matches_cache.json")
                if (file.exists()) {
                    val json = file.readText()
                    val type = object : TypeToken<List<Match>>() {}.type
                    val cached: List<Match> = Gson().fromJson(json, type)
                    allMatches.clear()
                    allMatches.addAll(cached)
                    android.util.Log.d("CrickBot", "Loaded ${allMatches.size} matches from cache")
                }
            } catch (e: Exception) {
                android.util.Log.e("CrickBot", "Error loading cache", e)
            }
        }
    }

    private fun saveMatchesToCache() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = Gson().toJson(allMatches)
                val file = File(getApplication<Application>().cacheDir, "matches_cache.json")
                file.writeText(json)
                android.util.Log.d("CrickBot", "Saved ${allMatches.size} matches to cache")
            } catch (e: Exception) {
                android.util.Log.e("CrickBot", "Failed to cache matches", e)
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            fetchAllData()
            _isLoading.value = false
        }
    }

    private suspend fun fetchAllData() {
        try {
            android.util.Log.d("CrickBot", "Fetching all matches and news...")
            
            kotlinx.coroutines.coroutineScope {
                val liveDeferred = async { RetrofitInstance.api.getLiveMatches() }
                val recentDeferred = async { RetrofitInstance.api.getRecentMatches() }
                val upcomingDeferred = async { RetrofitInstance.api.getUpcomingMatches() }
                val newsDeferred = async { RetrofitInstance.api.getTrendingNews() }

                val liveResponse = try { liveDeferred.await() } catch (e: Exception) { null }
                val recentResponse = try { recentDeferred.await() } catch (e: Exception) { null }
                val upcomingResponse = try { upcomingDeferred.await() } catch (e: Exception) { null }
                val newsResponse = try { newsDeferred.await() } catch (e: Exception) { 
                    android.util.Log.e("CrickBot", "News parallel fetch exception", e)
                    null 
                }

                val matchMap = mutableMapOf<Int, Match>()
                
                fun processResponse(response: Response<LiveMatchesResponse>?, type: String) {
                    if (response != null && response.isSuccessful) {
                        response.body()?.typeMatches?.forEach { typeMatch ->
                            typeMatch.seriesMatches?.forEach { seriesMatch ->
                                seriesMatch.seriesAdWrapper?.matches?.forEach { match ->
                                    match.matchInfo?.matchId?.let { matchMap[it] = match }
                                }
                            }
                        }
                    } else if (response != null) {
                        android.util.Log.e("CrickBot", "$type Fetch Error: ${response.code()} ${response.errorBody()?.string()}")
                    }
                }

                processResponse(liveResponse, "Live")
                processResponse(recentResponse, "Recent")
                processResponse(upcomingResponse, "Upcoming")

                if (newsResponse != null && newsResponse.isSuccessful) {
                    val news = newsResponse.body()?.storyList?.mapNotNull { it.story } ?: emptyList()
                    _trendingNews.clear()
                    _trendingNews.addAll(news)
                    android.util.Log.d("CrickBot", "Fetched ${_trendingNews.size} news stories")
                } else if (newsResponse != null) {
                    android.util.Log.e("CrickBot", "News API Error: ${newsResponse.code()}")
                }

                allMatches.clear()
                allMatches.addAll(matchMap.values)
                saveMatchesToCache()
            }
            android.util.Log.d("CrickBot", "Total unique matches loaded: ${allMatches.size}")
            
        } catch (e: Exception) {
            android.util.Log.e("CrickBot", "Global fetch error", e)
        }
    }

    fun onSendMessage(text: String) {
        if (text.isBlank()) return

        _messages.add(Message(text = text, isUser = true))
        processUserMessage(text)
    }

    private val GROQ_API_KEY = "Bearer ${BuildConfig.GROQ_API_KEY}"
    private val WEATHER_API_KEY = BuildConfig.WEATHER_API_KEY

    private fun processUserMessage(text: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val query = text.lowercase().trim()

            // 1. Handle Weather Queries and Context
            var weatherContext = ""
            if (query.contains("weather") || query.contains("rain") || query.contains("condition")) {
                val city = extractCity(query)
                if (city != null) {
                    weatherContext = fetchWeatherForContext(city)
                }
            }

            // 2. Ensure matches are loaded
            if (allMatches.isEmpty()) {
                fetchAllData()
            }

            // 3. Determine if we should show match cards
            val mentionedTeams = findMentionedTeams(query)
            val mentionedTournaments = findMentionedTournaments(query)
            val shouldShowCards = mentionedTeams.isNotEmpty() || 
                                 mentionedTournaments.isNotEmpty() ||
                                 query.contains("live") || 
                                 query.contains("recent") || 
                                 query.contains("upcoming") ||
                                 (query.contains("match") && !query.contains("summary") && !query.contains("detail"))

            // 4. Build Context for AI
            val isCricketQuery = shouldShowCards || listOf("score", "today", "ipl", "t20", "odi", "test", "who", "win", "wicket", "run", "news", "latest").any { query.contains(it) }
            val matchContext = if (isCricketQuery) buildMatchContext(mentionedTeams, mentionedTournaments, query) else ""
            
            // 5. Call AI with combined context
            val newsContext = if (query.contains("news") || query.contains("latest") || query.contains("update")) {
                buildNewsContext()
            } else ""

            val relevantNews = if (newsContext.isNotEmpty()) {
                _trendingNews.filter { story ->
                    story.headline?.lowercase()?.contains(query) == true || 
                    story.intro?.lowercase()?.contains(query) == true
                }.take(3)
            } else emptyList()

            callGroqApi(text, matchContext, weatherContext, newsContext, relevantNews)

            // 6. If appropriate, show match cards
            if (shouldShowCards) {
                performLegacySearch(query, mentionedTeams, mentionedTournaments)
            } else if (matchContext.isNotEmpty() && query.contains("match")) {
                 // Even if it didn't strictly trigger shouldShowCards (e.g. general "match info")
                 // but we found matches for context, show them if the user asked for "matches"
                 performLegacySearch(query, mentionedTeams, mentionedTournaments)
            }
            
            _isLoading.value = false
        }
    }

    private val weatherCache = mutableMapOf<String, Pair<String, Long>>()
    private val CACHE_EXPIRATION_MS = 30 * 60 * 1000 // 30 minutes

    private suspend fun fetchWeatherForContext(city: String): String {
        val normalizedCity = city.lowercase().trim()
        val cached = weatherCache[normalizedCity]
        if (cached != null && (System.currentTimeMillis() - cached.second) < CACHE_EXPIRATION_MS) {
            android.util.Log.d("CrickBot", "Using cached weather for $city")
            return cached.first
        }

        return try {
            val response = RetrofitInstance.weatherApi.getCurrentWeather(city, WEATHER_API_KEY)
            if (response.isSuccessful) {
                val weather = response.body()
                if (weather != null) {
                    val desc = weather.weather?.firstOrNull()?.description ?: "clear"
                    val temp = weather.main?.temp ?: 0.0
                    val humidity = weather.main?.humidity ?: 0
                    val wind = weather.wind?.speed ?: 0.0
                    val context = "Weather in $city: $desc, Temp: ${temp}°C, Humidity: ${humidity}%, Wind: ${wind} m/s."
                    weatherCache[normalizedCity] = Pair(context, System.currentTimeMillis())
                    context
                } else ""
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun buildMatchContext(teams: List<String>, tournaments: List<String>, query: String): String {
        val relevantMatches = if (teams.isEmpty() && tournaments.isEmpty()) {
            val q = query.lowercase()
            allMatches.filter { match ->
                val status = match.matchInfo?.status?.lowercase() ?: ""
                status.contains("live") || q.contains("live") || 
                (q.contains("today") && (status.contains("live") || status.contains("today") || status.contains("starts")))
            }.sortedByDescending { it.matchInfo?.status?.lowercase()?.contains("live") == true }
             .take(8)
        } else {
            allMatches.filter { match ->
                isMatchRelevant(match, teams, tournaments)
            }.sortedWith(compareByDescending<Match> { match ->
                var score = 0
                val info = match.matchInfo
                val status = info?.status?.lowercase() ?: ""
                
                if (teams.isNotEmpty()) {
                    val t1 = info?.team1?.teamSName?.lowercase() ?: ""
                    val t2 = info?.team2?.teamSName?.lowercase() ?: ""
                    val t1Full = info?.team1?.teamName?.lowercase() ?: ""
                    val t2Full = info?.team2?.teamName?.lowercase() ?: ""
                    
                    val teamsMatched = teams.count { teamKey ->
                        val aliases = commonTeams[teamKey] ?: listOf(teamKey)
                        aliases.any { alias -> 
                             t1.contains(alias) || t2.contains(alias) || t1Full.contains(alias) || t2Full.contains(alias)
                        }
                    }
                    score += teamsMatched * 10
                }
                
                if (status.contains("live")) score += 5
                else if (status.contains("starts") || status.contains("today")) score += 3
                else if (status.contains("won") || status.contains("lost") || status.contains("result")) score += 1
                
                score
            }).take(10)
        }

        if (relevantMatches.isEmpty() && (query.contains("live") || query.contains("today") || query.contains("match"))) {
            val backup = allMatches.filter { it.matchInfo?.status?.lowercase()?.contains("live") == true }.take(5)
            if (backup.isEmpty()) return ""
            return formatMatchContext(backup)
        }

        return formatMatchContext(relevantMatches)
    }

    private fun isMatchRelevant(match: Match, teams: List<String>, tournaments: List<String>): Boolean {
        val info = match.matchInfo ?: return false
        val t1 = info.team1?.teamSName?.lowercase() ?: ""
        val t2 = info.team2?.teamSName?.lowercase() ?: ""
        val t1Full = info.team1?.teamName?.lowercase() ?: ""
        val t2Full = info.team2?.teamName?.lowercase() ?: ""
        val series = info.seriesName?.lowercase() ?: ""

        val teamMatch = teams.any { teamKey ->
            val aliases = commonTeams[teamKey] ?: listOf(teamKey)
            aliases.any { alias ->
                t1.contains(alias) || t2.contains(alias) || t1Full.contains(alias) || t2Full.contains(alias)
            }
        }

        val tournamentMatch = tournaments.any { tourneyKey ->
            val aliases = commonTournaments[tourneyKey] ?: listOf(tourneyKey)
            aliases.any { alias -> series.contains(alias) }
        }

        return teamMatch || tournamentMatch
    }

    private fun formatMatchContext(matches: List<Match>): String {
        if (matches.isEmpty()) return ""
        
        val contextBuilder = StringBuilder("Current Cricket Context:\n")
        val maxTokens = 3000 // Approximate safety limit for context

        for (m in matches) {
            val info = m.matchInfo
            val score = m.matchScore
            val status = info?.status ?: "Unknown status"
            val team1 = info?.team1?.teamSName ?: "T1"
            val team2 = info?.team2?.teamSName ?: "T2"
            
            val t1s = score?.team1Score
            val t1Score = if (t1s?.inngs2 != null) {
                "${t1s.inngs1?.runs}/${t1s.inngs1?.wickets} & ${t1s.inngs2.runs}/${t1s.inngs2.wickets}"
            } else {
                t1s?.inngs1?.let { "${it.runs}/${it.wickets} in ${it.overs} ov" } ?: "Yet to bat"
            }

            val t2s = score?.team2Score
            val t2Score = if (t2s?.inngs2 != null) {
                "${t2s.inngs1?.runs}/${t2s.inngs1?.wickets} & ${t2s.inngs2.runs}/${t2s.inngs2.wickets}"
            } else {
                t2s?.inngs1?.let { "${it.runs}/${it.wickets} in ${it.overs} ov" } ?: "Yet to bat"
            }
            
            val matchLine = "Match: $team1 vs $team2, Series: ${info?.seriesName}, Status: $status. Scores: $team1 ($t1Score), $team2 ($t2Score).\n"
            
            // Rough estimation: 1 token ~= 4 characters
            if ((contextBuilder.length + matchLine.length) / 4 > maxTokens) break
            
            contextBuilder.append(matchLine)
        }
        
        return contextBuilder.toString()
    }

    private var newsCache: Pair<String, Long>? = null
    private val NEWS_CACHE_EXPIRATION_MS = 60 * 60 * 1000 // 1 hour

    private fun buildNewsContext(): String {
        val now = System.currentTimeMillis()
        newsCache?.let { (context, timestamp) ->
            if (now - timestamp < NEWS_CACHE_EXPIRATION_MS) {
                return context
            }
        }

        if (_trendingNews.isEmpty()) return ""
        val context = StringBuilder("Trending Cricket News:\n")
        _trendingNews.take(8).forEach { story ->
            context.append("- ${story.headline}: ${story.intro}\n")
        }
        val result = context.toString()
        newsCache = Pair(result, now)
        return result
    }

    private suspend fun callGroqApi(
        userText: String,
        matchContext: String = "",
        weatherContext: String = "",
        newsContext: String = "",
        newsStories: List<StoryDetail>? = null
    ) {
        try {
            val systemPrompt = StringBuilder("You are CrickBot, an elite cricket analyst. ")
            
            if (matchContext.isNotEmpty()) {
                systemPrompt.append("\n[LIVE MATCH DATA]\n$matchContext\nUse this for current scores/status.")
            }
            
            if (newsContext.isNotEmpty()) {
                systemPrompt.append("\n[TRENDING NEWS]\n$newsContext\nUse this for the latest developments.")
            }
            
            if (weatherContext.isNotEmpty()) {
                systemPrompt.append("\n[WEATHER INFO]\n$weatherContext\nExplain how this might impact the game (pitch, DLS, or delays).")
            }

            systemPrompt.append("\nINSTRUCTIONS:")
            systemPrompt.append("\n1. Prioritize context data for 'now' questions.")
            systemPrompt.append("\n2. Use internal knowledge for historical facts but label them as such.")
            systemPrompt.append("\n3. If context is missing for a live query, mention that match data isn't available.")
            systemPrompt.append("\n4. Keep it sporty, concise, and professional.")
            
            val request = GroqRequest(
                messages = listOf(
                    GroqMessage("system", systemPrompt.toString()),
                    GroqMessage("user", userText)
                )
            )
            val response = RetrofitInstance.groqApi.getChatCompletion(GROQ_API_KEY, request)
            if (response.isSuccessful) {
                val reply = response.body()?.choices?.firstOrNull()?.message?.content
                android.util.Log.d("CrickBot", "Groq AI Response: $reply")
                addBotMessage(reply ?: "I'm a bit stumped. Ask me about match scores!", newsStories = newsStories)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("CrickBot", "Groq API Error: ${response.code()} - $errorBody")
                if (response.code() == 429) {
                    addBotMessage("I'm receiving too many requests. Please wait a moment before asking again!")
                } else {
                    addBotMessage("My AI brain is a bit tired (Error ${response.code()}). Ask me about live scores instead!")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CrickBot", "Groq exception", e)
            addBotMessage("I'm having trouble thinking clearly. Try asking about a match.")
        }
    }

    private fun extractCity(query: String): String? {
        // Expanded keywords to include "of" and "near"
        val keywords = listOf("in ", "at ", "for ", "of ", "near ")
        for (word in keywords) {
            if (query.contains(word)) {
                val parts = query.substringAfter(word).split(" ")
                if (parts.isNotEmpty()) {
                    return parts[0].replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }
            }
        }
        return null
    }

    private fun performLegacySearch(query: String, mentionedTeams: List<String>, mentionedTournaments: List<String>) {
        val filteredMatches = when {
            mentionedTeams.isNotEmpty() || mentionedTournaments.isNotEmpty() -> {
                allMatches.filter { isMatchRelevant(it, mentionedTeams, mentionedTournaments) }
                    .sortedWith(compareByDescending<Match> { it.matchInfo?.status?.lowercase()?.contains("live") == true }
                        .thenByDescending { it.matchInfo?.status?.lowercase()?.contains("today") == true }
                    )
            }
            query.contains("live") || query.contains("today") -> {
                allMatches.filter { 
                    val s = it.matchInfo?.status?.lowercase() ?: ""
                    s.contains("live") || s.contains("today") || s.contains("starts")
                }.sortedByDescending { it.matchInfo?.status?.lowercase()?.contains("live") == true }
            }
            query.contains("recent") || query.contains("result") || query.contains("yesterday") -> {
                allMatches.filter { it.matchInfo?.status?.lowercase()?.let { s -> s.contains("won") || s.contains("lost") || s.contains("result") } == true }
            }
            query.contains("upcoming") || query.contains("tomorrow") -> {
                allMatches.filter { it.matchInfo?.status?.lowercase()?.contains("starts") == true }
            }
            else -> {
                // Default fallback if "matches" is mentioned but no specific filter
                if (query.contains("match")) {
                    allMatches.filter { it.matchInfo?.status?.lowercase()?.contains("live") == true }.take(5)
                } else emptyList()
            }
        }

        if (filteredMatches.isNotEmpty()) {
            filteredMatches.take(5).forEach { match ->
                val team1 = match.matchInfo?.team1?.teamName ?: "Team 1"
                val team2 = match.matchInfo?.team2?.teamName ?: "Team 2"
                addBotMessage("Match: $team1 vs $team2", matchData = match)
            }
        } else if (mentionedTeams.isNotEmpty() || mentionedTournaments.isNotEmpty()) {
            addBotMessage("No matches found matching your request currently.")
        }
    }

    private fun findMentionedTeams(query: String): List<String> {
        val found = mutableSetOf<String>()
        for ((fullName, aliases) in commonTeams) {
            if (aliases.any { alias -> 
                val pattern = "\\b${Regex.escape(alias)}\\b".toRegex()
                pattern.containsMatchIn(query)
            }) {
                found.add(fullName)
            }
        }
        return found.toList()
    }

    private fun findMentionedTournaments(query: String): List<String> {
        val found = mutableSetOf<String>()
        for ((fullName, aliases) in commonTournaments) {
            if (aliases.any { alias -> 
                val pattern = "\\b${Regex.escape(alias)}\\b".toRegex()
                pattern.containsMatchIn(query)
            }) {
                found.add(fullName)
            }
        }
        return found.toList()
    }

    private fun addBotMessage(text: String, matchData: Match? = null, newsStories: List<StoryDetail>? = null) {
        _messages.add(Message(text = text, isUser = false, matchData = matchData, newsStories = newsStories))
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
                        _messages.add(Message(
                            text = "Scorecard data is not yet available for this match.",
                            isUser = false,
                            isError = true,
                            errorAction = { fetchScorecard(match) }
                        ))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("CrickBot", "Scorecard fetch failed: ${response.code()} - $errorBody")
                    _messages.add(Message(
                        text = "Unable to fetch scorecard (Error ${response.code()}).",
                        isUser = false,
                        isError = true,
                        errorAction = { fetchScorecard(match) }
                    ))
                }
            } catch (e: Exception) {
                android.util.Log.e("CrickBot", "Exception fetching scorecard", e)
                _messages.add(Message(
                    text = "Network error while fetching scorecard.",
                    isUser = false,
                    isError = true,
                    errorAction = { fetchScorecard(match) }
                ))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchCommentary(match: Match) {
        val matchId = match.matchInfo?.matchId ?: return
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                android.util.Log.d("CrickBot", "Fetching commentary for matchId: $matchId")
                val response = RetrofitInstance.api.getCommentary(matchId)
                if (response.isSuccessful) {
                    val commList = response.body()?.commentaryList
                    if (!commList.isNullOrEmpty()) {
                        _messages.add(
                            Message(
                                text = "Recent commentary for ${match.matchInfo.team1?.teamSName} vs ${match.matchInfo.team2?.teamSName}:",
                                isUser = false,
                                commentaryData = com.example.crickbot.model.CommentaryData(
                                    matchId = matchId,
                                    commentary = commList.take(10)
                                )
                            )
                        )
                    } else {
                        _messages.add(Message(
                            text = "No commentary available for this match.",
                            isUser = false,
                            isError = true,
                            errorAction = { fetchCommentary(match) }
                        ))
                    }
                } else {
                    _messages.add(Message(
                        text = "Failed to load commentary.",
                        isUser = false,
                        isError = true,
                        errorAction = { fetchCommentary(match) }
                    ))
                }
            } catch (e: Exception) {
                android.util.Log.e("CrickBot", "Commentary exception", e)
                _messages.add(Message(
                    text = "Error loading commentary.",
                    isUser = false,
                    isError = true,
                    errorAction = { fetchCommentary(match) }
                ))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
