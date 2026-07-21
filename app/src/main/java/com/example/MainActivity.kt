package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.AppRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppViewModel
import com.example.viewmodel.AppViewModelFactory
import com.example.viewmodel.CallState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val database = remember { AppDatabase.getDatabase(context) }
            val repository = remember { AppRepository(database.appDao()) }
            
            // ViewModel instantiation using Factory
            val appViewModel: AppViewModel = viewModel(
                factory = AppViewModelFactory(application, repository)
            )

            val isDarkMode by appViewModel.isDarkMode.collectAsState()
            val currentUser by appViewModel.currentUser.collectAsState()
            val callState by appViewModel.callState.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (currentUser == null) {
                        // Onboarding & Login Gate
                        LoginScreen(
                            onLoginSuccess = { name, email, bio, avatar ->
                                appViewModel.registerAndLogin(name, email, bio, avatar)
                            }
                        )
                    } else {
                        // Main App Container with Scaffold
                        var currentTab by remember { mutableStateOf("Social") } // Social, Messages, Marketplace, Admin, Profile

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                NavigationBar(
                                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                                ) {
                                    NavigationBarItem(
                                        selected = currentTab == "Social",
                                        onClick = { currentTab = "Social" },
                                        label = { Text("Feed") },
                                        icon = { Icon(Icons.Default.Public, contentDescription = "Feed") },
                                        modifier = Modifier.testTag("nav_tab_social")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == "Messages",
                                        onClick = { currentTab = "Messages" },
                                        label = { Text("Chats") },
                                        icon = { Icon(Icons.Default.Chat, contentDescription = "Discussions") },
                                        modifier = Modifier.testTag("nav_tab_messages")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == "Games",
                                        onClick = { currentTab = "Games" },
                                        label = { Text("Jeux") },
                                        icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Jeux") },
                                        modifier = Modifier.testTag("nav_tab_games")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == "Productivity",
                                        onClick = { currentTab = "Productivity" },
                                        label = { Text("Studio") },
                                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Productivité") },
                                        modifier = Modifier.testTag("nav_tab_productivity")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == "Cloud",
                                        onClick = { currentTab = "Cloud" },
                                        label = { Text("Cloud") },
                                        icon = { Icon(Icons.Default.Cloud, contentDescription = "Cloud") },
                                        modifier = Modifier.testTag("nav_tab_cloud")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == "Marketplace",
                                        onClick = { currentTab = "Marketplace" },
                                        label = { Text("Market") },
                                        icon = { Icon(Icons.Default.Storefront, contentDescription = "Marché") },
                                        modifier = Modifier.testTag("nav_tab_marketplace")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == "Admin",
                                        onClick = { currentTab = "Admin" },
                                        label = { Text("Admin") },
                                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                                        modifier = Modifier.testTag("nav_tab_admin")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == "Profile",
                                        onClick = { currentTab = "Profile" },
                                        label = { Text("Profil") },
                                        icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                                        modifier = Modifier.testTag("nav_tab_profile")
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (currentTab) {
                                    "Social" -> SocialScreen(viewModel = appViewModel)
                                    "Messages" -> MessagingScreen(viewModel = appViewModel)
                                    "Games" -> GamesScreen(viewModel = appViewModel)
                                    "Productivity" -> ProductivityScreen(viewModel = appViewModel)
                                    "Cloud" -> CloudScreen(viewModel = appViewModel)
                                    "Marketplace" -> MarketplaceScreen(viewModel = appViewModel)
                                    "Admin" -> AdminScreen(viewModel = appViewModel)
                                    "Profile" -> ProfileScreen(viewModel = appViewModel)
                                }
                            }
                        }

                        // Full Screen Call simulation overlay
                        AnimatedVisibility(
                            visible = callState !is CallState.Idle,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            CallScreen(
                                callState = callState,
                                onDecline = { appViewModel.endCall() }
                            )
                        }
                    }
                }
            }
        }
    }
}
