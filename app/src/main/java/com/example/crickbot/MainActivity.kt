package com.example.crickbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crickbot.model.Match
import com.example.crickbot.model.Message
import com.example.crickbot.ui.CricketViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: CricketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF1A237E),
                    secondary = Color(0xFF00C853),
                    background = Color(0xFFF0F2F5)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(viewModel: CricketViewModel) {
    val messages = viewModel.messages
    val isLoading by viewModel.isLoading
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🏏", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("CrickBot", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Online", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask about a match...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.onSendMessage(inputText)
                                inputText = ""
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send"
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    MessageItem(message, viewModel)
                }
                if (isLoading) {
                    item {
                        TypingIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, viewModel: CricketViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        if (message.text.isNotEmpty()) {
            Surface(
                color = if (message.isUser) MaterialTheme.colorScheme.primary else Color.White,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                ),
                tonalElevation = 2.dp,
                shadowElevation = 1.dp,
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = if (message.isUser) Color.White else Color.Black,
                    fontSize = 15.sp
                )
            }
        }
        
        message.matchData?.let { match ->
            Spacer(modifier = Modifier.height(4.dp))
            MatchCard(match) {
                viewModel.fetchScorecard(match)
            }
        }

        message.scorecardData?.let { scorecard ->
            Spacer(modifier = Modifier.height(4.dp))
            ScorecardCard(scorecard)
        }
    }
}

@Composable
fun MatchCard(match: Match, onViewDetails: () -> Unit) {
    Card(
        modifier = Modifier
            .widthIn(max = 300.dp)
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = match.matchInfo?.seriesName ?: "Series info unavailable",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallTeamInfo(
                    name = match.matchInfo?.team1?.teamSName ?: "T1",
                    score = match.matchScore?.team1Score?.let { s ->
                        val i1 = s.inngs1
                        val i2 = s.inngs2
                        if (i2 != null) "${i1?.runs}/${i1?.wickets} & ${i2.runs}/${i2.wickets}"
                        else i1?.let { "${it.runs}/${it.wickets}" } ?: "---"
                    } ?: "---"
                )
                Text("vs", fontWeight = FontWeight.Bold, color = Color.LightGray, fontSize = 12.sp)
                SmallTeamInfo(
                    name = match.matchInfo?.team2?.teamSName ?: "T2",
                    score = match.matchScore?.team2Score?.let { s ->
                        val i1 = s.inngs1
                        val i2 = s.inngs2
                        if (i2 != null) "${i1?.runs}/${i1?.wickets} & ${i2.runs}/${i2.wickets}"
                        else i1?.let { "${it.runs}/${it.wickets}" } ?: "---"
                    } ?: "---"
                ,
                    alignEnd = true
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.matchInfo?.status ?: "Status unavailable",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onViewDetails, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                    Text("Details", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScorecardCard(scorecard: com.example.crickbot.model.ScorecardData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "MATCH SCORECARD",
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            scorecard.innings.forEachIndexed { index, innings ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = innings.teamNameDisplay,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${innings.runsDisplay}/${innings.wickets ?: 0} (${innings.oversDisplay})",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }

                val batsmen = innings.batsmen
                if (!batsmen.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Batting", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    
                    // Header
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Text("Batter", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("R", modifier = Modifier.width(30.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("B", modifier = Modifier.width(30.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("SR", modifier = Modifier.width(40.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    batsmen.forEach { batsman ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(batsman.name ?: "-", modifier = Modifier.weight(1f), fontSize = 12.sp)
                                Text("${batsman.runs ?: 0}", modifier = Modifier.width(30.dp), fontSize = 12.sp)
                                Text("${batsman.balls ?: 0}", modifier = Modifier.width(30.dp), fontSize = 12.sp)
                                Text(batsman.srDisplay, modifier = Modifier.width(40.dp), fontSize = 11.sp)
                            }
                            if (!batsman.outDesc.isNullOrEmpty()) {
                                Text(
                                    text = batsman.outDesc,
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                    }
                }

                val bowlers = innings.bowlers
                if (!bowlers.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Bowling", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    
                    // Header
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Text("Bowler", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("O", modifier = Modifier.width(30.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("R", modifier = Modifier.width(30.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("W", modifier = Modifier.width(30.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    bowlers.forEach { bowler ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Text(bowler.name ?: "-", modifier = Modifier.weight(1f), fontSize = 12.sp)
                            Text(bowler.oversDisplay, modifier = Modifier.width(30.dp), fontSize = 12.sp)
                            Text("${bowler.runs ?: 0}", modifier = Modifier.width(30.dp), fontSize = 12.sp)
                            Text("${bowler.wickets ?: 0}", modifier = Modifier.width(30.dp), fontSize = 12.sp)
                        }
                    }
                }

                val fows = innings.fowData?.fowList
                if (!fows.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Fall of Wickets", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        fows.forEachIndexed { idx, fow ->
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    text = "${fow.runs}/${idx + 1} (${fow.name})",
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                val partnerships = innings.partnershipData?.partnershipList
                if (!partnerships.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Key Partnerships", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    partnerships.take(3).forEach { p ->
                        Text(
                            text = "${p.totalRuns} (${p.totalBalls}) - ${p.batter1Name} & ${p.batter2Name}",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                
                if (index < scorecard.innings.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun SmallTeamInfo(name: String, score: String, alignEnd: Boolean = false) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(name, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        Text(score, fontSize = 12.sp, color = Color.DarkGray)
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("CrickBot is thinking...", fontSize = 12.sp, color = Color.Gray)
    }
}
