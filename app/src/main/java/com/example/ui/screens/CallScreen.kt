package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.viewmodel.CallState
import kotlinx.coroutines.delay

@Composable
fun CallScreen(
    callState: CallState,
    onDecline: () -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var isVideoCamOn by remember { mutableStateOf(true) }

    val partner = when (callState) {
        is CallState.Dialing -> callState.partner
        is CallState.Active -> callState.partner
        else -> return
    }

    val isVideo = when (callState) {
        is CallState.Dialing -> callState.isVideo
        is CallState.Active -> callState.isVideo
        else -> false
    }

    val isActive = callState is CallState.Active

    // Pulsing circle animation for Dialing state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Call timer state
    var secondsElapsed by remember { mutableStateOf(0) }
    LaunchedEffect(isActive) {
        if (isActive) {
            while (true) {
                delay(1000)
                secondsElapsed++
            }
        }
    }

    val minutes = secondsElapsed / 60
    val seconds = secondsElapsed % 60
    val timerString = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Dark slate background for high-tech communication feel
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Main calling interface card
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // User Info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isActive) "Appel en cours..." else "Mise en relation...",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar with Pulsing ring if Dialing
                Box(contentAlignment = Alignment.Center) {
                    if (!isActive) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .scale(pulseScale)
                                .background(Color.White.copy(alpha = 0.15f), shape = CircleShape)
                        )
                    }

                    AsyncImage(
                        model = partner.avatarUrl,
                        contentDescription = partner.name,
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = partner.name,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isActive) timerString else "Slogan: Connecter le monde sans frontières",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Video Camera active preview simulation
            if (isVideo && isActive && isVideoCamOn) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(180.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // User's own camera preview simulator (loads unspash tech background)
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=400&q=80",
                            contentDescription = "Mon aperçu caméra",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                        )
                        Text(
                            "Votre aperçu caméra HD (Simulé)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                        )
                    }
                }
            } else {
                // Audio Waveform animation simulation
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(60.dp)
                ) {
                    repeat(6) { index ->
                        val waveHeight by rememberInfiniteTransition(label = "wave_$index").animateFloat(
                            initialValue = 10f,
                            targetValue = if (isActive) 50f else 20f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(300 + (index * 100), easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "waveHeight"
                        )
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(waveHeight.dp)
                                .background(
                                    color = if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.White.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }
            }

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                // Mic Mute toggle
                IconButton(
                    onClick = { isMuted = !isMuted },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isMuted) Color.Red else Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Sourdine"
                    )
                }

                // Decline Call button
                IconButton(
                    onClick = onDecline,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("decline_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Raccrocher",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Camera or Speaker toggle
                IconButton(
                    onClick = {
                        if (isVideo) {
                            isVideoCamOn = !isVideoCamOn
                        } else {
                            isSpeakerOn = !isSpeakerOn
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isVideo && !isVideoCamOn) Color.Red else Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (isVideo) {
                            if (isVideoCamOn) Icons.Default.Videocam else Icons.Default.VideocamOff
                        } else {
                            if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute
                        },
                        contentDescription = "Action"
                    )
                }
            }
        }
    }
}
