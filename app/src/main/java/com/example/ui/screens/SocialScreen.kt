package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.PostEntity
import com.example.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(viewModel: AppViewModel) {
    val posts by viewModel.posts.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val spamResult by viewModel.spamCheckResult.collectAsState()

    var showCreatePostDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("Tous") }

    // Navigation and feed filters
    var currentFeedMode by remember { mutableStateOf("Chronologique") } // "Chronologique" or "Recommandé"

    // Reporting state
    var reportingPostId by remember { mutableStateOf<Long?>(null) }
    var reportReasonInput by remember { mutableStateOf("") }

    // Comments overlay state
    val activeCommentsPostId by viewModel.activeCommentPostId.collectAsState()
    val activeComments by viewModel.activePostComments.collectAsState()
    var commentTextInput by remember { mutableStateOf("") }

    val categories = listOf("Tous", "General", "Art", "Tech", "Marketplace")

    // Filtered posts logic
    val filteredPosts = remember(posts, selectedCategoryFilter, currentFeedMode) {
        var baseList = posts
        if (selectedCategoryFilter != "Tous") {
            baseList = baseList.filter { it.category == selectedCategoryFilter }
        }
        if (currentFeedMode == "Recommandé") {
            // Sort by likesCount first to simulate personalized recommendations
            baseList = baseList.sortedByDescending { it.likesCount }
        }
        baseList
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Header with Create button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Fil d'actualité",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Connectez-vous à la communauté",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.secondary)
                    )
                }

                Button(
                    onClick = { showCreatePostDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("create_post_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Publier")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Publier")
                }
            }

            // Feed Mode Switcher (Tab-like row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Chronologique", "Recommandé (IA)").forEach { mode ->
                    val isSelected = (mode == "Chronologique" && currentFeedMode == "Chronologique") ||
                            (mode.startsWith("Recommandé") && currentFeedMode == "Recommandé")
                    ElevatedFilterChip(
                        selected = isSelected,
                        onClick = {
                            currentFeedMode = if (mode.startsWith("Recommandé")) "Recommandé" else "Chronologique"
                        },
                        label = { Text(mode) },
                        leadingIcon = if (mode.startsWith("Recommandé")) {
                            { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            // Category Filter Scrollable Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategoryFilter == category,
                        onClick = { selectedCategoryFilter = category },
                        label = { Text(category) },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("category_chip_$category")
                    )
                }
            }

            // Feed List
            if (filteredPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Aucune publication",
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aucune publication trouvée dans cette catégorie.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredPosts, key = { it.id }) { post ->
                        PostCardItem(
                            post = post,
                            viewModel = viewModel,
                            onReportClick = { reportingPostId = post.id },
                            onCommentsClick = { viewModel.loadComments(post.id) }
                        )
                    }
                }
            }
        }

        // Create Post Dialog
        if (showCreatePostDialog) {
            CreatePostDialog(
                onDismiss = { showCreatePostDialog = false },
                onSubmit = { text, url, cat ->
                    viewModel.createPost(text, url, cat)
                    showCreatePostDialog = false
                }
            )
        }

        // Report Dialog
        if (reportingPostId != null) {
            AlertDialog(
                onDismissRequest = { reportingPostId = null },
                title = { Text("Signaler la publication") },
                text = {
                    Column {
                        Text("Pourquoi souhaitez-vous signaler cette publication ? Elle sera transmise à l'administration.")
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = reportReasonInput,
                            onValueChange = { reportReasonInput = it },
                            label = { Text("Raison du signalement") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            reportingPostId?.let { postId ->
                                viewModel.reportPost(postId, reportReasonInput.ifEmpty { "Contenu inapproprié" })
                            }
                            reportingPostId = null
                            reportReasonInput = ""
                        }
                    ) {
                        Text("Signaler")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { reportingPostId = null }) {
                        Text("Annuler")
                    }
                }
            )
        }

        // Comments Bottom Sheet overlay
        if (activeCommentsPostId != null) {
            Dialog(onDismissRequest = { viewModel.selectChatPartner(null); viewModel.loadComments(-1) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f)
                        .padding(12.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Commentaires",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            IconButton(onClick = { viewModel.loadComments(-1) }) {
                                Icon(Icons.Default.Close, contentDescription = "Fermer")
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // Comments List
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1.0f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (activeComments.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Aucun commentaire pour le moment. Soyez le premier !", color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            } else {
                                items(activeComments) { comment ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AsyncImage(
                                            model = comment.authorAvatarUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Column(
                                            modifier = Modifier
                                                .weight(1.0f)
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                comment.authorName,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                comment.text,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Write comment input
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = commentTextInput,
                                onValueChange = { commentTextInput = it },
                                placeholder = { Text("Écrire un commentaire...") },
                                modifier = Modifier.weight(1.0f),
                                maxLines = 2
                            )
                            IconButton(
                                onClick = {
                                    if (commentTextInput.isNotEmpty()) {
                                        activeCommentsPostId?.let { postId ->
                                            viewModel.addComment(postId, commentTextInput)
                                        }
                                        commentTextInput = ""
                                    }
                                },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Envoyer")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostCardItem(
    post: PostEntity,
    viewModel: AppViewModel,
    onReportClick: () -> Unit,
    onCommentsClick: () -> Unit
) {
    var isSpamAnalyzing by remember { mutableStateOf(false) }
    var localSpamVerdict by remember { mutableStateOf<String?>(null) }

    val formattedDate = remember(post.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        sdf.format(Date(post.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Post Author Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = post.authorAvatarUrl,
                        contentDescription = "Avatar de ${post.authorName}",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Category Tag
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = post.category,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Actions dropdown
                Box {
                    IconButton(onClick = onReportClick) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Signaler",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Text content
            Text(
                text = post.contentText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            // Post Image Attachment (if available)
            post.imageUrl?.let { imgUrl ->
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = imgUrl,
                    contentDescription = "Image de publication",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action counts row (Like, Comment, AI spam check)
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                Row(
                    modifier = Modifier
                        .clickable { viewModel.toggleLikePost(post) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (post.hasLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Aimer",
                        tint = if (post.hasLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = post.likesCount.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Comments Button
                Row(
                    modifier = Modifier
                        .clickable { onCommentsClick() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Commentaires",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Réponses",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // AI Spam check
                Button(
                    onClick = {
                        isSpamAnalyzing = true
                        viewModel.runSpamCheck(post.contentText)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("ai_check_post_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Analyser IA", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Inline local spam verdict response
            AnimatedVisibility(
                visible = isSpamAnalyzing,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                val spamResultGlobal by viewModel.spamCheckResult.collectAsState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Rapport d'analyse de modération IA",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            IconButton(
                                onClick = {
                                    isSpamAnalyzing = false
                                    viewModel.clearSpamCheck()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Fermer",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = spamResultGlobal ?: "Interrogation du modèle Gemini...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onSubmit: (text: String, imageUrl: String, category: String) -> Unit
) {
    var contentText by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("General") }

    val categories = listOf("General", "Art", "Tech", "Marketplace")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Créer une publication",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                // Category selector chip row
                Column {
                    Text("Catégorie :", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    label = { Text("Que voulez-vous dire ?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL de l'image (optionnel)") },
                    placeholder = { Text("https://example.com/image.jpg") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (contentText.isNotEmpty()) {
                                onSubmit(contentText, imageUrl, selectedCategory)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Publier")
                    }
                }
            }
        }
    }
}
