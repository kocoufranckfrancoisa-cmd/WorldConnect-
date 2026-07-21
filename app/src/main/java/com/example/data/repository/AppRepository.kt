package com.example.data.repository

import com.example.data.local.*
import com.example.data.remote.GeminiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Locale

class AppRepository(private val appDao: AppDao) {

    // --- User Session ---
    val currentUserFlow: Flow<UserProfileEntity?> = appDao.getCurrentUserFlow()
    val contactsFlow: Flow<List<UserProfileEntity>> = appDao.getAllContactsFlow()

    suspend fun getOrCreateCurrentUser(): UserProfileEntity {
        val existing = appDao.getCurrentUser()
        if (existing != null) return existing

        val defaultUser = UserProfileEntity(
            id = "user_me",
            name = "Alex Mercer",
            email = "alex.mercer@worldconnect.com",
            bio = "Tech explorer & digital nomad. Building a borderless future on WorldConnect! 🌍💻",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80",
            coverUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80",
            isCurrentUser = true
        )
        appDao.insertUser(defaultUser)
        appDao.insertAuditLog(AuditLogEntity(action = "ACCOUNT_INITIALIZATION", details = "Default user account created"))
        return defaultUser
    }

    suspend fun loginAsUser(name: String, email: String, bio: String, avatarUrl: String) {
        // Clear old current user status
        val oldCurrentUser = appDao.getCurrentUser()
        if (oldCurrentUser != null) {
            appDao.insertUser(oldCurrentUser.copy(isCurrentUser = false))
        }

        val newUser = UserProfileEntity(
            id = "user_me",
            name = name,
            email = email,
            bio = bio,
            avatarUrl = avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80" },
            coverUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80",
            isCurrentUser = true
        )
        appDao.insertUser(newUser)
        appDao.insertAuditLog(AuditLogEntity(action = "USER_LOGIN", details = "User logged in: $name ($email)"))
    }

    suspend fun updateCurrentUser(bio: String, name: String, avatarUrl: String, coverUrl: String) {
        val current = appDao.getCurrentUser() ?: return
        val updated = current.copy(
            bio = bio,
            name = name,
            avatarUrl = avatarUrl,
            coverUrl = coverUrl
        )
        appDao.insertUser(updated)
        appDao.insertAuditLog(AuditLogEntity(action = "PROFILE_UPDATE", details = "Updated bio, name and assets"))
    }

    suspend fun toggleBlockUser(userId: String) {
        val users = appDao.getAllUsers()
        val target = users.find { it.id == userId } ?: return
        val updated = target.copy(isBlocked = !target.isBlocked)
        appDao.insertUser(updated)
        appDao.insertAuditLog(
            AuditLogEntity(
                action = if (updated.isBlocked) "BLOCK_USER" else "UNBLOCK_USER",
                details = "User status changed",
                targetUser = target.name
            )
        )
    }

    // --- Prepopulate Initial Data ---
    suspend fun checkAndPrepopulateData() {
        val users = appDao.getAllUsers()
        if (users.size > 1) return // Already prepopulated

        // Contacts
        val contact1 = UserProfileEntity(
            id = "user_maya",
            name = "Maya Lin (AI Bot)",
            email = "maya.ai@worldconnect.com",
            bio = "Your official WorldConnect AI Assistant. Direct gateway to translation, games AI, and advice! 🤖✨",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=200&q=80",
            coverUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=800&q=80"
        )
        val contact2 = UserProfileEntity(
            id = "user_jean",
            name = "Jean Dupont",
            email = "jean.dupont@paris.fr",
            bio = "Co-founder of 'La Parisienne' Boutique. Chess grandmaster enthusiast & leathercraft designer in Paris. ♟️🥐",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80",
            coverUrl = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=800&q=80"
        )
        val contact3 = UserProfileEntity(
            id = "user_elena",
            name = "Elena Rostova",
            email = "elena.r@berlin.de",
            bio = "Interactive visual media designer & 3D artist. WorldConnect Games Othello Champion! 🎨♟️",
            avatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=200&q=80",
            coverUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=800&q=80"
        )
        val contact4 = UserProfileEntity(
            id = "user_mateo",
            name = "Mateo Silva",
            email = "mateo.silva@tech.br",
            bio = "Brazilian mechanical keyboard specialist and competitive gamer. Always online! ⌨️🕹️",
            avatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=200&q=80",
            coverUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=800&q=80"
        )

        appDao.insertUser(contact1)
        appDao.insertUser(contact2)
        appDao.insertUser(contact3)
        appDao.insertUser(contact4)

        // Prepopulate Social Posts
        appDao.insertPost(
            PostEntity(
                authorId = contact4.id,
                authorName = contact4.name,
                authorAvatarUrl = contact4.avatarUrl,
                contentText = "WorldConnect Games is officially here! Just played an intense match of Awalé (Mancala) and Chess with friends in Paris and Berlin. Low latency, live real-time voice chat, pure magic! 🕹️🌍",
                likesCount = 38,
                hasLiked = true,
                category = "General"
            )
        )
        appDao.insertPost(
            PostEntity(
                authorId = contact3.id,
                authorName = contact3.name,
                authorAvatarUrl = contact3.avatarUrl,
                contentText = "My digital canvas package 'Neon Dreamscapes' is live in the Marketplace! Also hosting an Othello tournament in WorldConnect Games tonight. Check it out! 🎨✨",
                imageUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?auto=format&fit=crop&w=600&q=80",
                likesCount = 42,
                hasLiked = false,
                category = "Art"
            )
        )
        appDao.insertPost(
            PostEntity(
                authorId = contact2.id,
                authorName = contact2.name,
                authorAvatarUrl = contact2.avatarUrl,
                contentText = "Fresh batch of Butter Macarons and hand-crafted leather goods ready to ship worldwide from Paris. Visitez notre boutique in the Marketplace! Coupon code: WC10 for first purchase. 🥐🇫🇷👜",
                likesCount = 19,
                hasLiked = false,
                category = "Marketplace"
            )
        )

        // Prepopulate Products
        appDao.insertProduct(
            ProductEntity(
                name = "Neon Dreamscapes Wallpaper Pack",
                description = "Bundle of 12 ultra-high resolution (8K) cyberpunk wallpapers with moving lighting, tailored beautifully for foldables, dual screens, and desktops. Includes light and dark variations.",
                price = 4.99,
                imageUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?auto=format&fit=crop&w=400&q=80",
                rating = 4.9f,
                reviewsCount = 45,
                category = "Art & Media",
                sellerId = contact3.id,
                sellerName = contact3.name
            )
        )
        appDao.insertProduct(
            ProductEntity(
                name = "Vintage Full-Grain Leather Messenger Bag",
                description = "Handmade in Paris from organic vegetable-tanned leather. Fits up to a 16-inch laptop. Heavy brass buckles and dynamic compartments. Ships in custom dustbag.",
                price = 149.00,
                imageUrl = "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=400&q=80",
                rating = 4.8f,
                reviewsCount = 18,
                category = "Bags",
                sellerId = contact2.id,
                sellerName = contact2.name
            )
        )
        appDao.insertProduct(
            ProductEntity(
                name = "Custom 65% Mechanical Keyboard",
                description = "Hot-swappable tactile switches, brass plate, lubed stabilizers, and durable double-shot PBT keycaps with custom legend engraving. Perfect for coding and gaming.",
                price = 110.00,
                imageUrl = "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=400&q=80",
                rating = 4.7f,
                reviewsCount = 29,
                category = "Electronics",
                sellerId = contact4.id,
                sellerName = contact4.name
            )
        )

        // Prepopulate initial system message
        appDao.insertMessage(
            MessageEntity(
                chatPartnerId = contact1.id,
                senderId = contact1.id,
                senderName = contact1.name,
                text = "Welcome to WorldConnect! I am Maya, your built-in intelligence assistant. Try WorldConnect Games to play Chess, Awalé, Puissance 4, and more against friends or AI bots! 🚀",
                timestamp = System.currentTimeMillis() - 60000
            )
        )

        // Prepopulate WorldConnect Games initial data
        prepopulateGameData(contact2, contact3, contact4)

        // Prepopulate Sticker Packs and Stickers
        prepopulateStickerData()

        appDao.insertAuditLog(
            AuditLogEntity(action = "DATABASE_PREPOPULATION", details = "Prepopulated contacts, social posts, marketplace items, games, and sticker packs")
        )
    }

