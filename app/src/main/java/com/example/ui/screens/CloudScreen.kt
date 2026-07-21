package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CloudFileEntity
import com.example.data.local.CloudFolderEntity
import com.example.data.local.StoragePlanEntity
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var activeSubTab by remember { mutableStateOf("FILES") } // "FILES", "SHARED", "RECENTS_FAVS", "BACKUPS", "STORAGE", "TRASH"

    val cloudFiles by viewModel.cloudFiles.collectAsStateWithLifecycle()
    val trashedCloudFiles by viewModel.trashedCloudFiles.collectAsStateWithLifecycle()
    val favoriteCloudFiles by viewModel.favoriteCloudFiles.collectAsStateWithLifecycle()
    val cloudFolders by viewModel.cloudFolders.collectAsStateWithLifecycle()
    val sharedCloudFiles by viewModel.sharedCloudFiles.collectAsStateWithLifecycle()
    val userQuota by viewModel.userQuota.collectAsStateWithLifecycle()
    val storagePlans by viewModel.storagePlans.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()

    var showUploadFileDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var selectedFileForDetail by remember { mutableStateOf<CloudFileEntity?>(null) }
    var showShareDialogForFile by remember { mutableStateOf<CloudFileEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "WorldConnect Cloud",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            val usedGb = String.format(Locale.US, "%.1f", (userQuota?.usedBytes ?: 0) / (1024.0 * 1024.0 * 1024.0))
                            val totalGb = String.format(Locale.US, "%.0f", (userQuota?.totalQuotaBytes ?: (10L * 1024 * 1024 * 1024)) / (1024.0 * 1024.0 * 1024.0))
                            Text(
                                "Stockage Sécurisé : $usedGb Go / $totalGb Go",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { activeSubTab = "STORAGE" }) {
                        Icon(Icons.Default.PieChart, contentDescription = "Statistiques de Stockage")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        floatingActionButton = {
            if (activeSubTab == "FILES") {
                FloatingActionButton(
                    onClick = { showUploadFileDialog = true },
                    modifier = Modifier.testTag("fab_upload_cloud_file"),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Téléverser Fichier")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Navigation Scroll Tabs
            ScrollableTabRow(
                selectedTabIndex = when (activeSubTab) {
                    "FILES" -> 0
                    "SHARED" -> 1
                    "RECENTS_FAVS" -> 2
                    "BACKUPS" -> 3
                    "STORAGE" -> 4
                    else -> 5
                },
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = activeSubTab == "FILES",
                    onClick = { activeSubTab = "FILES" },
                    text = { Text("Mes Fichiers (${cloudFiles.size})") },
                    icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                    modifier = Modifier.testTag("tab_cloud_files")
                )
                Tab(
                    selected = activeSubTab == "SHARED",
                    onClick = { activeSubTab = "SHARED" },
                    text = { Text("Partagés (${sharedCloudFiles.size})") },
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    modifier = Modifier.testTag("tab_cloud_shared")
                )
                Tab(
                    selected = activeSubTab == "RECENTS_FAVS",
                    onClick = { activeSubTab = "RECENTS_FAVS" },
                    text = { Text("Favoris (${favoriteCloudFiles.size})") },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    modifier = Modifier.testTag("tab_cloud_favs")
                )
                Tab(
                    selected = activeSubTab == "BACKUPS",
                    onClick = { activeSubTab = "BACKUPS" },
                    text = { Text("Sauvegardes") },
                    icon = { Icon(Icons.Default.Backup, contentDescription = null) },
                    modifier = Modifier.testTag("tab_cloud_backups")
                )
                Tab(
                    selected = activeSubTab == "STORAGE",
                    onClick = { activeSubTab = "STORAGE" },
                    text = { Text("Offres & Quota") },
                    icon = { Icon(Icons.Default.Storage, contentDescription = null) },
                    modifier = Modifier.testTag("tab_cloud_storage")
                )
                Tab(
                    selected = activeSubTab == "TRASH",
                    onClick = { activeSubTab = "TRASH" },
                    text = { Text("Corbeille (${trashedCloudFiles.size})") },
                    icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    modifier = Modifier.testTag("tab_cloud_trash")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub Tab Body
            Box(modifier = Modifier.weight(1.0f)) {
                when (activeSubTab) {
                    "FILES" -> CloudFilesSubTabContent(
                        files = cloudFiles,
                        folders = cloudFolders,
                        onFileClick = { selectedFileForDetail = it },
                        onToggleFavorite = { viewModel.toggleCloudFileFavorite(it.id) },
                        onToggleOffline = { viewModel.toggleCloudFileOffline(it.id) },
                        onTrashFile = { viewModel.trashCloudFile(it.id) },
                        onShareFile = { showShareDialogForFile = it },
                        onCreateFolderClick = { showCreateFolderDialog = true },
                        viewModel = viewModel
                    )
                    "SHARED" -> CloudSharedSubTabContent(
                        sharedFiles = sharedCloudFiles,
                        allFiles = cloudFiles
                    )
                    "RECENTS_FAVS" -> CloudFavsSubTabContent(
                        favoriteFiles = favoriteCloudFiles,
                        onFileClick = { selectedFileForDetail = it },
                        onToggleFavorite = { viewModel.toggleCloudFileFavorite(it.id) }
                    )
                    "BACKUPS" -> CloudBackupsSubTabContent(
                        userQuota = userQuota,
                        onUpdateBackupSettings = { photos, videos, docs ->
                            viewModel.updateBackupSettings(photos, videos, docs)
                            Toast.makeText(context, "Paramètres de sauvegarde automatique mis à jour", Toast.LENGTH_SHORT).show()
                        }
                    )
                    "STORAGE" -> CloudStoragePlansSubTabContent(
                        userQuota = userQuota,
                        plans = storagePlans,
                        onUpgradePlan = { planId ->
                            viewModel.upgradeStoragePlan(planId)
                            Toast.makeText(context, "Abonnement mis à jour avec succès !", Toast.LENGTH_LONG).show()
                        }
                    )
                    "TRASH" -> CloudTrashSubTabContent(
                        trashedFiles = trashedCloudFiles,
                        onRestore = { viewModel.restoreCloudFile(it.id) },
                        onDeletePermanently = { viewModel.deleteCloudFilePermanently(it.id) }
                    )
                }
            }
        }
    }

    // --- Dialogs ---
    if (showUploadFileDialog) {
        UploadFileDialog(
            folders = cloudFolders,
            onDismiss = { showUploadFileDialog = false },
            onConfirm = { name, type, ext, sizeMb, folderId ->
                val bytes = (sizeMb * 1024 * 1024).toLong()
                viewModel.uploadCloudFile(
                    name = name,
                    fileType = type,
                    extension = ext,
                    sizeBytes = bytes,
                    folderId = folderId
                )
                showUploadFileDialog = false
                Toast.makeText(context, "Fichier téléversé dans WorldConnect Cloud !", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showCreateFolderDialog) {
        CreateCloudFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { name, colorHex ->
                viewModel.createCloudFolder(name = name, colorHex = colorHex)
                showCreateFolderDialog = false
                Toast.makeText(context, "Dossier Cloud créé", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (selectedFileForDetail != null) {
        CloudFileDetailSheet(
            file = selectedFileForDetail!!,
            onDismiss = { selectedFileForDetail = null },
            onToggleFavorite = {
                viewModel.toggleCloudFileFavorite(selectedFileForDetail!!.id)
                selectedFileForDetail = null
            },
            onToggleOffline = {
                viewModel.toggleCloudFileOffline(selectedFileForDetail!!.id)
                selectedFileForDetail = null
            },
            onTrash = {
                viewModel.trashCloudFile(selectedFileForDetail!!.id)
                selectedFileForDetail = null
            },
            onAiAnalyze = {
                val current = selectedFileForDetail!!
                coroutineScope.launch {
                    val (ocr, summary) = viewModel.analyzeCloudFileWithAi(current.name, current.fileType)
                    viewModel.updateCloudFile(current.copy(ocrText = ocr, aiSummary = summary))
                    Toast.makeText(context, "Analyse IA Gemini terminée!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (showShareDialogForFile != null) {
        ShareCloudFileDialog(
            file = showShareDialogForFile!!,
            contacts = contacts,
            onDismiss = { showShareDialogForFile = null },
            onConfirmShare = { targetName, perm, isPass ->
                viewModel.shareCloudFile(
                    fileId = showShareDialogForFile!!.id,
                    sharedWithUserName = targetName,
                    permissionLevel = perm,
                    isPasswordProtected = isPass
                )
                showShareDialogForFile = null
                Toast.makeText(context, "Lien de partage généré et envoyé à $targetName", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// --- SUB-TAB 1: MES FICHIERS ---
@Composable
fun CloudFilesSubTabContent(
    files: List<CloudFileEntity>,
    folders: List<CloudFolderEntity>,
    onFileClick: (CloudFileEntity) -> Unit,
    onToggleFavorite: (CloudFileEntity) -> Unit,
    onToggleOffline: (CloudFileEntity) -> Unit,
    onTrashFile: (CloudFileEntity) -> Unit,
    onShareFile: (CloudFileEntity) -> Unit,
    onCreateFolderClick: () -> Unit,
    viewModel: AppViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("TOUS") } // TOUS, IMAGE, VIDEO, PDF, DOC, AUDIO, ZIP
    var selectedFolderId by remember { mutableStateOf<Long?>(null) }

    val filteredFiles = remember(files, searchQuery, selectedTypeFilter, selectedFolderId) {
        files.filter { file ->
            val matchesFolder = (selectedFolderId == null || file.folderId == selectedFolderId)
            val matchesSearch = searchQuery.isBlank() ||
                    file.name.contains(searchQuery, ignoreCase = true) ||
                    (file.ocrText?.contains(searchQuery, ignoreCase = true) == true) ||
                    (file.aiSummary?.contains(searchQuery, ignoreCase = true) == true)

            val matchesType = when (selectedTypeFilter) {
                "TOUS" -> true
                else -> file.fileType == selectedTypeFilter
            }

            matchesFolder && matchesSearch && matchesType
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Recherche intelligente (Nom, Texte OCR, IA)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = null) } }
            } else null,
            modifier = Modifier.fillMaxWidth().testTag("search_cloud_files_input"),
            singleLine = true,
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Folders Bar
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedFolderId == null,
                    onClick = { selectedFolderId = null },
                    label = { Text("Tous les dossiers") },
                    leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
            items(folders) { folder ->
                FilterChip(
                    selected = selectedFolderId == folder.id,
                    onClick = { selectedFolderId = folder.id },
                    label = { Text(folder.name) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(folder.colorHex)))
                        )
                    }
                )
            }
            item {
                IconButton(onClick = onCreateFolderClick) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "Nouveau dossier Cloud", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Type Filters
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val types = listOf("TOUS", "IMAGE", "VIDEO", "PDF", "DOC", "AUDIO", "ZIP")
            items(types) { t ->
                SuggestionChip(
                    onClick = { selectedTypeFilter = t },
                    label = { Text(t) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (selectedTypeFilter == t)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Files Grid
        if (filteredFiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Aucun fichier trouvé dans WorldConnect Cloud", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredFiles, key = { it.id }) { file ->
                    CloudFileCardItem(
                        file = file,
                        onClick = { onFileClick(file) },
                        onToggleFavorite = { onToggleFavorite(file) },
                        onShare = { onShareFile(file) },
                        onTrash = { onTrashFile(file) }
                    )
                }
            }
        }
    }
}

@Composable
fun CloudFileCardItem(
    file: CloudFileEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onTrash: () -> Unit
) {
    val fileIcon = when (file.fileType) {
        "IMAGE" -> Icons.Default.Image
        "VIDEO" -> Icons.Default.Movie
        "PDF" -> Icons.Default.PictureAsPdf
        "AUDIO" -> Icons.Default.AudioFile
        "ZIP" -> Icons.Default.FolderZip
        else -> Icons.Default.Description
    }

    val fileColor = when (file.fileType) {
        "IMAGE" -> Color(0xFFE91E63)
        "VIDEO" -> Color(0xFF9C27B0)
        "PDF" -> Color(0xFFF44336)
        "AUDIO" -> Color(0xFFFF9800)
        "ZIP" -> Color(0xFF795548)
        else -> Color(0xFF2196F3)
    }

    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("cloud_file_card_${file.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(fileColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(fileIcon, contentDescription = null, tint = fileColor, modifier = Modifier.size(20.dp))
                }

                Row {
                    if (file.isFavorite) {
                        Icon(Icons.Default.Star, contentDescription = "Favori", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                    }
                    if (file.isOfflineAvailable) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.OfflinePin, contentDescription = "Hors ligne", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(if (file.isFavorite) "Retirer des favoris" else "Ajouter aux favoris") },
                                onClick = { onToggleFavorite(); showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Partager le lien") },
                                onClick = { onShare(); showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Mettre à la corbeille") },
                                onClick = { onTrash(); showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = file.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            val formattedSize = remember(file.sizeBytes) {
                val mb = file.sizeBytes / (1024.0 * 1024.0)
                if (mb >= 1000) String.format(Locale.US, "%.1f Go", mb / 1024.0)
                else String.format(Locale.US, "%.1f Mo", mb)
            }

            Text(
                text = "$formattedSize • ${file.extension.uppercase(Locale.getDefault())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!file.aiSummary.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Résumé IA prêt", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

// --- SUB-TAB 2: FICHIERS PARTAGÉS ---
@Composable
fun CloudSharedSubTabContent(
    sharedFiles: List<com.example.data.local.SharedCloudFileEntity>,
    allFiles: List<CloudFileEntity>
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        Text("Fichiers et Liens Partagés WorldConnect", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))

        if (sharedFiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun fichier partagé pour le moment", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sharedFiles, key = { it.id }) { item ->
                    val file = allFiles.find { it.id == item.fileId }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(file?.name ?: "Fichier #${item.fileId}", fontWeight = FontWeight.Bold)
                                Text("Partagé avec: ${item.sharedWithUserName} (${item.permissionLevel})", style = MaterialTheme.typography.bodySmall)
                                Text(item.shareLink ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-TAB 3: FAVORIS ---
@Composable
fun CloudFavsSubTabContent(
    favoriteFiles: List<CloudFileEntity>,
    onFileClick: (CloudFileEntity) -> Unit,
    onToggleFavorite: (CloudFileEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        Text("Fichiers Favoris", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))

        if (favoriteFiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun fichier dans vos favoris", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(favoriteFiles, key = { it.id }) { file ->
                    CloudFileCardItem(
                        file = file,
                        onClick = { onFileClick(file) },
                        onToggleFavorite = { onToggleFavorite(file) },
                        onShare = {},
                        onTrash = {}
                    )
                }
            }
        }
    }
}

// --- SUB-TAB 4: SAUVEGARDES AUTOMATIQUES ---
@Composable
fun CloudBackupsSubTabContent(
    userQuota: com.example.data.local.UserQuotaEntity?,
    onUpdateBackupSettings: (Boolean, Boolean, Boolean) -> Unit
) {
    var photos by remember(userQuota) { mutableStateOf(userQuota?.autoBackupPhotos ?: true) }
    var videos by remember(userQuota) { mutableStateOf(userQuota?.autoBackupVideos ?: false) }
    var docs by remember(userQuota) { mutableStateOf(userQuota?.autoBackupDocs ?: true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Sauvegarde Automatique de l'Appareil", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Text(
            "Vos médias et données sont synchronisés en arrière-plan sur les serveurs sécurisés WorldConnect.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Photos de la galerie", fontWeight = FontWeight.Bold)
                        Text("Sauvegarder automatiquement les photos", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = photos, onCheckedChange = { photos = it; onUpdateBackupSettings(photos, videos, docs) })
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Vidéos Haute Définition", fontWeight = FontWeight.Bold)
                        Text("Uniquement en Wi-Fi", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = videos, onCheckedChange = { videos = it; onUpdateBackupSettings(photos, videos, docs) })
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Documents, Notes & Pièces Jointes", fontWeight = FontWeight.Bold)
                        Text("Base de données locale WorldConnect", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = docs, onCheckedChange = { docs = it; onUpdateBackupSettings(photos, videos, docs) })
                }
            }
        }
    }
}

// --- SUB-TAB 5: ABONNEMENTS ET QUOTA ---
@Composable
fun CloudStoragePlansSubTabContent(
    userQuota: com.example.data.local.UserQuotaEntity?,
    plans: List<StoragePlanEntity>,
    onUpgradePlan: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Gestion du Stockage & Abonnements", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        Spacer(modifier = Modifier.height(12.dp))

        // Progress Gauge Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val usedGb = (userQuota?.usedBytes ?: 0) / (1024.0 * 1024.0 * 1024.0)
                val totalGb = (userQuota?.totalQuotaBytes ?: (10L * 1024 * 1024 * 1024)) / (1024.0 * 1024.0 * 1024.0)
                val fraction = (usedGb / totalGb).coerceIn(0.0, 1.0).toFloat()

                Text("Espace Utilisé : ${String.format(Locale.US, "%.1f", usedGb)} Go sur ${String.format(Locale.US, "%.0f", totalGb)} Go", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Offres de Stockage WorldConnect Cloud", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(plans) { plan ->
                val isCurrent = userQuota?.currentPlanId == plan.planId
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.0f)) {
                            Text(plan.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("${plan.quotaGb} Go de stockage cloud", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Text(plan.description, style = MaterialTheme.typography.labelSmall)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (plan.priceMonthlyEur == 0.0) "Gratuit" else "${plan.priceMonthlyEur} €/mois", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            if (isCurrent) {
                                Button(onClick = {}, enabled = false) { Text("Actif") }
                            } else {
                                Button(
                                    onClick = { onUpgradePlan(plan.planId) },
                                    modifier = Modifier.testTag("btn_upgrade_plan_${plan.planId}")
                                ) {
                                    Text("Choisir")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-TAB 6: CORBEILLE ---
@Composable
fun CloudTrashSubTabContent(
    trashedFiles: List<CloudFileEntity>,
    onRestore: (CloudFileEntity) -> Unit,
    onDeletePermanently: (CloudFileEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        Text("Corbeille WorldConnect Cloud", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Text("Les fichiers supprimés sont conservés 30 jours avant suppression définitive.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(8.dp))

        if (trashedFiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("La corbeille est vide", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(trashedFiles, key = { it.id }) { file ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(file.name, fontWeight = FontWeight.Bold)
                                Text("${file.fileType} • ${file.extension}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { onRestore(file) }) {
                                Icon(Icons.Default.RestoreFromTrash, contentDescription = "Restaurer", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { onDeletePermanently(file) }) {
                                Icon(Icons.Default.DeleteForever, contentDescription = "Supprimer définitivement", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- DIALOGS & SHEETS ---

@Composable
fun UploadFileDialog(
    folders: List<CloudFolderEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double, Long?) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var fileType by remember { mutableStateOf("DOC") }
    var extension by remember { mutableStateOf("pdf") }
    var sizeMb by remember { mutableStateOf("12.5") }
    var selectedFolderId by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Téléverser un Fichier") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Nom du fichier") },
                    modifier = Modifier.fillMaxWidth().testTag("input_cloud_filename")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = extension,
                        onValueChange = { extension = it },
                        label = { Text("Extension") },
                        modifier = Modifier.weight(1.0f)
                    )
                    OutlinedTextField(
                        value = sizeMb,
                        onValueChange = { sizeMb = it },
                        label = { Text("Taille (Mo)") },
                        modifier = Modifier.weight(1.0f)
                    )
                }

                Text("Type de fichier :", fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val types = listOf("DOC", "IMAGE", "VIDEO", "PDF", "AUDIO", "ZIP")
                    items(types) { t ->
                        FilterChip(
                            selected = fileType == t,
                            onClick = { fileType = t; if (t == "IMAGE") extension = "jpg" else if (t == "PDF") extension = "pdf" },
                            label = { Text(t) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fileName.isNotBlank()) {
                        val size = sizeMb.toDoubleOrNull() ?: 5.0
                        onConfirm(fileName, fileType, extension, size, selectedFolderId)
                    }
                },
                modifier = Modifier.testTag("btn_confirm_upload_cloud")
            ) {
                Text("Téléverser")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun CreateCloudFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    var colorHex by remember { mutableStateOf("#2196F3") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau Dossier Cloud") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Nom du dossier") },
                    modifier = Modifier.fillMaxWidth().testTag("input_cloud_folder_name")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (folderName.isNotBlank()) {
                        onConfirm(folderName, colorHex)
                    }
                },
                modifier = Modifier.testTag("btn_confirm_cloud_folder")
            ) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudFileDetailSheet(
    file: CloudFileEntity,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleOffline: () -> Unit,
    onTrash: () -> Unit,
    onAiAnalyze: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(file.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text("Type: ${file.fileType} • Taille: ${file.sizeBytes / 1024} Ko", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            if (!file.aiSummary.isNullOrBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyse & Résumé IA Gemini", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(file.aiSummary, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(onClick = onAiAnalyze, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lancer Extraction OCR & Résumé IA")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onToggleFavorite) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (file.isFavorite) "Défavoriser" else "Favori")
                }
                OutlinedButton(onClick = onToggleOffline) {
                    Icon(Icons.Default.OfflinePin, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (file.isOfflineAvailable) "En ligne" else "Hors ligne")
                }
                OutlinedButton(onClick = onTrash, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Corbeille")
                }
            }
        }
    }
}

@Composable
fun ShareCloudFileDialog(
    file: CloudFileEntity,
    contacts: List<com.example.data.local.UserProfileEntity>,
    onDismiss: () -> Unit,
    onConfirmShare: (String, String, Boolean) -> Unit
) {
    var targetUser by remember { mutableStateOf(if (contacts.isNotEmpty()) contacts.first().name else "Tous les membres") }
    var permissionLevel by remember { mutableStateOf("READ") } // READ, COMMENT, EDIT
    var isPasswordProtected by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Partager Fichier WorldConnect") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Fichier : ${file.name}", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = targetUser,
                    onValueChange = { targetUser = it },
                    label = { Text("Destinataire ou Groupe") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Niveau d'autorisation :", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("READ", "COMMENT", "EDIT").forEach { perm ->
                        FilterChip(
                            selected = permissionLevel == perm,
                            onClick = { permissionLevel = perm },
                            label = { Text(perm) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmShare(targetUser, permissionLevel, isPasswordProtected) },
                modifier = Modifier.testTag("btn_confirm_share_cloud_file")
            ) {
                Text("Générer Lien Partagé")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
