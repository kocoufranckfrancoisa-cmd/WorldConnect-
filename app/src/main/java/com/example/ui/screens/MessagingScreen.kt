package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.MessageEntity
import com.example.data.local.UserProfileEntity
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingScreen(viewModel: AppViewModel) {
    val contacts by viewModel.contacts.collectAsState()
    val activePartner by viewModel.activeChatPartner.collectAsState()
    val chatMessages by viewModel.activeChatMessages.collectAsState()
    val replySuggestions by viewModel.replySuggestions.collectAsState()
    val chatSummary by viewModel.chatSummary.collectAsState()
    val translations by viewModel.translatedMessages.collectAsState()
    val stickerPacks by viewModel.stickerPacks.collectAsState()
    val allStickers by viewModel.allStickers.collectAsState()
    val pinnedMessages by viewModel.activeChatPinnedMessages.collectAsState()
    val activeDraft by viewModel.activeChatDraft.collectAsState()
    val activeReplyTo by viewModel.activeReplyTo.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var messageInput by remember { mutableStateOf("") }

    // Dialog & Sheet States
    var showStickerPicker by remember { mutableStateOf(false) }
    var showStickerCreator by remember { mutableStateOf(false) }
    var showGifPicker by remember { mutableStateOf(false) }
    var showPollCreator by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showForwardDialog by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showSearchOverlay by remember { mutableStateOf(false) }
    var messageToForward by remember { mutableStateOf<MessageEntity?>(null) }

    // Synchronize draft when changing partner
    LaunchedEffect(activePartner, activeDraft) {
        if (activeDraft != null && messageInput.isEmpty()) {
            messageInput = activeDraft?.text ?: ""
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        if (activePartner == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Messagerie WorldConnect",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Communication enrichie : Stickers, GIFs, Sondages, Fichiers, IA",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(contacts) { contact ->
                        ContactItemRow(
                            contact = contact,
                            onClick = { viewModel.selectChatPartner(contact) }
                        )
                    }
                }
            }
        } else {
            // Chat detail view
            val partner = activePartner!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Chat Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1.0f)
                    ) {
                        IconButton(onClick = {
                            viewModel.saveDraft(partner.id, messageInput)
                            viewModel.selectChatPartner(null)
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                        }

                        AsyncImage(
                            model = partner.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = partner.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "En ligne • Synchronisé",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    // Header Action Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(onClick = { showSearchOverlay = !showSearchOverlay }) {
                            Icon(Icons.Default.Search, contentDescription = "Rechercher", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.startCall(partner, isVideo = false) }) {
                            Icon(Icons.Default.Call, contentDescription = "Appel audio", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.startCall(partner, isVideo = true) }) {
                            Icon(Icons.Default.VideoCall, contentDescription = "Appel vidéo", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(
                            onClick = { viewModel.summarizeChat(partner.name, chatMessages) },
                            modifier = Modifier.testTag("ai_summary_chat")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Résumé IA", tint = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }

                // Pinned Messages Banner Bar
                if (pinnedMessages.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.0f)) {
                                Icon(Icons.Default.PushPin, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Message Épinglé : ${pinnedMessages.first().text.take(30)}...",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "(${pinnedMessages.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Search Overlay Bar
                AnimatedVisibility(visible = showSearchOverlay) {
                    Surface(
                        tonalElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Rechercher messages, fichiers, liens...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        viewModel.setSearchQuery("")
                                        showSearchOverlay = false
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // AI Summary Card overlay
                AnimatedVisibility(
                    visible = chatSummary != null,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Résumé Professionnel IA",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                IconButton(onClick = { viewModel.selectChatPartner(partner) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Fermer", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = chatSummary ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Messages stream column
                val displayMessages = if (searchQuery.isNotBlank()) searchResults else chatMessages

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    reverseLayout = false
                ) {
                    items(displayMessages) { message ->
                        val isMe = message.senderId == "user_me"
                        val translation = translations[message.id]

                        RichMessageBubbleItem(
                            message = message,
                            isMe = isMe,
                            translation = translation,
                            onReply = { viewModel.setReplyTo(message) },
                            onReaction = { emoji -> viewModel.toggleReaction(message, emoji) },
                            onPin = { viewModel.togglePinMessage(message) },
                            onFavorite = { viewModel.toggleFavoriteMessage(message) },
                            onForward = {
                                messageToForward = message
                                showForwardDialog = true
                            },
                            onVotePoll = { optIdx -> viewModel.votePoll(message, optIdx) },
                            onTranslate = { targetLang ->
                                viewModel.translateMessage(message.id, message.text, targetLang)
                            },
                            onClearTranslation = {
                                viewModel.clearTranslation(message.id)
                            },
                            onConvertToTask = {
                                viewModel.convertMessageToTask(message.id)
                            },
                            onConvertToNote = {
                                viewModel.convertMessageToNote(message.id)
                            }
                        )
                    }
                }

                // Reply Preview Container (If replying)
                activeReplyTo?.let { replyMsg ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(
                                    text = "Réponse à ${replyMsg.senderName}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    text = replyMsg.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { viewModel.setReplyTo(null) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Annuler la réponse", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Typing suggestions row (Gemini Smart Replies)
                if (replySuggestions.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        replySuggestions.forEach { suggestion ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        viewModel.sendRichMessage(partner.id, suggestion)
                                        messageInput = ""
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Rich Composer bar
                Surface(
                    tonalElevation = 3.dp,
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Attachment menu trigger
                            IconButton(onClick = { showAttachmentMenu = !showAttachmentMenu }) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Menu Pièces Jointes", tint = MaterialTheme.colorScheme.primary)
                            }

                            // Stickers picker trigger
                            IconButton(onClick = { showStickerPicker = true }) {
                                Icon(Icons.Default.StickyNote2, contentDescription = "Stickers", tint = MaterialTheme.colorScheme.primary)
                            }

                            // GIFs trigger
                            IconButton(onClick = { showGifPicker = true }) {
                                Icon(Icons.Default.Gif, contentDescription = "GIFs", tint = MaterialTheme.colorScheme.primary)
                            }

                            OutlinedTextField(
                                value = messageInput,
                                onValueChange = {
                                    messageInput = it
                                    viewModel.saveDraft(partner.id, it)
                                },
                                placeholder = { Text("Écrire un message enrichi...") },
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1.0f)
                                    .testTag("chat_input_field"),
                                maxLines = 4
                            )

                            // AI Rephrase Button
                            if (messageInput.isNotEmpty()) {
                                IconButton(onClick = {
                                    viewModel.aiRephraseText(messageInput, "PROFESSIONAL") { rephrased ->
                                        messageInput = rephrased
                                    }
                                }) {
                                    Icon(Icons.Default.AutoFixHigh, contentDescription = "Reformuler IA", tint = MaterialTheme.colorScheme.tertiary)
                                }
                            }

                            IconButton(
                                onClick = {
                                    if (messageInput.isNotEmpty()) {
                                        viewModel.sendRichMessage(partner.id, messageInput)
                                        messageInput = ""
                                    }
                                },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.testTag("send_chat_button")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Envoyer")
                            }
                        }

                        // Attachment Quick Bar Collapsible Menu
                        AnimatedVisibility(visible = showAttachmentMenu) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                AttachmentMenuItem(Icons.Default.Poll, "Sondage", MaterialTheme.colorScheme.primary) {
                                    showAttachmentMenu = false
                                    showPollCreator = true
                                }
                                AttachmentMenuItem(Icons.Default.LocationOn, "Position", Color(0xFFE65100)) {
                                    showAttachmentMenu = false
                                    showLocationDialog = true
                                }
                                AttachmentMenuItem(Icons.Default.ContactPhone, "Contact", Color(0xFF2E7D32)) {
                                    showAttachmentMenu = false
                                    viewModel.sendRichMessage(
                                        partnerId = partner.id,
                                        text = "Fiche Contact",
                                        mediaType = "CONTACT",
                                        contactName = "Sophie Bernard",
                                        contactPhone = "+33 6 12 34 56 78",
                                        contactAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80"
                                    )
                                }
                                AttachmentMenuItem(Icons.Default.InsertDriveFile, "Document", Color(0xFF1565C0)) {
                                    showAttachmentMenu = false
                                    viewModel.sendRichMessage(
                                        partnerId = partner.id,
                                        text = "Document WorldConnect.pdf",
                                        mediaType = "DOCUMENT",
                                        documentName = "Rapport_Projet_WorldConnect_2026.pdf",
                                        documentSize = "2.4 MB"
                                    )
                                }
                                AttachmentMenuItem(Icons.Default.Collections, "Album", Color(0xFFD81B60)) {
                                    showAttachmentMenu = false
                                    viewModel.sendRichMessage(
                                        partnerId = partner.id,
                                        text = "Album Photos",
                                        mediaType = "ALBUM",
                                        albumUrlsJson = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e|https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba|https://images.unsplash.com/photo-1563089145-599997674d42"
                                    )
                                }
                                AttachmentMenuItem(Icons.Default.Schedule, "Programmer", Color(0xFF6A1B9A)) {
                                    showAttachmentMenu = false
                                    showScheduleDialog = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Modals Render ---
    if (showStickerPicker) {
        StickerPickerModal(
            packs = stickerPacks,
            stickers = allStickers,
            onSelectSticker = { sticker ->
                activePartner?.let {
                    viewModel.sendRichMessage(it.id, "Sticker", mediaType = "STICKER", mediaUrl = sticker.imageUrl)
                }
                showStickerPicker = false
            },
            onOpenCreator = {
                showStickerPicker = false
                showStickerCreator = true
            },
            onDismiss = { showStickerPicker = false }
        )
    }

    if (showStickerCreator) {
        StickerCreatorDialog(
            onCreateSticker = { url, emoji ->
                viewModel.createCustomSticker(url, emoji)
                activePartner?.let {
                    viewModel.sendRichMessage(it.id, "Sticker Personnalisé", mediaType = "STICKER", mediaUrl = url)
                }
            },
            onDismiss = { showStickerCreator = false }
        )
    }

    if (showGifPicker) {
        GifPickerModal(
            onSelectGif = { gifUrl ->
                activePartner?.let {
                    viewModel.sendRichMessage(it.id, "GIF Animé", mediaType = "GIF", mediaUrl = gifUrl)
                }
                showGifPicker = false
            },
            onDismiss = { showGifPicker = false }
        )
    }

    if (showPollCreator) {
        PollCreatorDialog(
            onCreatePoll = { q, opts, isAnon ->
                activePartner?.let {
                    viewModel.sendRichMessage(
                        partnerId = it.id,
                        text = "Sondage : $q",
                        mediaType = "POLL",
                        pollQuestion = q,
                        pollOptionsJson = opts.joinToString("|"),
                        pollIsAnonymous = isAnon
                    )
                }
            },
            onDismiss = { showPollCreator = false }
        )
    }

    if (showLocationDialog) {
        ShareLocationDialog(
            onShareLocation = { addr, isLive, dur ->
                activePartner?.let {
                    viewModel.sendRichMessage(
                        partnerId = it.id,
                        text = if (isLive) "📍 Localisation en direct ($dur min)" else "📍 Localisation actuelle",
                        mediaType = "LOCATION",
                        locationAddress = addr,
                        isLiveLocation = isLive,
                        liveLocationDurationMinutes = dur
                    )
                }
            },
            onDismiss = { showLocationDialog = false }
        )
    }

    if (showScheduleDialog) {
        ScheduleMessageDialog(
            onSchedule = { time ->
                activePartner?.let {
                    viewModel.scheduleMessage(it.id, messageInput.ifBlank { "Message sous planning" }, time)
                    messageInput = ""
                }
            },
            onDismiss = { showScheduleDialog = false }
        )
    }

    if (showForwardDialog && messageToForward != null) {
        ForwardMessageDialog(
            contacts = contacts,
            onForwardTo = { partnerIds ->
                viewModel.forwardMessages(partnerIds, listOf(messageToForward!!))
                messageToForward = null
            },
            onDismiss = { showForwardDialog = false }
        )
    }
}

@Composable
fun AttachmentMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun RichMessageBubbleItem(
    message: MessageEntity,
    isMe: Boolean,
    translation: String?,
    onReply: () -> Unit,
    onReaction: (String) -> Unit,
    onPin: () -> Unit,
    onFavorite: () -> Unit,
    onForward: () -> Unit,
    onVotePoll: (Int) -> Unit,
    onTranslate: (String) -> Unit,
    onClearTranslation: () -> Unit,
    onConvertToTask: () -> Unit = {},
    onConvertToNote: () -> Unit = {}
) {
    var showActionMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        )
                    )
                    .clickable { showActionMenu = !showActionMenu }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .widthIn(max = 280.dp)
            ) {
                Column {
                    // Pinned / Favorite indicators
                    if (message.isPinned || message.isFavorite) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                            if (message.isPinned) {
                                Icon(Icons.Default.PushPin, contentDescription = null, tint = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            if (message.isFavorite) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                            }
                        }
                    }

                    // Reply Header if this is a reply to another message
                    message.replyToText?.let { replyText ->
                        Surface(
                            color = if (isMe) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "↳ $replyText",
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }

                    // Custom Media Renderer based on mediaType
                    when (message.mediaType) {
                        "STICKER" -> {
                            AsyncImage(
                                model = message.mediaUrl ?: message.imageUrl,
                                contentDescription = "Sticker",
                                modifier = Modifier
                                    .size(140.dp)
                                    .padding(4.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        "GIF" -> {
                            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
                                AsyncImage(
                                    model = message.mediaUrl,
                                    contentDescription = "GIF",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Surface(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(4.dp)
                                ) {
                                    Text("GIF", fontSize = 9.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                        "DOCUMENT" -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1.0f)) {
                                        Text(message.documentName ?: "Document.pdf", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${message.documentSize ?: "1.2 MB"} • Antivirus Vérifié ✅", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                        "LOCATION" -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFE65100))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (message.isLiveLocation) "Position en Direct 📡" else "Position Partagée", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(message.locationAddress ?: "Localisation actuelle", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                        "CONTACT" -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = message.contactAvatar ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80",
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(message.contactName ?: "Contact", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(message.contactPhone ?: "+33 6 00 00 00 00", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                        "POLL" -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(message.pollQuestion ?: "Sondage", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val options = message.pollOptionsJson?.split("|") ?: listOf("Option A", "Option B")
                                    options.forEachIndexed { idx, opt ->
                                        OutlinedButton(
                                            onClick = { onVotePoll(idx) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Text(opt, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                        "ALBUM" -> {
                            val urls = message.albumUrlsJson?.split("|") ?: emptyList()
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                            ) {
                                urls.take(3).forEach { url ->
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .weight(1.0f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                        else -> {
                            Text(
                                text = message.text,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Time and Status footer
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp)),
                            color = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.secondary,
                            fontSize = 10.sp
                        )
                        if (isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Lu",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Reactions Display Badge Bar
        if (message.reactions.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(top = 2.dp, start = if (isMe) 0.dp else 12.dp)
            ) {
                listOf("👍", "❤️", "😂", "😮", "😢", "😡").forEach { emoji ->
                    if (message.reactions.contains(emoji)) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shadowElevation = 1.dp
                        ) {
                            Text(emoji, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }

        // Action Menu (Quick Reactions, Reply, Pin, Star, Forward, Translate)
        if (showActionMenu) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Quick Emoji Reactions Row
                    LazyRow(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        items(listOf("👍", "❤️", "😂", "😮", "😢", "😡")) { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .clickable {
                                        onReaction(emoji)
                                        showActionMenu = false
                                    }
                                    .padding(horizontal = 6.dp)
                            )
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onReply()
                                showActionMenu = false
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Répondre", fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPin()
                                showActionMenu = false
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (message.isPinned) "Désépingler" else "Épingler", fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onFavorite()
                                showActionMenu = false
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (message.isFavorite) "Retirer des favoris" else "Ajouter aux favoris", fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onForward()
                                showActionMenu = false
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Transférer", fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onConvertToTask()
                                showActionMenu = false
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AddTask, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Créer une Tâche", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onConvertToNote()
                                showActionMenu = false
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NoteAdd, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ajouter aux Notes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Inline Translation result
        translation?.let { trans ->
            Box(
                modifier = Modifier
                    .padding(start = if (isMe) 0.dp else 28.dp, top = 4.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
                    .widthIn(max = 240.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1.0f)) {
                        Text(
                            text = "Traduction IA :",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = trans,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(onClick = onClearTranslation, modifier = Modifier.size(18.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

@Composable
fun ContactItemRow(
    contact: UserProfileEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("contact_item_${contact.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = contact.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1.0f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = contact.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        }
    }
}