    private suspend fun prepopulateStickerData() {
        val pack1 = com.example.data.local.StickerPackEntity(
            id = "pack_animals",
            title = "Mignons Animaux 🐱🐶",
            author = "WorldConnect Studio",
            description = "Stickers haute définition d'animaux rigolos et expressifs",
            category = "Animaux",
            thumbnailUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=150&q=80",
            stickerCount = 6
        )
        val pack2 = com.example.data.local.StickerPackEntity(
            id = "pack_reactions",
            title = "Réactions Fun 😂🔥",
            author = "Community Creators",
            description = "Express yourself with vibrant reaction memes and stickers",
            category = "Reactions",
            thumbnailUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80",
            stickerCount = 6
        )

        appDao.insertStickerPack(pack1)
        appDao.insertStickerPack(pack2)

        val stickers = listOf(
            com.example.data.local.StickerEntity("stk_1", "pack_animals", "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=200&q=80", "🐱", isAnimated = true),
            com.example.data.local.StickerEntity("stk_2", "pack_animals", "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=200&q=80", "🐶", isAnimated = false),
            com.example.data.local.StickerEntity("stk_3", "pack_animals", "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?auto=format&fit=crop&w=200&q=80", "😎", isAnimated = true),
            com.example.data.local.StickerEntity("stk_4", "pack_reactions", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80", "😂", isAnimated = true),
            com.example.data.local.StickerEntity("stk_5", "pack_reactions", "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=200&q=80", "❤️", isAnimated = false),
            com.example.data.local.StickerEntity("stk_6", "pack_reactions", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=200&q=80", "🔥", isAnimated = true)
        )
        stickers.forEach { appDao.insertSticker(it) }
    }

    private suspend fun prepopulateGameData(
        jean: UserProfileEntity,
        elena: UserProfileEntity,
        mateo: UserProfileEntity
    ) {
        // Prepopulate Game Rooms
        appDao.insertGameRoom(
            com.example.data.local.GameRoomEntity(
                title = "Salon International Échecs Rapide",
                gameType = "Échecs",
                hostId = jean.id,
                hostName = jean.name,
                isPrivate = false,
                currentPlayers = 1,
                maxPlayers = 2,
                isRanked = true,
                region = "Mondial"
            )
        )
        appDao.insertGameRoom(
            com.example.data.local.GameRoomEntity(
                title = "Awalé (Oware) - Défi Traditionnel",
                gameType = "Awalé",
                hostId = mateo.id,
                hostName = mateo.name,
                isPrivate = false,
                currentPlayers = 1,
                maxPlayers = 2,
                isRanked = true,
                region = "Afrique / Europe"
            )
        )
        appDao.insertGameRoom(
            com.example.data.local.GameRoomEntity(
                title = "Puissance 4 Masters",
                gameType = "Puissance 4",
                hostId = elena.id,
                hostName = elena.name,
                isPrivate = false,
                currentPlayers = 1,
                maxPlayers = 2,
                isRanked = false,
                region = "Mondial"
            )
        )

        // Prepopulate Tournaments
        appDao.insertTournament(
            com.example.data.local.TournamentEntity(
                title = "Grand Chelem d'Échecs WorldConnect 2026",
                gameType = "Échecs",
                prizePool = "5,000 USD + Badge Or",
                playersCount = 12,
                maxPlayers = 16,
                status = "REGISTRATION"
            )
        )
        appDao.insertTournament(
            com.example.data.local.TournamentEntity(
                title = "Coupe du Monde d'Awalé & Mancala",
                gameType = "Awalé",
                prizePool = "2,500 USD + Trophée Lumineux",
                playersCount = 8,
                maxPlayers = 8,
                status = "IN_PROGRESS"
            )
        )
        appDao.insertTournament(
            com.example.data.local.TournamentEntity(
                title = "Championnat Puissance 4 Express",
                gameType = "Puissance 4",
                prizePool = "1,000 USD",
                playersCount = 32,
                maxPlayers = 32,
                status = "COMPLETED",
                winnerName = elena.name
            )
        )

        // Prepopulate Game Stats
        val gamesList = listOf("Échecs", "Dames", "Othello", "Gomoku", "Puissance 4", "Tic-Tac-Toe", "Domino", "Backgammon", "Awalé")
        gamesList.forEachIndexed { idx, game ->
            appDao.insertOrUpdateGameStat(
                com.example.data.local.GameStatisticEntity(
                    gameType = game,
                    eloRating = 1200 + (idx * 25),
                    wins = 5 + idx,
                    losses = 2,
                    draws = 1,
                    winStreak = 3,
                    trophiesCount = idx + 1
                )
            )
        }

        // Prepopulate Achievements
        appDao.insertAchievement(
            com.example.data.local.AchievementEntity(
                id = "ach_first_win",
                title = "Première Victoire",
                description = "Gagner votre premier match dans WorldConnect Games",
                iconName = "EmojiEvents",
                isUnlocked = true,
                unlockedAt = System.currentTimeMillis() - 86400000
            )
        )
        appDao.insertAchievement(
            com.example.data.local.AchievementEntity(
                id = "ach_ai_master",
                title = "Vainqueur d'IA",
                description = "Battre l'intelligence artificielle Gemini au niveau Difficile",
                iconName = "AutoAwesome",
                isUnlocked = true,
                unlockedAt = System.currentTimeMillis() - 43200000
            )
        )
        appDao.insertAchievement(
            com.example.data.local.AchievementEntity(
                id = "ach_tourney_champ",
                title = "Roi du Tournoi",
                description = "Atteindre la finale d'un tournoi mondial officiel",
                iconName = "MilitaryTech",
                isUnlocked = false
            )
        )
        appDao.insertAchievement(
            com.example.data.local.AchievementEntity(
                id = "ach_strategist",
                title = "Grand Stratège",
                description = "Jouer à 5 jeux de stratégie différents",
                iconName = "Psychology",
                isUnlocked = true,
                unlockedAt = System.currentTimeMillis() - 10000000
            )
        )
    }

    // --- WorldConnect Games Repo ---
    val gameRoomsFlow: Flow<List<com.example.data.local.GameRoomEntity>> = appDao.getAllGameRooms()
    val recentMatchesFlow: Flow<List<com.example.data.local.GameMatchEntity>> = appDao.getRecentMatches()
    val tournamentsFlow: Flow<List<com.example.data.local.TournamentEntity>> = appDao.getAllTournaments()
    val gameStatsFlow: Flow<List<com.example.data.local.GameStatisticEntity>> = appDao.getAllGameStats()
    val achievementsFlow: Flow<List<com.example.data.local.AchievementEntity>> = appDao.getAllAchievements()

    suspend fun createGameRoom(
        title: String,
        gameType: String,
        isPrivate: Boolean,
        accessCode: String?,
        isRanked: Boolean,
        region: String
    ) {
        val user = getOrCreateCurrentUser()
        val room = com.example.data.local.GameRoomEntity(
            title = title,
            gameType = gameType,
            hostId = user.id,
            hostName = user.name,
            isPrivate = isPrivate,
            accessCode = accessCode,
            currentPlayers = 1,
            maxPlayers = 2,
            isRanked = isRanked,
            region = region
        )
        appDao.insertGameRoom(room)
        appDao.insertAuditLog(AuditLogEntity(action = "CREATE_GAME_ROOM", details = "Room created for $gameType: $title"))
    }

    suspend fun deleteGameRoom(roomId: Long) {
        appDao.deleteGameRoom(roomId)
    }

    suspend fun recordGameMatch(
        gameType: String,
        opponentName: String,
        winnerName: String,
        movesHistory: String,
        isAiOpponent: Boolean
    ) {
        val user = getOrCreateCurrentUser()
        val match = com.example.data.local.GameMatchEntity(
            gameType = gameType,
            player1Name = user.name,
            player2Name = opponentName,
            winnerName = winnerName,
            movesHistory = movesHistory,
            isAiOpponent = isAiOpponent,
            eloChange = if (winnerName == user.name) 18 else -12
        )
        appDao.insertGameMatch(match)
        appDao.insertAuditLog(AuditLogEntity(action = "RECORD_MATCH", details = "Game match finished ($gameType), winner: $winnerName"))
    }

    suspend fun createTournament(
        title: String,
        gameType: String,
        prizePool: String,
        maxPlayers: Int
    ) {
        val tourney = com.example.data.local.TournamentEntity(
            title = title,
            gameType = gameType,
            prizePool = prizePool,
            maxPlayers = maxPlayers,
            playersCount = 1
        )
        appDao.insertTournament(tourney)
        appDao.insertAuditLog(AuditLogEntity(action = "CREATE_TOURNAMENT", details = "Tournament created: $title ($gameType)"))
    }

    suspend fun getAiGameMove(gameType: String, boardState: String, difficulty: String): String {
        val prompt = "You are Gemini AI Games Master in WorldConnect Games. You are playing $gameType at difficulty '$difficulty'. Current board state description: \"$boardState\". Output ONLY a concise JSON string describing your next optimal move: {\"moveIndex\": number, \"comment\": \"short tactical tip\"}. No preamble or markdown fences."
        return GeminiClient.generateText(prompt, "You are a master game strategy AI engine.")
    }

    // --- Social Network Repo ---
    val feedPostsFlow: Flow<List<PostEntity>> = appDao.getFeedPostsChronological()
    val reportedPostsFlow: Flow<List<PostEntity>> = appDao.getReportedPosts()

    suspend fun createPost(contentText: String, imageUrl: String? = null, category: String = "General") {
        val user = getOrCreateCurrentUser()
        val post = PostEntity(
            authorId = user.id,
            authorName = user.name,
            authorAvatarUrl = user.avatarUrl,
            contentText = contentText,
            imageUrl = imageUrl?.ifEmpty { null },
            category = category
        )
        appDao.insertPost(post)
        appDao.insertAuditLog(AuditLogEntity(action = "CREATE_POST", details = "Created post in category $category"))
    }

    suspend fun toggleLikePost(post: PostEntity) {
        val updated = post.copy(
            hasLiked = !post.hasLiked,
            likesCount = if (post.hasLiked) post.likesCount - 1 else post.likesCount + 1
        )
        appDao.insertPost(updated)
    }

    suspend fun reportPost(postId: Long, reason: String) {
        appDao.reportPost(postId, reason)
        appDao.insertAuditLog(AuditLogEntity(action = "REPORT_POST", details = "Post ID $postId reported for: $reason"))
    }

    suspend fun deletePost(postId: Long) {
        appDao.deletePost(postId)
        appDao.insertAuditLog(AuditLogEntity(action = "DELETE_POST", details = "Deleted post ID $postId by administrator"))
    }

    fun getComments(postId: Long): Flow<List<CommentEntity>> = appDao.getCommentsForPost(postId)

    suspend fun addComment(postId: Long, text: String) {
        val user = getOrCreateCurrentUser()
        val comment = CommentEntity(
            postId = postId,
            authorName = user.name,
            authorAvatarUrl = user.avatarUrl,
            text = text
        )
        appDao.insertComment(comment)
    }

    // --- Messaging Repo ---
    fun getMessages(partnerId: String): Flow<List<MessageEntity>> = appDao.getMessagesWithPartner(partnerId)
    val allMessagesFlow: Flow<List<MessageEntity>> = appDao.getAllMessagesFlow()

    suspend fun sendMessage(
        partnerId: String,
        text: String,
        imageUrl: String? = null,
        isVoice: Boolean = false,
        voiceDuration: Int = 0,
        replyToMessageId: Long? = null,
        replyToText: String? = null,
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
        val user = getOrCreateCurrentUser()
        val message = MessageEntity(
            chatPartnerId = partnerId,
            senderId = user.id,
            senderName = user.name,
            text = text,
            imageUrl = imageUrl ?: if (mediaType == "IMAGE") mediaUrl else null,
            isVoice = isVoice || mediaType == "VOICE",
            voiceDurationSec = voiceDuration,
            replyToMessageId = replyToMessageId,
            replyToText = replyToText,
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
        appDao.insertMessage(message)

        // Clear draft when sending
        appDao.deleteDraft(partnerId)

        // Trigger AI automation if partner is Maya
        if (partnerId == "user_maya") {
            simulateAiReply(text)
        }
    }

    private suspend fun simulateAiReply(userPrompt: String) {
        // We use Gemini model to respond in real time!
        val systemInstruction = "You are Maya Lin, WorldConnect's official built-in multilingual AI assistant. Be friendly, incredibly professional, and keep replies beautifully concise (under 3 sentences). Represent WorldConnect's values: connecting the world, free, and without borders. Support translations, summarize requests, and give smart recommendations if asked."
        val responseText = GeminiClient.generateText(userPrompt, systemInstruction)

        val aiMessage = MessageEntity(
            chatPartnerId = "user_maya",
            senderId = "user_maya",
            senderName = "Maya Lin (AI Bot)",
            text = responseText,
            isAiGenerated = true
        )
        appDao.insertMessage(aiMessage)
    }

    // --- Marketplace Repo ---
    val productsFlow: Flow<List<ProductEntity>> = appDao.getAllProductsFlow()

    suspend fun addProduct(name: String, description: String, price: Double, category: String, imageUrl: String) {
        val user = getOrCreateCurrentUser()
        val product = ProductEntity(
            name = name,
            description = description,
            price = price,
            imageUrl = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=400&q=80" },
            category = category,
            sellerId = user.id,
            sellerName = user.name
        )
        appDao.insertProduct(product)
        appDao.insertAuditLog(AuditLogEntity(action = "ADD_PRODUCT", details = "Product added to marketplace: $name ($price USD)"))
    }

    suspend fun removeProduct(productId: Long) {
        appDao.deleteProduct(productId)
        appDao.insertAuditLog(AuditLogEntity(action = "DELETE_PRODUCT", details = "Product ID $productId removed"))
    }

    // --- Audit Logs ---
    val auditLogsFlow: Flow<List<AuditLogEntity>> = appDao.getRecentAuditLogs()

    // --- Advanced AI Tools ---

    suspend fun translateMessage(text: String, targetLang: String): String {
        val prompt = "Translate the following text into $targetLang. Output ONLY the translated text, do not add any quotes, preambles, or additional notes:\n\n$text"
        val response = GeminiClient.generateText(prompt, "You are a professional, high-fidelity native translator.")
        appDao.insertAuditLog(AuditLogEntity(action = "AI_TRANSLATION", details = "Translated message to $targetLang"))
        return response
    }

    suspend fun summarizeConversation(partnerName: String, messages: List<MessageEntity>): String {
        if (messages.isEmpty()) return "No conversation history to summarize."
        val conversationHistory = messages.joinToString("\n") { "${it.senderName}: ${it.text}" }
        val prompt = "Please summarize this conversation history between WorldConnect users. Highlight key topics, agreements, and requests. Keep it highly readable and bulleted, under 150 words:\n\n$conversationHistory"
        val response = GeminiClient.generateText(prompt, "You are a highly efficient, professional corporate meeting summarizer.")
        appDao.insertAuditLog(AuditLogEntity(action = "AI_SUMMARIZATION", details = "Summarized conversation with $partnerName"))
        return response
    }

    suspend fun getSmartReplySuggestions(messages: List<MessageEntity>): List<String> {
        if (messages.isEmpty()) return listOf("Hi!", "Hello there!", "How is it going?")
        // Grab last 5 messages for context
        val recent = messages.takeLast(5).joinToString("\n") { "${it.senderName}: ${it.text}" }
        val prompt = "Based on the following recent messaging history, generate exactly 3 short, helpful, and highly relevant reply suggestions for the current user. Format the output as a clean comma-separated list like 'suggestion 1, suggestion 2, suggestion 3' and nothing else:\n\n$recent"
        val response = GeminiClient.generateText(prompt, "You are a helpful, conversational smart typing companion.")
        return try {
            response.split(",")
                .map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                .filter { it.isNotEmpty() }
                .take(3)
        } catch (e: Exception) {
            listOf("That sounds great!", "Thanks for letting me know.", "I'll get back to you soon.")
        }
    }

    // --- Rich Communication Repo Extensions ---
    val stickerPacksFlow: Flow<List<com.example.data.local.StickerPackEntity>> = appDao.getAllStickerPacks()
    val allStickersFlow: Flow<List<com.example.data.local.StickerEntity>> = appDao.getAllStickers()
    val favoriteMessagesFlow: Flow<List<MessageEntity>> = appDao.getFavoriteMessages()
    val scheduledMessagesFlow: Flow<List<com.example.data.local.ScheduledMessageEntity>> = appDao.getScheduledMessages()

    fun getPinnedMessages(partnerId: String): Flow<List<MessageEntity>> = appDao.getPinnedMessages(partnerId)
    fun searchMessages(query: String): Flow<List<MessageEntity>> = appDao.searchMessages(query)
    fun getDraft(partnerId: String): Flow<com.example.data.local.MessageDraftEntity?> = appDao.getDraft(partnerId)

    suspend fun saveDraft(partnerId: String, text: String, replyToId: Long? = null) {
        if (text.isBlank()) {
            appDao.deleteDraft(partnerId)
        } else {
            appDao.saveDraft(com.example.data.local.MessageDraftEntity(chatPartnerId = partnerId, text = text, replyToMessageId = replyToId))
        }
    }

    suspend fun clearDraft(partnerId: String) {
        appDao.deleteDraft(partnerId)
    }

    suspend fun scheduleMessage(
        partnerId: String,
        text: String,
        scheduledTimestamp: Long,
        mediaType: String = "TEXT",
        mediaUrl: String? = null
    ) {
        val user = getOrCreateCurrentUser()
        appDao.insertScheduledMessage(
            com.example.data.local.ScheduledMessageEntity(
                chatPartnerId = partnerId,
                senderId = user.id,
                senderName = user.name,
                text = text,
                mediaType = mediaType,
                mediaUrl = mediaUrl,
                scheduledTimestamp = scheduledTimestamp
            )
        )
        appDao.insertAuditLog(AuditLogEntity(action = "SCHEDULE_MESSAGE", details = "Scheduled message for partner $partnerId at $scheduledTimestamp"))
    }

    suspend fun cancelScheduledMessage(id: Long) {
        appDao.deleteScheduledMessage(id)
    }

    suspend fun togglePinMessage(msg: MessageEntity) {
        val updated = msg.copy(isPinned = !msg.isPinned)
        appDao.updateMessage(updated)
    }

    suspend fun toggleFavoriteMessage(msg: MessageEntity) {
        val updated = msg.copy(isFavorite = !msg.isFavorite)
        appDao.updateMessage(updated)
    }

    suspend fun toggleReaction(msg: MessageEntity, emoji: String) {
        val user = getOrCreateCurrentUser()
        // Simple reaction parsing/updating
        val currentReactions = msg.reactions
        val updatedReactions = if (currentReactions.contains(emoji)) {
            currentReactions.replace("$emoji:${user.id};", "").replace("$emoji:${user.id}", "")
        } else {
            "$currentReactions$emoji:${user.id};"
        }
        val updated = msg.copy(reactions = updatedReactions)
        appDao.updateMessage(updated)
    }

    suspend fun votePoll(msg: MessageEntity, optionIndex: Int) {
        val user = getOrCreateCurrentUser()
        val votesMap = try {
            if (msg.pollVotesJson.isNullOrEmpty()) mutableMapOf<Int, MutableList<String>>()
            else {
                // simple format parse e.g. "0:user1,user2|1:user3"
                val map = mutableMapOf<Int, MutableList<String>>()
                msg.pollVotesJson.split("|").forEach { entry ->
                    val parts = entry.split(":")
                    if (parts.size == 2) {
                        val idx = parts[0].toIntOrNull()
                        val voters = parts[1].split(",").filter { it.isNotEmpty() }.toMutableList()
                        if (idx != null) map[idx] = voters
                    }
                }
                map
            }
        } catch (e: Exception) {
            mutableMapOf<Int, MutableList<String>>()
        }

        val listForOpt = votesMap.getOrPut(optionIndex) { mutableListOf() }
        if (listForOpt.contains(user.id)) {
            listForOpt.remove(user.id)
        } else {
            listForOpt.add(user.id)
        }

        val newVotesJson = votesMap.entries.joinToString("|") { "${it.key}:${it.value.joinToString(",")}" }
        val updated = msg.copy(pollVotesJson = newVotesJson)
        appDao.updateMessage(updated)
    }

    suspend fun createCustomSticker(imageUrl: String, emoji: String) {
        val stickerId = "stk_custom_${System.currentTimeMillis()}"
        val packId = "pack_custom"
        // Ensure custom pack exists
        val customPack = com.example.data.local.StickerPackEntity(
            id = packId,
            title = "Mes Stickers Personnalisés ✨",
            author = "Alex Mercer",
            description = "Stickers uniques créés avec l'Éditeur Studio AI",
            category = "Personnalisé",
            thumbnailUrl = imageUrl,
            isOfficial = false,
            stickerCount = 1
        )
        appDao.insertStickerPack(customPack)
        val sticker = com.example.data.local.StickerEntity(
            id = stickerId,
            packId = packId,
            imageUrl = imageUrl,
            emoji = emoji,
            isAnimated = false,
            isCustomCreated = true
        )
        appDao.insertSticker(sticker)
        appDao.insertAuditLog(AuditLogEntity(action = "CREATE_CUSTOM_STICKER", details = "Created custom sticker in Studio"))
    }

    suspend fun analyzePostSpamAndMod(postText: String): String {
        val prompt = "Analyze the following post for spam, offensive content, or community policy violations. Return a short 1-sentence verdict on whether it is SAFE or FLAGGED with reason:\n\n$postText"
        return GeminiClient.generateText(prompt, "You are an automated community content moderation safety analyzer.")
    }

    suspend fun aiRephraseAndGrammar(text: String, mode: String): String {
        val prompt = when(mode) {
            "GRAMMAR" -> "Correct any grammatical, spelling, and punctuation errors in the following text. Preserve tone and meaning. Output ONLY the corrected text:\n\n$text"
            "PROFESSIONAL" -> "Rephrase the following message to make it extremely polite, professional, and well-structured for a business context. Output ONLY the rephrased text:\n\n$text"
            "CASUAL" -> "Rephrase the following message to make it warm, friendly, casual, and energetic. Output ONLY the rephrased text:\n\n$text"
            else -> "Improve and refine the following message for clarity and conciseness:\n\n$text"
        }
        return GeminiClient.generateText(prompt, "You are an elite communication and language refinement assistant.")
    }

    // --- PRODUCTIVITY MODULE REPOSITORY ---

    val allNotesFlow: Flow<List<NoteEntity>> = appDao.getAllNotesFlow()
    val archivedNotesFlow: Flow<List<NoteEntity>> = appDao.getArchivedNotesFlow()
    val trashedNotesFlow: Flow<List<NoteEntity>> = appDao.getTrashedNotesFlow()
    val allFoldersFlow: Flow<List<NoteFolderEntity>> = appDao.getAllFoldersFlow()
    val allTasksFlow: Flow<List<TaskEntity>> = appDao.getAllTasksFlow()
    val allCalendarEventsFlow: Flow<List<CalendarEventEntity>> = appDao.getAllCalendarEventsFlow()
    val allRemindersFlow: Flow<List<ProductivityReminderEntity>> = appDao.getAllRemindersFlow()

    suspend fun insertNote(note: NoteEntity): Long = appDao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = appDao.updateNote(note)

    suspend fun trashNote(noteId: Long) {
        val note = appDao.getNoteById(noteId) ?: return
        appDao.updateNote(note.copy(isTrashed = true, updatedAt = System.currentTimeMillis()))
    }

    suspend fun restoreNote(noteId: Long) {
        val note = appDao.getNoteById(noteId) ?: return
        appDao.updateNote(note.copy(isTrashed = false, isArchived = false, updatedAt = System.currentTimeMillis()))
    }

    suspend fun archiveNote(noteId: Long) {
        val note = appDao.getNoteById(noteId) ?: return
        appDao.updateNote(note.copy(isArchived = !note.isArchived, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleNotePin(noteId: Long) {
        val note = appDao.getNoteById(noteId) ?: return
        appDao.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleNoteFavorite(noteId: Long) {
        val note = appDao.getNoteById(noteId) ?: return
        appDao.updateNote(note.copy(isFavorite = !note.isFavorite, updatedAt = System.currentTimeMillis()))
    }

    suspend fun duplicateNote(noteId: Long) {
        val note = appDao.getNoteById(noteId) ?: return
        val duplicate = note.copy(
            id = 0,
            title = "${note.title} (Copie)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        appDao.insertNote(duplicate)
    }

    suspend fun deleteNotePermanently(noteId: Long) = appDao.deleteNotePermanently(noteId)

    suspend fun insertFolder(name: String, colorHex: String = "#2196F3"): Long {
        val folder = NoteFolderEntity(name = name, colorHex = colorHex)
        return appDao.insertFolder(folder)
    }

    suspend fun deleteFolder(folderId: Long) = appDao.deleteFolder(folderId)

    // Tasks CRUD
    suspend fun insertTask(task: TaskEntity): Long = appDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = appDao.updateTask(task)
    suspend fun deleteTask(taskId: Long) = appDao.deleteTask(taskId)

    suspend fun toggleTaskDone(taskId: Long) {
        val task = appDao.getTaskById(taskId) ?: return
        val newStatus = if (task.status == "DONE") "TODO" else "DONE"
        val newProgress = if (newStatus == "DONE") 100 else 0
        appDao.updateTask(task.copy(status = newStatus, progressPercent = newProgress, updatedAt = System.currentTimeMillis()))
    }

    // Calendar Events CRUD
    suspend fun insertCalendarEvent(event: CalendarEventEntity): Long = appDao.insertCalendarEvent(event)
    suspend fun updateCalendarEvent(event: CalendarEventEntity) = appDao.updateCalendarEvent(event)
    suspend fun deleteCalendarEvent(eventId: Long) = appDao.deleteCalendarEvent(eventId)

    // Reminders CRUD
    suspend fun insertReminder(title: String, scheduledTime: Long, targetType: String, targetId: Long): Long {
        val reminder = ProductivityReminderEntity(title = title, scheduledTime = scheduledTime, targetType = targetType, targetId = targetId)
        return appDao.insertReminder(reminder)
    }
    suspend fun deleteReminder(reminderId: Long) = appDao.deleteReminder(reminderId)

    // --- AI Productivity Intelligence Services ---

    suspend fun summarizeNoteWithAi(content: String): String {
        val prompt = "Veuillez fournir un résumé très clair, structuré et concis de la note suivante en français sous forme de puces principales:\n\n$content"
        return GeminiClient.generateText(prompt, "Vous êtes un assistant IA de synthèse de texte pour l'application WorldConnect Productivity.")
    }

    suspend fun extractTasksFromNoteWithAi(content: String): List<String> {
        val prompt = "Analysez ce texte et extrayez toutes les actions/tâches à faire. Renvoyez une liste simple de tâches (une par ligne, sans numérotation ni puce):\n\n$content"
        val response = GeminiClient.generateText(prompt, "Vous êtes un extracteur de tâches intelligent.")
        return response.lines().map { it.trim() }.filter { it.isNotBlank() }
    }

    suspend fun createEventFromNoteWithAi(noteTitle: String, noteContent: String): CalendarEventEntity {
        val prompt = "Extrayez la date, l'heure et l'intitulé d'un événement à partir du texte suivant. Donnez uniquement le titre de l'événement et une brève description:\n\n$noteTitle\n$noteContent"
        val response = GeminiClient.generateText(prompt, "Vous êtes un assistant d'agenda intelligent.")
        return CalendarEventEntity(
            title = "RDV : $noteTitle",
            description = response,
            startTime = System.currentTimeMillis() + 86400000, // Demain même heure par défaut
            endTime = System.currentTimeMillis() + 86400000 + 3600000,
            category = "Réunion"
        )
    }

    suspend fun autoCategorizeNoteWithAi(title: String, content: String): String {
        val prompt = "Classez la note suivante dans UNE seule catégorie parmi: [Travail, Personnel, Idée, Projets, Finance, Santé, Études]. Renvoyez uniquement le nom de la catégorie:\n\nTitre: $title\nTexte: $content"
        val cat = GeminiClient.generateText(prompt, "Vous êtes un classificateur de notes.").trim()
        return if (cat.length > 25) "Général" else cat
    }

    suspend fun transcribeAndSummarizeVoiceNoteWithAi(audioName: String): Pair<String, String> {
        val prompt = "Générez une transcription réaliste pour une note vocale intitulée '$audioName' enregistrée sur WorldConnect Productivity, ainsi qu'un résumé rapide. Renvoyez la transcription au début et le résumé après la ligne 'RÉSUMÉ:'."
        val response = GeminiClient.generateText(prompt, "Vous êtes un assistant de transcription et résumé audio.")
        val parts = response.split("RÉSUMÉ:", ignoreCase = true)
        val transcription = parts.getOrNull(0)?.trim() ?: "Message vocal enregistré avec succès."
        val summary = parts.getOrNull(1)?.trim() ?: "Résumé synthétique disponible."
        return Pair(transcription, summary)
    }

    // --- WorldConnect Cross-Module Integrations ---

    suspend fun convertMessageToTask(messageId: Long, category: String = "Messagerie", priority: String = "MOYENNE"): Long {
        val msg = appDao.getMessageById(messageId) ?: return 0
        val task = TaskEntity(
            title = "Tâche depuis message de ${msg.senderName}",
            description = msg.text,
            priority = priority,
            category = category,
            linkedWorldConnectType = "MESSAGE",
            linkedWorldConnectId = messageId.toString()
        )
        val id = appDao.insertTask(task)
        appDao.insertAuditLog(AuditLogEntity(action = "CONVERT_MESSAGE_TO_TASK", details = "Converted message $messageId to task $id"))
        return id
    }

    suspend fun convertMessageToNote(messageId: Long, folderId: Long? = null): Long {
        val msg = appDao.getMessageById(messageId) ?: return 0
        val note = NoteEntity(
            title = "Note créée depuis discussion avec ${msg.senderName}",
            content = msg.text,
            folderId = folderId,
            category = "Messagerie",
            linkedWorldConnectType = "MESSAGE",
            linkedWorldConnectId = messageId.toString()
        )
        val id = appDao.insertNote(note)
        appDao.insertAuditLog(AuditLogEntity(action = "CONVERT_MESSAGE_TO_NOTE", details = "Converted message $messageId to note $id"))
        return id
    }

    suspend fun shareNoteToChat(noteId: Long, chatPartnerId: String) {
        val note = appDao.getNoteById(noteId) ?: return
        val current = appDao.getCurrentUser() ?: return
        val shareMessage = MessageEntity(
            chatPartnerId = chatPartnerId,
            senderId = current.id,
            senderName = current.name,
            text = "📝 [Note partagée: ${note.title}]\n\n${note.content}",
            mediaType = "DOCUMENT",
            documentName = "${note.title}.txt"
        )
        appDao.insertMessage(shareMessage)
        appDao.insertAuditLog(AuditLogEntity(action = "SHARE_NOTE_TO_CHAT", details = "Shared note $noteId to chat $chatPartnerId"))
    }

    suspend fun shareTaskListToChat(chatPartnerId: String, tasksSummary: String) {
        val current = appDao.getCurrentUser() ?: return
        val shareMessage = MessageEntity(
            chatPartnerId = chatPartnerId,
            senderId = current.id,
            senderName = current.name,
            text = "✅ [Liste de Tâches Partagée]\n\n$tasksSummary",
            mediaType = "TEXT"
        )
        appDao.insertMessage(shareMessage)
    }

    suspend fun convertPostToNote(postId: Long): Long {
        // Fetch post or convert directly
        val note = NoteEntity(
            title = "Publication sauvegardée en Note",
            content = "Note créée depuis la publication WorldConnect #$postId.",
            category = "Social",
            linkedWorldConnectType = "POST",
            linkedWorldConnectId = postId.toString()
        )
        return appDao.insertNote(note)
    }

    suspend fun convertProductToNote(productId: Long): Long {
        val note = NoteEntity(
            title = "Article Marketplace en mémoire",
            content = "Aide-mémoire d'achat pour le produit #$productId sur le Marché WorldConnect.",
            category = "Marketplace",
            linkedWorldConnectType = "PRODUCT",
            linkedWorldConnectId = productId.toString()
        )
        return appDao.insertNote(note)
    }

    // --- CLOUD STORAGE MODULE REPOSITORY ---

    val allCloudFilesFlow: Flow<List<CloudFileEntity>> = appDao.getAllCloudFilesFlow()
    val trashedCloudFilesFlow: Flow<List<CloudFileEntity>> = appDao.getTrashedCloudFilesFlow()
    val favoriteCloudFilesFlow: Flow<List<CloudFileEntity>> = appDao.getFavoriteCloudFilesFlow()
    val allCloudFoldersFlow: Flow<List<CloudFolderEntity>> = appDao.getAllCloudFoldersFlow()
    val allSharedCloudFilesFlow: Flow<List<SharedCloudFileEntity>> = appDao.getAllSharedFilesFlow()
    val allStoragePlansFlow: Flow<List<StoragePlanEntity>> = appDao.getAllStoragePlansFlow()

    fun userQuotaFlow(userId: String = "usr_current"): Flow<UserQuotaEntity?> = appDao.getUserQuotaFlow(userId)

    suspend fun insertCloudFile(file: CloudFileEntity): Long {
        return appDao.insertCloudFile(file)
    }

    suspend fun updateCloudFile(file: CloudFileEntity) {
        appDao.updateCloudFile(file.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun trashCloudFile(fileId: Long) {
        appDao.trashCloudFile(fileId)
    }

    suspend fun restoreCloudFile(fileId: Long) {
        appDao.restoreCloudFile(fileId)
    }

    suspend fun deleteCloudFilePermanently(fileId: Long) {
        appDao.deleteCloudFilePermanently(fileId)
    }

    suspend fun toggleCloudFileFavorite(fileId: Long) {
        appDao.toggleCloudFileFavorite(fileId)
    }

    suspend fun toggleCloudFileOffline(fileId: Long) {
        appDao.toggleCloudFileOffline(fileId)
    }

    suspend fun createCloudFolder(name: String, parentFolderId: Long? = null, colorHex: String = "#2196F3"): Long {
        val folder = CloudFolderEntity(name = name, parentFolderId = parentFolderId, colorHex = colorHex)
        return appDao.insertCloudFolder(folder)
    }

    suspend fun deleteCloudFolder(folderId: Long) {
        appDao.deleteCloudFolder(folderId)
    }

    suspend fun shareFileWithUser(
        fileId: Long,
        sharedWithUserName: String,
        permissionLevel: String = "READ",
        isPasswordProtected: Boolean = false,
        expiresDays: Int = 30
    ): Long {
        val shared = SharedCloudFileEntity(
            fileId = fileId,
            sharedWithUserName = sharedWithUserName,
            permissionLevel = permissionLevel,
            shareLink = "https://cloud.worldconnect.app/share/f_$fileId",
            isPasswordProtected = isPasswordProtected,
            expiresAt = System.currentTimeMillis() + (expiresDays * 86400000L)
        )
        return appDao.insertSharedFile(shared)
    }

    suspend fun upgradeStoragePlan(userId: String = "usr_current", planId: String) {
        val currentQuota = appDao.getUserQuota(userId) ?: UserQuotaEntity(userId = userId)
        val gbMultiplier = when (planId) {
            "plus" -> 100L
            "premium" -> 1024L
            "business" -> 2048L
            "enterprise" -> 10240L
            else -> 10L
        }
        val newQuota = currentQuota.copy(
            currentPlanId = planId,
            totalQuotaBytes = gbMultiplier * 1024 * 1024 * 1024
        )
        appDao.insertOrUpdateQuota(newQuota)
    }

    suspend fun updateBackupSettings(userId: String = "usr_current", autoPhotos: Boolean, autoVideos: Boolean, autoDocs: Boolean) {
        val currentQuota = appDao.getUserQuota(userId) ?: UserQuotaEntity(userId = userId)
        val updated = currentQuota.copy(
            autoBackupPhotos = autoPhotos,
            autoBackupVideos = autoVideos,
            autoBackupDocs = autoDocs
        )
        appDao.insertOrUpdateQuota(updated)
    }

    suspend fun performOcrAndSummarizeFileWithAi(fileName: String, fileType: String): Pair<String, String> {
        val prompt = "Extraire le texte OCR et faire un résumé analytique court du document WorldConnect Cloud intitulé '$fileName' (Type: $fileType)."
        val aiResult = GeminiClient.generateText(prompt, "You are a Cloud Document AI analyst.")
        val ocrPart = "Extrait OCR: $fileName - Contenu numérisé le ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"
        val summaryPart = if (aiResult.isNotBlank()) aiResult else "Document stratégique analysé avec succès par l'intelligence artificielle Gemini."
        return Pair(ocrPart, summaryPart)
    }

    suspend fun seedInitialCloudDataIfEmpty() {
        val plans = listOf(
            StoragePlanEntity("free", "Plan Gratuit", 10, 0.0, "10 Go offerts à la création du compte WorldConnect"),
            StoragePlanEntity("plus", "WorldConnect Plus", 100, 2.99, "100 Go de stockage sécurisé + Sauvegarde automatique"),
            StoragePlanEntity("premium", "WorldConnect Premium", 1024, 9.99, "1 To de stockage + IA Gemini OCR & Recherche Illimitée"),
            StoragePlanEntity("business", "Business Pro", 2048, 19.99, "2 To + Collaboration d'équipe en temps réel"),
            StoragePlanEntity("enterprise", "Entreprise Sur-Mesure", 10240, 49.99, "10 To + Chiffrement matériel dédié & Support VIP 24/7")
        )
        appDao.insertStoragePlans(plans)

        val defaultQuota = UserQuotaEntity(
            userId = "usr_current",
            currentPlanId = "free",
            totalQuotaBytes = 10L * 1024 * 1024 * 1024,
            usedBytes = 3L * 1024 * 1024 * 1024 + 200 * 1024 * 1024
        )
        appDao.insertOrUpdateQuota(defaultQuota)

        // Seed demo folders if empty
        val foldersFlow = appDao.getAllCloudFoldersFlow().firstOrNull()
        if (foldersFlow.isNullOrEmpty()) {
            val f1 = CloudFolderEntity(name = "Documents Pro", colorHex = "#2196F3")
            val f2 = CloudFolderEntity(name = "Photos & Médias", colorHex = "#E91E63")
            val f3 = CloudFolderEntity(name = "Sauvegardes WorldConnect", colorHex = "#4CAF50")
            val f1Id = appDao.insertCloudFolder(f1)
            val f2Id = appDao.insertCloudFolder(f2)
            val f3Id = appDao.insertCloudFolder(f3)

            // Seed demo files
            appDao.insertCloudFile(
                CloudFileEntity(
                    name = "Rapport_Financier_2026.pdf",
                    fileType = "PDF",
                    extension = "pdf",
                    sizeBytes = 14500000L,
                    folderId = f1Id,
                    storageUrl = "https://cloud.worldconnect.app/files/doc1.pdf",
                    isFavorite = true,
                    aiSummary = "Analyse financière du T2 2026 indiquant une croissance de 28% sur l'écosystème WorldConnect.",
                    ocrText = "WORLDCONNECT CORP - REPORT Q2 2026"
                )
            )
            appDao.insertCloudFile(
                CloudFileEntity(
                    name = "Photo_Communaute_Marseille.jpg",
                    fileType = "IMAGE",
                    extension = "jpg",
                    sizeBytes = 6200000L,
                    folderId = f2Id,
                    storageUrl = "https://cloud.worldconnect.app/files/img1.jpg",
                    isFavorite = true
                )
            )
            appDao.insertCloudFile(
                CloudFileEntity(
                    name = "Sauvegarde_Messagerie_20260721.zip",
                    fileType = "ZIP",
                    extension = "zip",
                    sizeBytes = 2800000000L,
                    folderId = f3Id,
                    storageUrl = "https://cloud.worldconnect.app/files/backup1.zip",
                    isOfflineAvailable = true
                )
            )
        }
    }
}


