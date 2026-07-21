package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.AuditLogEntity
import com.example.data.local.PostEntity
import com.example.data.local.UserProfileEntity
import com.example.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: AppViewModel) {
    val reportedPosts by viewModel.reportedPosts.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var selectedTab by remember { mutableStateOf("Stats") } // Stats, Modération, Comptes, Journaux

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Administration",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                text = "Tableau de bord de sécurité et de surveillance mondiale",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.secondary)
            )
        }

        // Custom Tab Row
        ScrollableTabRow(
            selectedTabIndex = when(selectedTab) {
                "Stats" -> 0
                "Modération" -> 1
                "Comptes" -> 2
                else -> 3
            },
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("Stats", "Modération", "Comptes", "Journaux").forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_$tab")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .padding(horizontal = 16.dp)
        ) {
            when(selectedTab) {
                "Stats" -> StatsDashboardView(reportedPosts.size, contacts.size, auditLogs.size)
                "Modération" -> ModerationQueueView(reportedPosts, viewModel)
                "Comptes" -> UsersManagementView(contacts, viewModel)
                else -> AuditLogsView(auditLogs)
            }
        }
    }
}

@Composable
fun StatsDashboardView(reportsCount: Int, usersCount: Int, logsCount: Int) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            // Cards Overview
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    Triple("Utilisateurs", usersCount.toString(), Icons.Default.People),
                    Triple("Signalements", reportsCount.toString(), Icons.Default.Warning),
                    Triple("Requêtes IA", "148", Icons.Default.AutoAwesome)
                ).forEach { (title, count, icon) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.weight(1.0f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Icon(icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(count, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                            Text(title, style = MaterialTheme.typography.bodySmall, color = secondaryColor)
                        }
                    }
                }
            }
        }

        item {
            // Bespoke custom drawn chart showing WorldConnect traffic volume
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Activité Globale (Dernières 24 Heures)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Requêtes traitées par minute en temps réel",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        val width = size.width
                        val height = size.height

                        // Draw Grid lines
                        val gridLines = 4
                        for (i in 0..gridLines) {
                            val y = height * i / gridLines
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.15f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 2f
                            )
                        }

                        // Coordinates
                        val dataPoints = listOf(40f, 120f, 85f, 220f, 160f, 290f, 210f, 350f, 310f, 420f, 380f, 490f)
                        val maxVal = 550f
                        val stepX = width / (dataPoints.size - 1)

                        val points = dataPoints.mapIndexed { idx, value ->
                            Offset(stepX * idx, height - (value / maxVal * height))
                        }

                        // Draw Gradient fill
                        val fillPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, height)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(width, height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )

                        // Draw Line
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = primaryColor,
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 6f
                            )
                        }

                        // Draw circles on points
                        points.forEach { pt ->
                            drawCircle(
                                color = tertiaryColor,
                                radius = 6f,
                                center = pt
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModerationQueueView(reportedPosts: List<PostEntity>, viewModel: AppViewModel) {
    if (reportedPosts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("File de modération vide. Aucun contenu signalé ! 🎉", color = MaterialTheme.colorScheme.secondary)
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(reportedPosts) { post ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = post.authorAvatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(post.authorName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Signalé pour : ${post.reportReason ?: "Raison inconnue"}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Red))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(post.contentText, style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.deletePost(post.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1.0f)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Supprimer", fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    // Dismiss flag (save post with isReported = false)
                                    viewModel.createPost(
                                        text = post.contentText,
                                        imageUrl = post.imageUrl,
                                        category = post.category
                                    )
                                    viewModel.deletePost(post.id) // deletes old reported row
                                },
                                modifier = Modifier.weight(1.0f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ignorer", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsersManagementView(users: List<UserProfileEntity>, viewModel: AppViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(users) { user ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.0f)) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(user.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    Button(
                        onClick = { viewModel.toggleBlockContact(user.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (user.isBlocked) MaterialTheme.colorScheme.primary else Color.Red.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (user.isBlocked) "Débloquer" else "Bloquer")
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogsView(logs: List<AuditLogEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        if (logs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Aucun journal d'audit enregistré.", color = MaterialTheme.colorScheme.secondary)
                }
            }
        } else {
            items(logs) { log ->
                val date = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "[${log.action}]",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = date,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Text(
                        text = log.details,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}
