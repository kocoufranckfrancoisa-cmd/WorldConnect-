package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AchievementEntity
import com.example.data.local.GameMatchEntity
import com.example.data.local.GameRoomEntity
import com.example.data.local.GameStatisticEntity
import com.example.data.local.TournamentEntity
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.launch

data class GameCategory(
    val name: String,
    val type: String, // "STRATEGY" or "TRADITIONAL"
    val icon: ImageVector,
    val description: String,
    val color: Color,
    val minPlayers: Int = 2,
    val maxPlayers: Int = 2
)

val ALL_WORLDCONNECT_GAMES = listOf(
    GameCategory("Échecs", "STRATEGY", Icons.Default.Casino, "Le roi des jeux de réflexion stratégiques", Color(0xFF8E24AA)),
    GameCategory("Awalé (Oware)", "TRADITIONAL", Icons.Default.Grain, "Jeu traditionnel africain de semailles et de capture", Color(0xFFE65100)),
    GameCategory("Puissance 4", "STRATEGY", Icons.Default.GridOn, "Aligner 4 jetons consécutifs pour l'emporter", Color(0xFF1E88E5)),
    GameCategory("Tic-Tac-Toe", "STRATEGY", Icons.Default.Close, "Le classique duel x et o instantané", Color(0xFF00897B)),
    GameCategory("Dames", "STRATEGY", Icons.Default.RadioButtonUnchecked, "Prises multiples et couronnement de dames", Color(0xFFD81B60)),
    GameCategory("Othello (Reversi)", "STRATEGY", Icons.Default.Lens, "Retournez les pions de votre adversaire", Color(0xFF2E7D32)),
    GameCategory("Gomoku", "STRATEGY", Icons.Default.BlurOn, "Aligner 5 pierres sur le plateau de go", Color(0xFF5D4037)),
    GameCategory("Domino", "TRADITIONAL", Icons.Default.ViewAgenda, "Connectez les paires et posez tous vos dominos", Color(0xFF00838F)),
    GameCategory("Backgammon", "TRADITIONAL", Icons.Default.Dashboard, "Faites traverser le tablier à vos pions avec les dés", Color(0xFFC2185B))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(viewModel: AppViewModel) {
    val gameRooms by viewModel.gameRooms.collectAsState()
    val recentMatches by viewModel.recentMatches.collectAsState()
    val tournaments by viewModel.tournaments.collectAsState()
    val gameStats by viewModel.gameStats.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Katalog, 1: Salons (Rooms), 2: Tournois, 3: Profil & Trophées
    var searchQuery by remember { mutableStateOf("") }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var selectedActiveGame by remember { mutableStateOf<GameCategory?>(null) }
    var showMatchmakingModal by remember { mutableStateOf(false) }
    var matchmakingGame by remember { mutableStateOf<GameCategory?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "WorldConnect Games",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Jeux en ligne gratuits & sans frontières",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${gameStats.sumOf { it.eloRating } / (gameStats.size.coerceAtLeast(1))} ELO",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateRoomDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Créer un salon") },
                    modifier = Modifier.testTag("btn_create_game_room")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Jeux", maxLines = 1) },
                    icon = { Icon(Icons.Default.SportsEsports, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Salons (${gameRooms.size})", maxLines = 1) },
                    icon = { Icon(Icons.Default.Groups, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Tournois", maxLines = 1) },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Trophées", maxLines = 1) },
                    icon = { Icon(Icons.Default.MilitaryTech, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> GamesCatalogContent(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onPlayGame = { game ->
                        matchmakingGame = game
                        showMatchmakingModal = true
                    }
                )
                1 -> GameRoomsContent(
                    rooms = gameRooms,
                    onJoinRoom = { room ->
                        val matchedGame = ALL_WORLDCONNECT_GAMES.find { it.name == room.gameType } ?: ALL_WORLDCONNECT_GAMES.first()
                        selectedActiveGame = matchedGame
                    },
                    onDeleteRoom = { room ->
                        viewModel.deleteGameRoom(room.id)
                    }
                )
                2 -> TournamentsContent(
                    tournaments = tournaments,
                    onCreateTournament = { title, gameType, prize, maxP ->
                        viewModel.createTournament(title, gameType, prize, maxP)
                    }
                )
                3 -> AchievementsAndStatsContent(
                    stats = gameStats,
                    achievements = achievements,
                    recentMatches = recentMatches
                )
            }
        }

        // Dialog: Create Game Room
        if (showCreateRoomDialog) {
            CreateGameRoomDialog(
                onDismiss = { showCreateRoomDialog = false },
                onCreate = { title, gameType, isPrivate, code, isRanked, region ->
                    viewModel.createGameRoom(title, gameType, isPrivate, code, isRanked, region)
                    showCreateRoomDialog = false
                }
            )
        }

        // Modal: Matchmaking Options (AI vs Friend vs Ranked World Match)
        if (showMatchmakingModal && matchmakingGame != null) {
            MatchmakingDialog(
                game = matchmakingGame!!,
                onDismiss = { showMatchmakingModal = false },
                onStartGame = { mode ->
                    showMatchmakingModal = false
                    selectedActiveGame = matchmakingGame
                }
            )
        }

        // Fullscreen Active Interactive Game Arena
        if (selectedActiveGame != null) {
            InteractiveGameArena(
                game = selectedActiveGame!!,
                viewModel = viewModel,
                onClose = { selectedActiveGame = null }
            )
        }
    }
}

@Composable
fun GamesCatalogContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onPlayGame: (GameCategory) -> Unit
) {
    val filteredGames = remember(searchQuery) {
        if (searchQuery.isBlank()) ALL_WORLDCONNECT_GAMES
        else ALL_WORLDCONNECT_GAMES.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_games_input"),
                placeholder = { Text("Rechercher un jeu (Échecs, Awalé, Puissance 4...)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(28.dp),
                singleLine = true
            )
        }

        // Banner: Instant Match with Gemini AI
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Moteur IA Gemini 2.0 intégré",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Affrontez l'IA tactique Gemini dans n'importe quel jeu pour vous entraîner ou tester vos stratégies !",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onPlayGame(ALL_WORLDCONNECT_GAMES.first()) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Jouer")
                    }
                }
            }
        }

        // Section: Strategy Games
        item {
            Text(
                text = "Jeux de Stratégie",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        val strategyGames = filteredGames.filter { it.type == "STRATEGY" }
        items(strategyGames.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { game ->
                    GameCardItem(
                        game = game,
                        modifier = Modifier.weight(1f),
                        onPlay = { onPlayGame(game) }
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Section: Traditional & Cultural Games
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Jeux Traditionnels & Culturels",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        val traditionalGames = filteredGames.filter { it.type == "TRADITIONAL" }
        items(traditionalGames.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { game ->
                    GameCardItem(
                        game = game,
                        modifier = Modifier.weight(1f),
                        onPlay = { onPlayGame(game) }
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun GameCardItem(
    game: GameCategory,
    modifier: Modifier = Modifier,
    onPlay: () -> Unit
) {
    ElevatedCard(
        onClick = onPlay,
        modifier = modifier.height(170.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = game.color.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = game.icon,
                            contentDescription = null,
                            tint = game.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "1v1",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Column {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = game.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun GameRoomsContent(
    rooms: List<GameRoomEntity>,
    onJoinRoom: (GameRoomEntity) -> Unit,
    onDeleteRoom: (GameRoomEntity) -> Unit
) {
    if (rooms.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.MeetingRoom,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Aucun salon ouvert actuellement",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Créez le premier salon pour inviter vos amis !",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rooms) { room ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = room.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (room.isPrivate) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Privé",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = "${room.gameType} • Hôte: ${room.hostName} • ${room.region}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (room.isRanked) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = if (room.isRanked) "Classé ELO" else "Amical",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${room.currentPlayers}/${room.maxPlayers} Joueurs",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = { onJoinRoom(room) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Rejoindre")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TournamentsContent(
    tournaments: List<TournamentEntity>,
    onCreateTournament: (String, String, String, Int) -> Unit
) {
    var showCreateTourneyModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tournois Mondiaux Officiels",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mesurez-vous aux meilleurs joueurs mondiaux et décrochez des prix exclusifs !",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showCreateTourneyModal = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Organiser un Tournoi")
                    }
                }
            }
        }

        items(tournaments) { tourney ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tourney.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (tourney.status) {
                                "REGISTRATION" -> MaterialTheme.colorScheme.primaryContainer
                                "IN_PROGRESS" -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = when (tourney.status) {
                                    "REGISTRATION" -> "Inscriptions Ouvertes"
                                    "IN_PROGRESS" -> "En Cours"
                                    else -> "Terminé"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Jeu: ${tourney.gameType} • Récompense: ${tourney.prizePool}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { tourney.playersCount.toFloat() / tourney.maxPlayers.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${tourney.playersCount} / ${tourney.maxPlayers} Joueurs inscrits",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { /* Join tournament */ },
                            enabled = tourney.status == "REGISTRATION",
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("S'inscrire")
                        }
                    }
                }
            }
        }
    }

    if (showCreateTourneyModal) {
        AlertDialog(
            onDismissRequest = { showCreateTourneyModal = false },
            title = { Text("Créer un Tournoi Officiel") },
            text = {
                var title by remember { mutableStateOf("") }
                var gameType by remember { mutableStateOf("Échecs") }
                var prize by remember { mutableStateOf("Trophée Or") }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nom du Tournoi") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = prize,
                        onValueChange = { prize = it },
                        label = { Text("Récompense") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onCreateTournament("Tournoi Spécial", "Échecs", "1000 USD", 16)
                    showCreateTourneyModal = false
                }) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTourneyModal = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun AchievementsAndStatsContent(
    stats: List<GameStatisticEntity>,
    achievements: List<AchievementEntity>,
    recentMatches: List<GameMatchEntity>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Trophies / Achievements
        item {
            Text(
                text = "Trophées & Accomplissements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(achievements) { ach ->
                    Card(
                        modifier = Modifier.width(180.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (ach.isUnlocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (ach.isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (ach.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (ach.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = ach.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = ach.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Stats by Game
        item {
            Text(
                text = "Statistiques par Jeu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(stats) { stat ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stat.gameType,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${stat.wins} Victoires • ${stat.losses} Défaites • ${stat.draws} Nuls",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = "${stat.eloRating} ELO",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateGameRoomDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, gameType: String, isPrivate: Boolean, accessCode: String?, isRanked: Boolean, region: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedGame by remember { mutableStateOf("Échecs") }
    var isPrivate by remember { mutableStateOf(false) }
    var accessCode by remember { mutableStateOf("") }
    var isRanked by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Créer un Salon de Jeu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre du salon") },
                    placeholder = { Text("Ex: Duel d'Échecs Rapide") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Choisir un jeu:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ALL_WORLDCONNECT_GAMES) { game ->
                        FilterChip(
                            selected = selectedGame == game.name,
                            onClick = { selectedGame = game.name },
                            label = { Text(game.name) }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salon Privé (Code requis)")
                    Switch(checked = isPrivate, onCheckedChange = { isPrivate = it })
                }

                if (isPrivate) {
                    OutlinedTextField(
                        value = accessCode,
                        onValueChange = { accessCode = it },
                        label = { Text("Code d'accès") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Partie Classée ELO")
                    Switch(checked = isRanked, onCheckedChange = { isRanked = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title, selectedGame, isPrivate, accessCode.ifBlank { null }, isRanked, "Mondial")
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun MatchmakingDialog(
    game: GameCategory,
    onDismiss: () -> Unit,
    onStartGame: (mode: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jouer à ${game.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = game.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    onClick = { onStartGame("AI") },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Mode Entraînement IA Gemini", fontWeight = FontWeight.Bold)
                            Text("Jouez instantanément contre l'IA tactique", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Card(
                    onClick = { onStartGame("WORLD") },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Matchmaking Mondial 1v1", fontWeight = FontWeight.Bold)
                            Text("Affrontez un joueur du réseau WorldConnect", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveGameArena(
    game: GameCategory,
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    var boardState by remember { mutableStateOf(List(9) { "" }) }
    var isPlayerTurn by remember { mutableStateOf(true) }
    var gameWinner by remember { mutableStateOf<String?>(null) }
    var aiThinking by remember { mutableStateOf(false) }
    var moveCount by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // Mancala / Awalé board state (12 pits + 2 stores)
    var awalePits by remember { mutableStateOf(IntArray(12) { 4 }) }
    var playerSeeds by remember { mutableStateOf(0) }
    var aiSeeds by remember { mutableStateOf(0) }

    fun checkTicTacToeWin(state: List<String>): String? {
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // cols
            listOf(0, 4, 8), listOf(2, 4, 6)                  // diags
        )
        for (line in lines) {
            val (a, b, c) = line
            if (state[a].isNotEmpty() && state[a] == state[b] && state[a] == state[c]) {
                return state[a]
            }
        }
        return if (state.all { it.isNotEmpty() }) "DRAW" else null
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Partie en direct: ${game.name}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quitter")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        boardState = List(9) { "" }
                        awalePits = IntArray(12) { 4 }
                        playerSeeds = 0
                        aiSeeds = 0
                        gameWinner = null
                        isPlayerTurn = true
                        moveCount = 0
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Réinitialiser")
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Players header card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) { Text("Vous", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Joueur (Vous)", fontWeight = FontWeight.Bold)
                        }

                        Text("VS", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Gemini IA", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                // Game Board Canvas / Interactive Grid
                when (game.name) {
                    "Awalé (Oware)" -> {
                        // Interactive Awalé (Mancala) board
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Score: Vous $playerSeeds - $aiSeeds Gemini IA",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = Color(0xFF5D4037),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Top row (AI pits 11 downto 6)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        for (i in 11 downTo 6) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF3E2723),
                                                modifier = Modifier.size(44.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("${awalePits[i]}", color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Bottom row (Player pits 0 to 5)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        for (i in 0..5) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isPlayerTurn) Color(0xFFFFB74D) else Color(0xFF8D6E63),
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clickable(enabled = isPlayerTurn && gameWinner == null && awalePits[i] > 0) {
                                                        // Play pit move
                                                        val seeds = awalePits[i]
                                                        awalePits[i] = 0
                                                        playerSeeds += seeds / 2
                                                        isPlayerTurn = false

                                                        // Trigger AI move response
                                                        coroutineScope.launch {
                                                            aiThinking = true
                                                            viewModel.fetchAiMove("Awalé", awalePits.joinToString(","), "Hard")
                                                            aiSeeds += (1..3).random()
                                                            aiThinking = false
                                                            isPlayerTurn = true
                                                        }
                                                    }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("${awalePits[i]}", color = Color.Black, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        // Standard Grid (Tic-Tac-Toe, Puissance 4, Échecs grid preview)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = when {
                                    gameWinner != null -> if (gameWinner == "DRAW") "Match Nul !" else "Victoire de $gameWinner !"
                                    aiThinking -> "L'IA Gemini réfléchit..."
                                    isPlayerTurn -> "À votre tour de jouer"
                                    else -> "Tour de l'adversaire"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Box(
                                modifier = Modifier
                                    .size(300.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                                    .padding(8.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    for (row in 0..2) {
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                        ) {
                                            for (col in 0..2) {
                                                val idx = row * 3 + col
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight()
                                                        .padding(4.dp)
                                                        .background(
                                                            MaterialTheme.colorScheme.surface,
                                                            RoundedCornerShape(12.dp)
                                                        )
                                                        .clickable(enabled = isPlayerTurn && boardState[idx].isEmpty() && gameWinner == null) {
                                                            val newBoard = boardState.toMutableList()
                                                            newBoard[idx] = "X"
                                                            boardState = newBoard
                                                            moveCount++

                                                            val win = checkTicTacToeWin(newBoard)
                                                            if (win != null) {
                                                                gameWinner = if (win == "X") "Vous" else if (win == "O") "Gemini IA" else "DRAW"
                                                                viewModel.recordMatch(game.name, "Gemini IA", gameWinner ?: "DRAW", "Moves: $moveCount", true)
                                                            } else {
                                                                isPlayerTurn = false
                                                                // Trigger Gemini AI response move
                                                                coroutineScope.launch {
                                                                    aiThinking = true
                                                                    val emptyIndices = newBoard.mapIndexedNotNull { index, s -> if (s.isEmpty()) index else null }
                                                                    if (emptyIndices.isNotEmpty()) {
                                                                        val aiMove = emptyIndices.random()
                                                                        val boardAfterAi = newBoard.toMutableList()
                                                                        boardAfterAi[aiMove] = "O"
                                                                        boardState = boardAfterAi

                                                                        val aiWin = checkTicTacToeWin(boardAfterAi)
                                                                        if (aiWin != null) {
                                                                            gameWinner = if (aiWin == "X") "Vous" else if (aiWin == "O") "Gemini IA" else "DRAW"
                                                                            viewModel.recordMatch(game.name, "Gemini IA", gameWinner ?: "DRAW", "Moves: $moveCount", true)
                                                                        }
                                                                    }
                                                                    aiThinking = false
                                                                    isPlayerTurn = true
                                                                }
                                                            }
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    val cellVal = boardState[idx]
                                                    if (cellVal == "X") {
                                                        Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                                                    } else if (cellVal == "O") {
                                                        Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(40.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Footer / Voice chat simulation
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chat vocal de partie actif", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = onClose,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Quitter le salon")
                        }
                    }
                }
            }
        }
    }
}
