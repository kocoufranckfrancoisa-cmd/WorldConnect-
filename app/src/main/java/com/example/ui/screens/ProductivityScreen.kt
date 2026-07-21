package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CalendarEventEntity
import com.example.data.local.NoteEntity
import com.example.data.local.NoteFolderEntity
import com.example.data.local.TaskEntity
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductivityScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Sub-Tab state: "NOTES", "TASKS", "CALENDAR", "VOICE_DRAW", "INTEGRATIONS"
    var activeSubTab by remember { mutableStateOf("NOTES") }

    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val archivedNotes by viewModel.archivedNotes.collectAsStateWithLifecycle()
    val trashedNotes by viewModel.trashedNotes.collectAsStateWithLifecycle()
    val noteFolders by viewModel.noteFolders.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val calendarEvents by viewModel.calendarEvents.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()

    // Dialog & Sheet States
    var showCreateNoteDialog by remember { mutableStateOf(false) }
    var selectedNoteForEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var showCreateEventDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "WorldConnect Productivity",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Notes, Tâches, Agenda & IA Intégrée",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        floatingActionButton = {
            when (activeSubTab) {
                "NOTES" -> FloatingActionButton(
                    onClick = { showCreateNoteDialog = true },
                    modifier = Modifier.testTag("fab_create_note"),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.NoteAdd, contentDescription = "Nouvelle Note")
                }
                "TASKS" -> FloatingActionButton(
                    onClick = { showCreateTaskDialog = true },
                    modifier = Modifier.testTag("fab_create_task"),
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.AddTask, contentDescription = "Nouvelle Tâche")
                }
                "CALENDAR" -> FloatingActionButton(
                    onClick = { showCreateEventDialog = true },
                    modifier = Modifier.testTag("fab_create_event"),
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Icon(Icons.Default.Event, contentDescription = "Nouvel Événement")
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
                    "NOTES" -> 0
                    "TASKS" -> 1
                    "CALENDAR" -> 2
                    "VOICE_DRAW" -> 3
                    else -> 4
                },
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = activeSubTab == "NOTES",
                    onClick = { activeSubTab = "NOTES" },
                    text = { Text("Bloc-notes (${notes.size})") },
                    icon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.testTag("tab_notes")
                )
                Tab(
                    selected = activeSubTab == "TASKS",
                    onClick = { activeSubTab = "TASKS" },
                    text = { Text("Tâches (${tasks.count { it.status != "DONE" }})") },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                    modifier = Modifier.testTag("tab_tasks")
                )
                Tab(
                    selected = activeSubTab == "CALENDAR",
                    onClick = { activeSubTab = "CALENDAR" },
                    text = { Text("Agenda (${calendarEvents.size})") },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    modifier = Modifier.testTag("tab_calendar")
                )
                Tab(
                    selected = activeSubTab == "VOICE_DRAW",
                    onClick = { activeSubTab = "VOICE_DRAW" },
                    text = { Text("Vocales & Dessin") },
                    icon = { Icon(Icons.Default.Mic, contentDescription = null) },
                    modifier = Modifier.testTag("tab_voice_draw")
                )
                Tab(
                    selected = activeSubTab == "INTEGRATIONS",
                    onClick = { activeSubTab = "INTEGRATIONS" },
                    text = { Text("Hub WorldConnect") },
                    icon = { Icon(Icons.Default.Hub, contentDescription = null) },
                    modifier = Modifier.testTag("tab_integrations")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Area
            Box(modifier = Modifier.weight(1.0f)) {
                when (activeSubTab) {
                    "NOTES" -> NotesSubTabContent(
                        notes = notes,
                        archivedNotes = archivedNotes,
                        trashedNotes = trashedNotes,
                        folders = noteFolders,
                        onNoteClick = { selectedNoteForEdit = it },
                        onTogglePin = { viewModel.toggleNotePin(it.id) },
                        onToggleFavorite = { viewModel.toggleNoteFavorite(it.id) },
                        onArchive = { viewModel.archiveNote(it.id) },
                        onTrash = { viewModel.trashNote(it.id) },
                        onRestore = { viewModel.restoreNote(it.id) },
                        onDeletePermanently = { viewModel.deleteNotePermanently(it.id) },
                        onDuplicate = { viewModel.duplicateNote(it.id) },
                        onCreateFolderClick = { showCreateFolderDialog = true },
                        viewModel = viewModel
                    )
                    "TASKS" -> TasksSubTabContent(
                        tasks = tasks,
                        onToggleDone = { viewModel.toggleTaskDone(it.id) },
                        onDeleteTask = { viewModel.deleteTask(it.id) },
                        onQuickAddTask = { title, priority ->
                            viewModel.createTask(title = title, priority = priority)
                        },
                        contacts = contacts,
                        onShareToChat = { partnerId, summary ->
                            viewModel.shareTaskListToChat(partnerId, summary)
                            Toast.makeText(context, "Liste de tâches envoyée à la discussion!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    "CALENDAR" -> CalendarSubTabContent(
                        events = calendarEvents,
                        onDeleteEvent = { viewModel.deleteCalendarEvent(it.id) }
                    )
                    "VOICE_DRAW" -> VoiceAndDrawSubTabContent(
                        onSaveVoiceNote = { audioName, text, summary ->
                            viewModel.createNote(
                                title = "Note Vocale - $audioName",
                                content = text,
                                category = "Vocale",
                                voiceTranscription = text,
                                voiceSummary = summary
                            )
                            Toast.makeText(context, "Note vocale enregistrée et transcrite par IA!", Toast.LENGTH_SHORT).show()
                        },
                        onSaveDrawing = { drawingSvgPath ->
                            viewModel.createNote(
                                title = "Dessin Schéma Studio",
                                content = "[Schéma/Dessin incorporé]",
                                category = "Dessin",
                                drawingsJson = drawingSvgPath
                            )
                            Toast.makeText(context, "Dessin enregistré dans le Bloc-notes!", Toast.LENGTH_SHORT).show()
                        },
                        viewModel = viewModel
                    )
                    "INTEGRATIONS" -> IntegrationsHubContent(
                        viewModel = viewModel,
                        contacts = contacts
                    )
                }
            }
        }
    }

    // --- Dialogs ---
    if (showCreateNoteDialog) {
        CreateOrEditNoteDialog(
            note = null,
            folders = noteFolders,
            onDismiss = { showCreateNoteDialog = false },
            onSave = { title, content, category, colorHex, folderId, tagsJson, isLocked, passwordPin ->
                viewModel.createNote(
                    title = title,
                    content = content,
                    category = category,
                    colorHex = colorHex,
                    folderId = folderId,
                    tagsJson = tagsJson,
                    isLocked = isLocked,
                    passwordPin = passwordPin
                )
                showCreateNoteDialog = false
                Toast.makeText(context, "Note créée avec succès", Toast.LENGTH_SHORT).show()
            },
            viewModel = viewModel
        )
    }

    if (selectedNoteForEdit != null) {
        CreateOrEditNoteDialog(
            note = selectedNoteForEdit,
            folders = noteFolders,
            onDismiss = { selectedNoteForEdit = null },
            onSave = { title, content, category, colorHex, folderId, tagsJson, isLocked, passwordPin ->
                val current = selectedNoteForEdit!!
                viewModel.updateNote(
                    current.copy(
                        title = title,
                        content = content,
                        category = category,
                        colorHex = colorHex,
                        folderId = folderId,
                        tagsJson = tagsJson,
                        isLocked = isLocked,
                        passwordPin = passwordPin
                    )
                )
                selectedNoteForEdit = null
                Toast.makeText(context, "Note mise à jour", Toast.LENGTH_SHORT).show()
            },
            viewModel = viewModel
        )
    }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { name, colorHex ->
                viewModel.createFolder(name, colorHex)
                showCreateFolderDialog = false
            }
        )
    }

    if (showCreateTaskDialog) {
        CreateTaskDialog(
            onDismiss = { showCreateTaskDialog = false },
            onConfirm = { title, desc, priority, category, dueDate, dueTime, hasReminder ->
                viewModel.createTask(
                    title = title,
                    description = desc,
                    priority = priority,
                    category = category,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    hasReminder = hasReminder
                )
                showCreateTaskDialog = false
                Toast.makeText(context, "Tâche ajoutée", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showCreateEventDialog) {
        CreateEventDialog(
            onDismiss = { showCreateEventDialog = false },
            onConfirm = { title, desc, startTime, location, videoCallUrl, category ->
                viewModel.createCalendarEvent(
                    title = title,
                    description = desc,
                    startTime = startTime,
                    location = location,
                    videoCallUrl = videoCallUrl,
                    category = category
                )
                showCreateEventDialog = false
                Toast.makeText(context, "Événement planifié dans l'agenda", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// --- SUB-TAB 1: BLOC-NOTES ---
@Composable
fun NotesSubTabContent(
    notes: List<NoteEntity>,
    archivedNotes: List<NoteEntity>,
    trashedNotes: List<NoteEntity>,
    folders: List<NoteFolderEntity>,
    onNoteClick: (NoteEntity) -> Unit,
    onTogglePin: (NoteEntity) -> Unit,
    onToggleFavorite: (NoteEntity) -> Unit,
    onArchive: (NoteEntity) -> Unit,
    onTrash: (NoteEntity) -> Unit,
    onRestore: (NoteEntity) -> Unit,
    onDeletePermanently: (NoteEntity) -> Unit,
    onDuplicate: (NoteEntity) -> Unit,
    onCreateFolderClick: () -> Unit,
    viewModel: AppViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterCategory by remember { mutableStateOf("Toutes") } // Toutes, Épinglées, Favoris, Archivées, Corbeille
    var selectedFolderId by remember { mutableStateOf<Long?>(null) }

    val filteredNotes = remember(notes, archivedNotes, trashedNotes, searchQuery, selectedFilterCategory, selectedFolderId) {
        val targetList = when (selectedFilterCategory) {
            "Archivées" -> archivedNotes
            "Corbeille" -> trashedNotes
            else -> notes
        }

        targetList.filter { note ->
            val matchesFolder = (selectedFolderId == null || note.folderId == selectedFolderId)
            val matchesSearch = searchQuery.isBlank() ||
                    note.title.contains(searchQuery, ignoreCase = true) ||
                    note.content.contains(searchQuery, ignoreCase = true) ||
                    note.category.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedFilterCategory) {
                "Épinglées" -> note.isPinned
                "Favoris" -> note.isFavorite
                else -> true
            }

            matchesFolder && matchesSearch && matchesCategory
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        // Search & Filter Row
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher dans les notes, étiquettes, contenu...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = null) } }
            } else null,
            modifier = Modifier.fillMaxWidth().testTag("search_notes_input"),
            singleLine = true,
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Folders List
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedFolderId == null,
                    onClick = { selectedFolderId = null },
                    label = { Text("Tous dossiers") },
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
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "Nouveau dossier", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val categories = listOf("Toutes", "Épinglées", "Favoris", "Archivées", "Corbeille")
            items(categories) { cat ->
                SuggestionChip(
                    onClick = { selectedFilterCategory = cat },
                    label = { Text(cat) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (selectedFilterCategory == cat)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Notes Grid
        if (filteredNotes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.StickyNote2,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Aucune note trouvée",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Appuyez sur le bouton '+' pour ajouter votre première note.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredNotes, key = { it.id }) { note ->
                    NoteCardItem(
                        note = note,
                        onClick = { onNoteClick(note) },
                        onTogglePin = { onTogglePin(note) },
                        onToggleFavorite = { onToggleFavorite(note) },
                        onArchive = { onArchive(note) },
                        onTrash = { onTrash(note) },
                        onRestore = { onRestore(note) },
                        onDeletePermanently = { onDeletePermanently(note) },
                        onDuplicate = { onDuplicate(note) }
                    )
                }
            }
        }
    }
}

@Composable
fun NoteCardItem(
    note: NoteEntity,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onArchive: () -> Unit,
    onTrash: () -> Unit,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit,
    onDuplicate: () -> Unit
) {
    val cardBg = try {
        Color(android.graphics.Color.parseColor(note.colorHex)).copy(alpha = 0.15f)
    } catch (e: Exception) {
        MaterialTheme.colorScheme.surfaceVariant
    }

    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("note_card_${note.id}"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = note.category,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row {
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Épinglée",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (note.isFavorite) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favori",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu note", modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            if (!note.isTrashed) {
                                DropdownMenuItem(
                                    text = { Text(if (note.isPinned) "Désépingler" else "Épingler") },
                                    onClick = { onTogglePin(); showMenu = false },
                                    leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (note.isFavorite) "Retirer favori" else "Ajouter favori") },
                                    onClick = { onToggleFavorite(); showMenu = false },
                                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Dupliquer") },
                                    onClick = { onDuplicate(); showMenu = false },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (note.isArchived) "Désarchiver" else "Archiver") },
                                    onClick = { onArchive(); showMenu = false },
                                    leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Mettre à la corbeille") },
                                    onClick = { onTrash(); showMenu = false },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Restaurer") },
                                    onClick = { onRestore(); showMenu = false },
                                    leadingIcon = { Icon(Icons.Default.RestoreFromTrash, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Supprimer définitivement") },
                                    onClick = { onDeletePermanently(); showMenu = false },
                                    leadingIcon = { Icon(Icons.Default.DeleteForever, contentDescription = null) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (note.isLocked) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Note Verrouillée", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            } else {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!note.drawingsJson.isNullOrBlank()) {
                    Icon(Icons.Default.Brush, contentDescription = "Dessin", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                }
                if (!note.voiceTranscription.isNullOrBlank()) {
                    Icon(Icons.Default.Mic, contentDescription = "Note Vocale", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                }
                Spacer(modifier = Modifier.weight(1.0f))
                val dateStr = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(note.updatedAt))
                Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
    }
}

// --- SUB-TAB 2: GESTIONNAIRE DE TÂCHES ---
@Composable
fun TasksSubTabContent(
    tasks: List<TaskEntity>,
    onToggleDone: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onQuickAddTask: (String, String) -> Unit,
    contacts: List<com.example.data.local.UserProfileEntity>,
    onShareToChat: (String, String) -> Unit
) {
    var quickTaskTitle by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("MOYENNE") }
    var selectedCategoryFilter by remember { mutableStateOf("Toutes") }
    var showShareSheet by remember { mutableStateOf(false) }

    val filteredTasks = remember(tasks, selectedCategoryFilter) {
        if (selectedCategoryFilter == "Toutes") tasks
        else tasks.filter { it.category == selectedCategoryFilter }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        // Quick Add Task Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = quickTaskTitle,
                        onValueChange = { quickTaskTitle = it },
                        placeholder = { Text("Ajouter rapidement une tâche...") },
                        modifier = Modifier.weight(1.0f).testTag("quick_task_input"),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    IconButton(
                        onClick = {
                            if (quickTaskTitle.isNotBlank()) {
                                onQuickAddTask(quickTaskTitle, selectedPriority)
                                quickTaskTitle = ""
                            }
                        },
                        modifier = Modifier.testTag("btn_quick_add_task")
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Ajouter", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Priorité: ", style = MaterialTheme.typography.labelMedium, modifier = Modifier.align(Alignment.CenterVertically))
                    listOf("BASSE", "MOYENNE", "ELEVEE", "URGENTE").forEach { prio ->
                        FilterChip(
                            selected = selectedPriority == prio,
                            onClick = { selectedPriority = prio },
                            label = { Text(prio, fontSize = 10.sp) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Categories & Share
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1.0f)) {
                val cats = listOf("Toutes", "Général", "Travail", "Messagerie", "Projets")
                items(cats) { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat) }
                    )
                }
            }

            IconButton(onClick = { showShareSheet = true }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Partager la liste", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tasks List
        if (filteredTasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucune tâche disponible dans cette catégorie", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskCardItem(
                        task = task,
                        onToggleDone = { onToggleDone(task) },
                        onDelete = { onDeleteTask(task) }
                    )
                }
            }
        }
    }

    if (showShareSheet && contacts.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showShareSheet = false },
            title = { Text("Partager la liste de tâches") },
            text = {
                Column {
                    Text("Choisissez un contact WorldConnect:")
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(contacts) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val summary = tasks.filter { it.status != "DONE" }
                                            .joinToString("\n") { "• [${it.priority}] ${it.title}" }
                                        onShareToChat(contact.id, summary)
                                        showShareSheet = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(contact.name, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showShareSheet = false }) { Text("Fermer") }
            }
        )
    }
}

