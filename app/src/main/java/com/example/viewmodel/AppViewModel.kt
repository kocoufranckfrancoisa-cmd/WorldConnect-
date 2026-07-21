package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AuditLogEntity
import com.example.data.local.CommentEntity
import com.example.data.local.MessageEntity
import com.example.data.local.PostEntity
import com.example.data.local.ProductEntity
import com.example.data.local.UserProfileEntity
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application, private val repository: AppRepository) : AndroidViewModel(application) {

    init {
        viewModelScope.launch {
            repository.seedInitialCloudDataIfEmpty()
        }
    }

    // --- System UI / Theme ---
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // --- User Profiles ---
    val currentUser: StateFlow<UserProfileEntity?> = repository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val contacts: StateFlow<List<UserProfileEntity>> = repository.contactsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Social Feed ---
    val posts: StateFlow<List<PostEntity>> = repository.feedPostsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reportedPosts: StateFlow<List<PostEntity>> = repository.reportedPostsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Marketplace ---
    val products: StateFlow<List<ProductEntity>> = repository.productsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Audit Logs ---
    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- WorldConnect Games State Flows ---
    val gameRooms: StateFlow<List<com.example.data.local.GameRoomEntity>> = repository.gameRoomsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentMatches: StateFlow<List<com.example.data.local.GameMatchEntity>> = repository.recentMatchesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tournaments: StateFlow<List<com.example.data.local.TournamentEntity>> = repository.tournamentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gameStats: StateFlow<List<com.example.data.local.GameStatisticEntity>> = repository.gameStatsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<com.example.data.local.AchievementEntity>> = repository.achievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Chat & Conversation ---
    private val _activeChatPartner = MutableStateFlow<UserProfileEntity?>(null)
    val activeChatPartner: StateFlow<UserProfileEntity?> = _activeChatPartner.asStateFlow()

    // --- Rich Communication Module Flows ---
    val stickerPacks: StateFlow<List<com.example.data.local.StickerPackEntity>> = repository.stickerPacksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStickers: StateFlow<List<com.example.data.local.StickerEntity>> = repository.allStickersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteMessages: StateFlow<List<MessageEntity>> = repository.favoriteMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduledMessages: StateFlow<List<com.example.data.local.ScheduledMessageEntity>> = repository.scheduledMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chat Pinned Messages
    val activeChatPinnedMessages: StateFlow<List<MessageEntity>> = _activeChatPartner
        .flatMapLatest { partner ->
            if (partner == null) flowOf(emptyList())
            else repository.getPinnedMessages(partner.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chat Draft
    val activeChatDraft: StateFlow<com.example.data.local.MessageDraftEntity?> = _activeChatPartner
        .flatMapLatest { partner ->
            if (partner == null) flowOf(null)
            else repository.getDraft(partner.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Reply Target Message
    private val _activeReplyTo = MutableStateFlow<MessageEntity?>(null)
    val activeReplyTo: StateFlow<MessageEntity?> = _activeReplyTo.asStateFlow()

    // Message Search Query and Results
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<MessageEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else repository.searchMessages(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setReplyTo(message: MessageEntity?) {
        _activeReplyTo.value = message
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveDraft(partnerId: String, text: String) {
        viewModelScope.launch {
            repository.saveDraft(partnerId, text, _activeReplyTo.value?.id)
        }
    }

    fun scheduleMessage(partnerId: String, text: String, scheduledTimestamp: Long, mediaType: String = "TEXT", mediaUrl: String? = null) {
        viewModelScope.launch {
            repository.scheduleMessage(partnerId, text, scheduledTimestamp, mediaType, mediaUrl)
        }
    }

    fun cancelScheduledMessage(id: Long) {
        viewModelScope.launch {
            repository.cancelScheduledMessage(id)
        }
    }

    fun togglePinMessage(msg: MessageEntity) {
        viewModelScope.launch {
            repository.togglePinMessage(msg)
        }
    }

    fun toggleFavoriteMessage(msg: MessageEntity) {
        viewModelScope.launch {
            repository.toggleFavoriteMessage(msg)
        }
    }

    fun toggleReaction(msg: MessageEntity, emoji: String) {
        viewModelScope.launch {
            repository.toggleReaction(msg, emoji)
        }
    }

    fun votePoll(msg: MessageEntity, optionIndex: Int) {
        viewModelScope.launch {
            repository.votePoll(msg, optionIndex)
        }
    }

    fun createCustomSticker(imageUrl: String, emoji: String) {
        viewModelScope.launch {
            repository.createCustomSticker(imageUrl, emoji)
        }
    }

    fun forwardMessages(targetPartnerIds: List<String>, messages: List<MessageEntity>) {
        viewModelScope.launch {
            targetPartnerIds.forEach { partnerId ->
                messages.forEach { msg ->
                    repository.sendMessage(
                        partnerId = partnerId,
                        text = if (msg.mediaType == "TEXT") "➡️ Transfert : ${msg.text}" else "➡️ Transfert de message",
                        imageUrl = msg.imageUrl,
                        isVoice = msg.isVoice,
                        voiceDuration = msg.voiceDurationSec,
                        mediaType = msg.mediaType,
                        mediaUrl = msg.mediaUrl,
                        documentName = msg.documentName,
                        documentSize = msg.documentSize,
                        locationLat = msg.locationLat,
                        locationLng = msg.locationLng,
                        locationAddress = msg.locationAddress,
                        contactName = msg.contactName,
                        contactPhone = msg.contactPhone,
                        contactAvatar = msg.contactAvatar,
                        pollQuestion = msg.pollQuestion,
                        pollOptionsJson = msg.pollOptionsJson,
                        albumUrlsJson = msg.albumUrlsJson
                    )
                }
            }
        }
    }

    fun sendRichMessage(
        partnerId: String,
        text: String,
        mediaType: String = "TEXT",
        mediaUrl: String? = null,
        documentName: String? = null,
        documentSize: String? = null,
        locationLat: Double? = null,
        locationLng: Double? = null,
        locationAddress: String? = null,
        isLiveLocation: Boolean = false,
        liveLocationDurationMinutes: Int? = null,
        contactName: String? = null,
        contactPhone: String? = null,
        contactAvatar: String? = null,
        pollQuestion: String? = null,
        pollOptionsJson: String? = null,
        pollIsAnonymous: Boolean = false,
        albumUrlsJson: String? = null
    ) {
        viewModelScope.launch {
            val replyMsg = _activeReplyTo.value
            repository.sendMessage(
                partnerId = partnerId,
                text = text,
                replyToMessageId = replyMsg?.id,
                replyToText = replyMsg?.text,
                mediaType = mediaType,
                mediaUrl = mediaUrl,
                documentName = documentName,
                documentSize = documentSize,
                locationLat = locationLat,
                locationLng = locationLng,
                locationAddress = locationAddress,
                isLiveLocation = isLiveLocation,
                liveLocationDurationMinutes = liveLocationDurationMinutes,
                contactName = contactName,
                contactPhone = contactPhone,
                contactAvatar = contactAvatar,
                pollQuestion = pollQuestion,
                pollOptionsJson = pollOptionsJson,
                pollIsAnonymous = pollIsAnonymous,
                albumUrlsJson = albumUrlsJson
            )
            _activeReplyTo.value = null
            _replySuggestions.value = emptyList()
        }
    }

    fun aiRephraseText(text: String, mode: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.aiRephraseAndGrammar(text, mode)
            onResult(result)
        }
    }

    // Dynamically retrieve messages for the selected chat partner
    val activeChatMessages: StateFlow<List<MessageEntity>> = _activeChatPartner
        .flatMapLatest { partner ->
            if (partner == null) flowOf(emptyList())
            else repository.getMessages(partner.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Smart reply suggestions
    private val _replySuggestions = MutableStateFlow<List<String>>(emptyList())
    val replySuggestions: StateFlow<List<String>> = _replySuggestions.asStateFlow()

    // AI Chat Summaries
    private val _chatSummary = MutableStateFlow<String?>(null)
    val chatSummary: StateFlow<String?> = _chatSummary.asStateFlow()

    // Translation Cache (messageId -> Translated Text)
    private val _translatedMessages = MutableStateFlow<Map<Long, String>>(emptyMap())
    val translatedMessages: StateFlow<Map<Long, String>> = _translatedMessages.asStateFlow()

    // --- Calling Simulation States ---
    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    // --- Active Post Comments ---
    private val _activePostComments = MutableStateFlow<List<CommentEntity>>(emptyList())
    val activePostComments: StateFlow<List<CommentEntity>> = _activePostComments.asStateFlow()

    private val _activeCommentPostId = MutableStateFlow<Long?>(null)
    val activeCommentPostId: StateFlow<Long?> = _activeCommentPostId.asStateFlow()

    // --- Spam Detector ---
    private val _spamCheckResult = MutableStateFlow<String?>(null)
    val spamCheckResult: StateFlow<String?> = _spamCheckResult.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getOrCreateCurrentUser()
            repository.checkAndPrepopulateData()
        }
    }

    // --- Authentication Actions ---
    fun registerAndLogin(name: String, email: String, bio: String, avatarUrl: String) {
        viewModelScope.launch {
            repository.loginAsUser(name, email, bio, avatarUrl)
        }
    }

    fun updateProfile(name: String, bio: String, avatarUrl: String, coverUrl: String) {
        viewModelScope.launch {
            repository.updateCurrentUser(bio, name, avatarUrl, coverUrl)
        }
    }

    fun toggleBlockContact(userId: String) {
        viewModelScope.launch {
            repository.toggleBlockUser(userId)
        }
    }

    // --- Post Actions ---
    fun createPost(text: String, imageUrl: String? = null, category: String = "General") {
        viewModelScope.launch {
            repository.createPost(text, imageUrl, category)
        }
    }

    fun toggleLikePost(post: PostEntity) {
        viewModelScope.launch {
            repository.toggleLikePost(post)
        }
    }

    fun reportPost(postId: Long, reason: String) {
        viewModelScope.launch {
            repository.reportPost(postId, reason)
        }
    }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            repository.deletePost(postId)
        }
    }

    fun loadComments(postId: Long) {
        _activeCommentPostId.value = postId
        viewModelScope.launch {
            repository.getComments(postId).collect {
                _activePostComments.value = it
            }
        }
    }

    fun addComment(postId: Long, text: String) {
        viewModelScope.launch {
            repository.addComment(postId, text)
        }
    }

    // --- Message Actions ---
    fun selectChatPartner(partner: UserProfileEntity?) {
        _activeChatPartner.value = partner
        _chatSummary.value = null
        _replySuggestions.value = emptyList()
        
        if (partner != null) {
            viewModelScope.launch {
                // Generate suggestions
                val currentMessages = repository.getMessages(partner.id).firstOrNull() ?: emptyList()
                if (currentMessages.isNotEmpty() && partner.id != "user_maya") {
                    _replySuggestions.value = repository.getSmartReplySuggestions(currentMessages)
                }
            }
        }
    }

    fun sendMessage(partnerId: String, text: String, imageUrl: String? = null, isVoice: Boolean = false, voiceDuration: Int = 0) {
        viewModelScope.launch {
            repository.sendMessage(partnerId, text, imageUrl, isVoice, voiceDuration)
            // Clear suggestions while waiting or recalculate
            _replySuggestions.value = emptyList()
            // Regenerate reply suggestions for current chat
            val updatedMessages = repository.getMessages(partnerId).firstOrNull() ?: emptyList()
            if (updatedMessages.isNotEmpty() && partnerId != "user_maya") {
                _replySuggestions.value = repository.getSmartReplySuggestions(updatedMessages)
            }
        }
    }

    // --- Marketplace Actions ---
    fun addProduct(name: String, description: String, price: Double, category: String, imageUrl: String) {
        viewModelScope.launch {
            repository.addProduct(name, description, price, category, imageUrl)
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            repository.removeProduct(productId)
        }
    }

    // --- AI Intelligence Actions ---
    fun translateMessage(messageId: Long, text: String, targetLang: String) {
        viewModelScope.launch {
            val translation = repository.translateMessage(text, targetLang)
            val currentMap = _translatedMessages.value.toMutableMap()
            currentMap[messageId] = translation
            _translatedMessages.value = currentMap
        }
    }

    fun clearTranslation(messageId: Long) {
        val currentMap = _translatedMessages.value.toMutableMap()
        currentMap.remove(messageId)
        _translatedMessages.value = currentMap
    }

    fun summarizeChat(partnerName: String, messages: List<MessageEntity>) {
        viewModelScope.launch {
            _chatSummary.value = "Generating AI Summary..."
            val summary = repository.summarizeConversation(partnerName, messages)
            _chatSummary.value = summary
        }
    }

    fun runSpamCheck(postText: String) {
        viewModelScope.launch {
            _spamCheckResult.value = "Analyzing post..."
            val result = repository.analyzePostSpamAndMod(postText)
            _spamCheckResult.value = result
        }
    }

    fun clearSpamCheck() {
        _spamCheckResult.value = null
    }

    // --- Call Actions ---
    fun startCall(partner: UserProfileEntity, isVideo: Boolean) {
        _callState.value = CallState.Dialing(partner, isVideo)
        // Transition to active after 2.5 seconds
        viewModelScope.launch {
            kotlinx.coroutines.delay(2500)
            if (_callState.value is CallState.Dialing) {
                _callState.value = CallState.Active(partner, isVideo, System.currentTimeMillis())
            }
        }
    }

    fun endCall() {
        _callState.value = CallState.Idle
    }

    // --- WorldConnect Games Actions ---
    fun createGameRoom(title: String, gameType: String, isPrivate: Boolean, accessCode: String?, isRanked: Boolean, region: String) {
        viewModelScope.launch {
            repository.createGameRoom(title, gameType, isPrivate, accessCode, isRanked, region)
        }
    }

    fun deleteGameRoom(roomId: Long) {
        viewModelScope.launch {
            repository.deleteGameRoom(roomId)
        }
    }

    fun recordMatch(gameType: String, opponentName: String, winnerName: String, movesHistory: String, isAiOpponent: Boolean) {
        viewModelScope.launch {
            repository.recordGameMatch(gameType, opponentName, winnerName, movesHistory, isAiOpponent)
        }
    }

    fun createTournament(title: String, gameType: String, prizePool: String, maxPlayers: Int) {
        viewModelScope.launch {
            repository.createTournament(title, gameType, prizePool, maxPlayers)
        }
    }

    suspend fun fetchAiMove(gameType: String, boardState: String, difficulty: String): String {
        return repository.getAiGameMove(gameType, boardState, difficulty)
    }

    // --- PRODUCTIVITY MODULE STATE FLOWS & ACTIONS ---

    val notes: StateFlow<List<com.example.data.local.NoteEntity>> = repository.allNotesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedNotes: StateFlow<List<com.example.data.local.NoteEntity>> = repository.archivedNotesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashedNotes: StateFlow<List<com.example.data.local.NoteEntity>> = repository.trashedNotesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val noteFolders: StateFlow<List<com.example.data.local.NoteFolderEntity>> = repository.allFoldersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<com.example.data.local.TaskEntity>> = repository.allTasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calendarEvents: StateFlow<List<com.example.data.local.CalendarEventEntity>> = repository.allCalendarEventsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productivityReminders: StateFlow<List<com.example.data.local.ProductivityReminderEntity>> = repository.allRemindersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createNote(
        title: String,
        content: String,
        folderId: Long? = null,
        category: String = "Général",
        colorHex: String = "#3F51B5",
        tagsJson: String = "[]",
        drawingsJson: String? = null,
        voiceNoteUrl: String? = null,
        voiceTranscription: String? = null,
        voiceSummary: String? = null,
        attachmentsJson: String? = null,
        isLocked: Boolean = false,
        passwordPin: String? = null
    ) {
        viewModelScope.launch {
            val note = com.example.data.local.NoteEntity(
                title = title.ifBlank { "Sans titre" },
                content = content,
                folderId = folderId,
                category = category,
                colorHex = colorHex,
                tagsJson = tagsJson,
                drawingsJson = drawingsJson,
                voiceNoteUrl = voiceNoteUrl,
                voiceTranscription = voiceTranscription,
                voiceSummary = voiceSummary,
                attachmentsJson = attachmentsJson,
                isLocked = isLocked,
                passwordPin = passwordPin
            )
            repository.insertNote(note)
        }
    }

    fun updateNote(note: com.example.data.local.NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun trashNote(noteId: Long) {
        viewModelScope.launch {
            repository.trashNote(noteId)
        }
    }

    fun restoreNote(noteId: Long) {
        viewModelScope.launch {
            repository.restoreNote(noteId)
        }
    }

    fun archiveNote(noteId: Long) {
        viewModelScope.launch {
            repository.archiveNote(noteId)
        }
    }

    fun toggleNotePin(noteId: Long) {
        viewModelScope.launch {
            repository.toggleNotePin(noteId)
        }
    }

    fun toggleNoteFavorite(noteId: Long) {
        viewModelScope.launch {
            repository.toggleNoteFavorite(noteId)
        }
    }

    fun duplicateNote(noteId: Long) {
        viewModelScope.launch {
            repository.duplicateNote(noteId)
        }
    }

    fun deleteNotePermanently(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNotePermanently(noteId)
        }
    }

    fun createFolder(name: String, colorHex: String = "#2196F3") {
        viewModelScope.launch {
            repository.insertFolder(name, colorHex)
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            repository.deleteFolder(folderId)
        }
    }

    fun createTask(
        title: String,
        description: String = "",
        priority: String = "MOYENNE",
        category: String = "Général",
        dueDate: Long? = null,
        dueTime: String? = null,
        hasReminder: Boolean = false,
        repeatFrequency: String = "NONE",
        subtasksJson: String? = null
    ) {
        viewModelScope.launch {
            val task = com.example.data.local.TaskEntity(
                title = title.ifBlank { "Nouvelle Tâche" },
                description = description,
                priority = priority,
                category = category,
                dueDate = dueDate,
                dueTime = dueTime,
                hasReminder = hasReminder,
                repeatFrequency = repeatFrequency,
                subtasksJson = subtasksJson
            )
            repository.insertTask(task)
        }
    }

    fun toggleTaskDone(taskId: Long) {
        viewModelScope.launch {
            repository.toggleTaskDone(taskId)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun createCalendarEvent(
        title: String,
        description: String = "",
        startTime: Long = System.currentTimeMillis(),
        endTime: Long = System.currentTimeMillis() + 3600000,
        isAllDay: Boolean = false,
        location: String = "",
        videoCallUrl: String? = null,
        colorHex: String = "#4CAF50",
        category: String = "Réunion",
        participantsJson: String = "[]"
    ) {
        viewModelScope.launch {
            val event = com.example.data.local.CalendarEventEntity(
                title = title.ifBlank { "Nouvel Événement" },
                description = description,
                startTime = startTime,
                endTime = endTime,
                isAllDay = isAllDay,
                location = location,
                videoCallUrl = videoCallUrl,
                colorHex = colorHex,
                category = category,
                participantsJson = participantsJson
            )
            repository.insertCalendarEvent(event)
        }
    }

    fun deleteCalendarEvent(eventId: Long) {
        viewModelScope.launch {
            repository.deleteCalendarEvent(eventId)
        }
    }

    fun createReminder(title: String, scheduledTime: Long, targetType: String, targetId: Long) {
        viewModelScope.launch {
            repository.insertReminder(title, scheduledTime, targetType, targetId)
        }
    }

    fun deleteReminder(reminderId: Long) {
        viewModelScope.launch {
            repository.deleteReminder(reminderId)
        }
    }

    // AI methods
    suspend fun summarizeNoteWithAi(content: String): String = repository.summarizeNoteWithAi(content)
    suspend fun extractTasksFromNoteWithAi(content: String): List<String> = repository.extractTasksFromNoteWithAi(content)
    suspend fun createEventFromNoteWithAi(noteTitle: String, noteContent: String): com.example.data.local.CalendarEventEntity =
        repository.createEventFromNoteWithAi(noteTitle, noteContent)
    suspend fun autoCategorizeNoteWithAi(title: String, content: String): String = repository.autoCategorizeNoteWithAi(title, content)
    suspend fun transcribeAndSummarizeVoiceNoteWithAi(audioName: String): Pair<String, String> = repository.transcribeAndSummarizeVoiceNoteWithAi(audioName)

    // Integrations
    fun convertMessageToTask(messageId: Long, category: String = "Messagerie", priority: String = "MOYENNE") {
        viewModelScope.launch {
            repository.convertMessageToTask(messageId, category, priority)
        }
    }

    fun convertMessageToNote(messageId: Long, folderId: Long? = null) {
        viewModelScope.launch {
            repository.convertMessageToNote(messageId, folderId)
        }
    }

    fun shareNoteToChat(noteId: Long, chatPartnerId: String) {
        viewModelScope.launch {
            repository.shareNoteToChat(noteId, chatPartnerId)
        }
    }

    fun shareTaskListToChat(chatPartnerId: String, summary: String) {
        viewModelScope.launch {
            repository.shareTaskListToChat(chatPartnerId, summary)
        }
    }

    fun convertPostToNote(postId: Long) {
        viewModelScope.launch {
            repository.convertPostToNote(postId)
        }
    }

    fun convertProductToNote(productId: Long) {
        viewModelScope.launch {
            repository.convertProductToNote(productId)
        }
    }

    // --- CLOUD STORAGE MODULE STATE FLOWS & ACTIONS ---

    val cloudFiles: StateFlow<List<com.example.data.local.CloudFileEntity>> = repository.allCloudFilesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashedCloudFiles: StateFlow<List<com.example.data.local.CloudFileEntity>> = repository.trashedCloudFilesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteCloudFiles: StateFlow<List<com.example.data.local.CloudFileEntity>> = repository.favoriteCloudFilesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cloudFolders: StateFlow<List<com.example.data.local.CloudFolderEntity>> = repository.allCloudFoldersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sharedCloudFiles: StateFlow<List<com.example.data.local.SharedCloudFileEntity>> = repository.allSharedCloudFilesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userQuota: StateFlow<com.example.data.local.UserQuotaEntity?> = repository.userQuotaFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val storagePlans: StateFlow<List<com.example.data.local.StoragePlanEntity>> = repository.allStoragePlansFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun uploadCloudFile(
        name: String,
        fileType: String,
        extension: String,
        sizeBytes: Long,
        folderId: Long? = null,
        linkedModule: String? = null,
        linkedEntityId: String? = null
    ) {
        viewModelScope.launch {
            val file = com.example.data.local.CloudFileEntity(
                name = name.ifBlank { "Fichier_Sans_Titre" },
                fileType = fileType,
                extension = extension,
                sizeBytes = sizeBytes,
                folderId = folderId,
                storageUrl = "https://cloud.worldconnect.app/uploads/${System.currentTimeMillis()}_$name",
                linkedModule = linkedModule,
                linkedEntityId = linkedEntityId
            )
            repository.insertCloudFile(file)
        }
    }

    fun updateCloudFile(file: com.example.data.local.CloudFileEntity) {
        viewModelScope.launch {
            repository.updateCloudFile(file)
        }
    }

    fun trashCloudFile(fileId: Long) {
        viewModelScope.launch {
            repository.trashCloudFile(fileId)
        }
    }

    fun restoreCloudFile(fileId: Long) {
        viewModelScope.launch {
            repository.restoreCloudFile(fileId)
        }
    }

    fun deleteCloudFilePermanently(fileId: Long) {
        viewModelScope.launch {
            repository.deleteCloudFilePermanently(fileId)
        }
    }

    fun toggleCloudFileFavorite(fileId: Long) {
        viewModelScope.launch {
            repository.toggleCloudFileFavorite(fileId)
        }
    }

    fun toggleCloudFileOffline(fileId: Long) {
        viewModelScope.launch {
            repository.toggleCloudFileOffline(fileId)
        }
    }

    fun createCloudFolder(name: String, parentFolderId: Long? = null, colorHex: String = "#2196F3") {
        viewModelScope.launch {
            repository.createCloudFolder(name, parentFolderId, colorHex)
        }
    }

    fun deleteCloudFolder(folderId: Long) {
        viewModelScope.launch {
            repository.deleteCloudFolder(folderId)
        }
    }

    fun shareCloudFile(
        fileId: Long,
        sharedWithUserName: String,
        permissionLevel: String = "READ",
        isPasswordProtected: Boolean = false,
        expiresDays: Int = 30
    ) {
        viewModelScope.launch {
            repository.shareFileWithUser(fileId, sharedWithUserName, permissionLevel, isPasswordProtected, expiresDays)
        }
    }

    fun upgradeStoragePlan(planId: String) {
        viewModelScope.launch {
            repository.upgradeStoragePlan("usr_current", planId)
        }
    }

    fun updateBackupSettings(autoPhotos: Boolean, autoVideos: Boolean, autoDocs: Boolean) {
        viewModelScope.launch {
            repository.updateBackupSettings("usr_current", autoPhotos, autoVideos, autoDocs)
        }
    }

    suspend fun analyzeCloudFileWithAi(fileName: String, fileType: String): Pair<String, String> {
        return repository.performOcrAndSummarizeFileWithAi(fileName, fileType)
    }
}



// Sealed class representing active call simulation
sealed class CallState {
    object Idle : CallState()
    data class Dialing(val partner: UserProfileEntity, val isVideo: Boolean) : CallState()
    data class Active(val partner: UserProfileEntity, val isVideo: Boolean, val startTime: Long) : CallState()
}

class AppViewModelFactory(
    private val application: Application,
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