@Composable
fun TaskCardItem(
    task: TaskEntity,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit
) {
    val prioColor = when (task.priority) {
        "URGENTE" -> MaterialTheme.colorScheme.error
        "ELEVEE" -> Color(0xFFFF9800)
        "MOYENNE" -> Color(0xFF2196F3)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("task_card_${task.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.status == "DONE",
                onCheckedChange = { onToggleDone() },
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1.0f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (task.status == "DONE") androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = prioColor.copy(alpha = 0.2f)) {
                        Text(
                            text = task.priority,
                            style = MaterialTheme.typography.labelSmall,
                            color = prioColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (task.linkedWorldConnectType != null) {
                    Text(
                        text = "Liée à WorldConnect: ${task.linkedWorldConnectType}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// --- SUB-TAB 3: AGENDA & CALENDRIER ---
@Composable
fun CalendarSubTabContent(
    events: List<CalendarEventEntity>,
    onDeleteEvent: (CalendarEventEntity) -> Unit
) {
    var viewMode by remember { mutableStateOf("Mois") } // Jour, Semaine, Mois, Année

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        // View Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Jour", "Semaine", "Mois", "Année").forEach { mode ->
                FilterChip(
                    selected = viewMode == mode,
                    onClick = { viewMode = mode },
                    label = { Text(mode) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Visual Calendar Card Simulator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Juillet 2026", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    listOf("L", "M", "M", "J", "V", "S", "D").forEach { day ->
                        Text(day, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Événements planifiés", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

        Spacer(modifier = Modifier.height(6.dp))

        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun événement dans l'agenda", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(events, key = { it.id }) { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(event.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text(event.description, style = MaterialTheme.typography.bodySmall)
                                if (!event.videoCallUrl.isNullOrBlank()) {
                                    Text("🔗 ${event.videoCallUrl}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            IconButton(onClick = { onDeleteEvent(event) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-TAB 4: VOCALES & DESSIN CANVAS ---
@Composable
fun VoiceAndDrawSubTabContent(
    onSaveVoiceNote: (String, String, String) -> Unit,
    onSaveDrawing: (String) -> Unit,
    viewModel: AppViewModel
) {
    var mode by remember { mutableStateOf("VOICE") } // "VOICE" or "DRAW"
    val coroutineScope = rememberCoroutineScope()

    // Voice State
    var isRecording by remember { mutableStateOf(false) }
    var voiceTitle by remember { mutableStateOf("Recherche Stratégique WorldConnect") }
    var isAiTranscribing by remember { mutableStateOf(false) }

    // Drawing State
    val paths = remember { mutableStateListOf<List<Offset>>() }
    var currentPath = remember { mutableStateListOf<Offset>() }
    var drawColor by remember { mutableStateOf(Color.Red) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            FilterChip(
                selected = mode == "VOICE",
                onClick = { mode = "VOICE" },
                label = { Text("Notes Vocales & IA") },
                leadingIcon = { Icon(Icons.Default.Mic, contentDescription = null) }
            )
            Spacer(modifier = Modifier.width(12.dp))
            FilterChip(
                selected = mode == "DRAW",
                onClick = { mode = "DRAW" },
                label = { Text("Dessin & Stylet Studio") },
                leadingIcon = { Icon(Icons.Default.Brush, contentDescription = null) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (mode == "VOICE") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Studio Enregistreur Vocal", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(16.dp))

                    IconButton(
                        onClick = { isRecording = !isRecording },
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Enregistrer",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (isRecording) "Enregistrement en cours... 🔴" else "Appuyez pour enregistrer",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isAiTranscribing = true
                            coroutineScope.launch {
                                val (transcription, summary) = viewModel.transcribeAndSummarizeVoiceNoteWithAi(voiceTitle)
                                onSaveVoiceNote(voiceTitle, transcription, summary)
                                isAiTranscribing = false
                            }
                        },
                        enabled = !isAiTranscribing
                    ) {
                        if (isAiTranscribing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Transcription IA...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Générer Transcription & Résumé IA")
                        }
                    }
                }
            }
        } else {
            // DRAW CANVAS
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(Color.Red, Color.Blue, Color.Green, Color.Black).forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(
                                        width = if (drawColor == col) 2.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    )
                                    .clickable { drawColor = col }
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { paths.clear() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Effacer")
                        }
                        Button(onClick = { onSaveDrawing("SVG_PATH_DATA_SIMULATED") }) {
                            Text("Sauvegarder")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath.add(offset)
                                },
                                onDrag = { change, _ ->
                                    currentPath.add(change.position)
                                },
                                onDragEnd = {
                                    paths.add(currentPath.toList())
                                    currentPath.clear()
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        for (p in paths) {
                            for (i in 0 until p.size - 1) {
                                drawLine(color = drawColor, start = p[i], end = p[i + 1], strokeWidth = 5f)
                            }
                        }
                        for (i in 0 until currentPath.size - 1) {
                            drawLine(color = drawColor, start = currentPath[i], end = currentPath[i + 1], strokeWidth = 5f)
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-TAB 5: HUB INTÉGRATIONS WORLDCONNECT ---
@Composable
fun IntegrationsHubContent(
    viewModel: AppViewModel,
    contacts: List<com.example.data.local.UserProfileEntity>
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Passerelle Intégrée WorldConnect", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Convertissez instantanément vos discussions, publications et commandes en notes et tâches actionnables.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Actions d'intégration rapides", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.convertMessageToNote(1L)
                Toast.makeText(context, "Dernier message converti en Note!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Chat, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Convertir dernier message en Note")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.convertMessageToTask(1L, priority = "ELEVEE")
                Toast.makeText(context, "Message converti en Tâche prioritaire!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.AddTask, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Convertir message en Tâche prioritaire")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.convertPostToNote(101L)
                Toast.makeText(context, "Publication ajoutée au Bloc-notes", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Icon(Icons.Default.Public, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sauvegarder publication Sociale en Note")
        }
    }
}

// --- DIALOG COMPOSABLES ---
@Composable
fun CreateOrEditNoteDialog(
    note: NoteEntity?,
    folders: List<NoteFolderEntity>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Long?, String, Boolean, String?) -> Unit,
    viewModel: AppViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var category by remember { mutableStateOf(note?.category ?: "Général") }
    var colorHex by remember { mutableStateOf(note?.colorHex ?: "#3F51B5") }
    var folderId by remember { mutableStateOf(note?.folderId) }
    var isLocked by remember { mutableStateOf(note?.isLocked ?: false) }
    var isSummarizing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (note == null) "Nouvelle Note" else "Modifier Note") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre") },
                    modifier = Modifier.fillMaxWidth().testTag("input_note_title")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Contenu de la note...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp).testTag("input_note_content")
                )
                Spacer(modifier = Modifier.height(8.dp))

                // AI Tools Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            if (content.isNotBlank()) {
                                isSummarizing = true
                                coroutineScope.launch {
                                    val summary = viewModel.summarizeNoteWithAi(content)
                                    content += "\n\n--- RÉSUMÉ IA ---\n$summary"
                                    isSummarizing = false
                                }
                            }
                        },
                        enabled = !isSummarizing
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Résumé IA", fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Verrouiller: ", fontSize = 12.sp)
                        Switch(checked = isLocked, onCheckedChange = { isLocked = it })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, content, category, colorHex, folderId, "[]", isLocked, null) },
                modifier = Modifier.testTag("btn_save_note")
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau Dossier") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom du dossier") },
                modifier = Modifier.fillMaxWidth().testTag("input_folder_name")
            )
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name, "#2196F3") }) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Long?, String?, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MOYENNE") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle Tâche") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Intitulé de la tâche") },
                    modifier = Modifier.fillMaxWidth().testTag("input_task_title")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description / Détails") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onConfirm(title, desc, priority, "Général", null, null, false) }) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun CreateEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long, String, String?, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Planifier un Événement") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre de l'événement") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onConfirm(title, desc, System.currentTimeMillis() + 86400000, location, "https://meet.worldconnect.app/room-123", "Réunion") }) {
                Text("Planifier")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
