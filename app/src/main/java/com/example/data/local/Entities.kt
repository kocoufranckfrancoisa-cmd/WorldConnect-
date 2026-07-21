package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val bio: String,
    val avatarUrl: String,
    val coverUrl: String,
    val isCurrentUser: Boolean = false,
    val isSpamCandidate: Boolean = false,
    val isBlocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String,
    val contentText: String,
    val imageUrl: String? = null,
    val likesCount: Int = 0,
    val hasLiked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val isReported: Boolean = false,
    val reportReason: String? = null,
    val category: String = "General"
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val authorName: String,
    val authorAvatarUrl: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatPartnerId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val isVoice: Boolean = false,
    val voiceDurationSec: Int = 0,
    val status: String = "SENT", // SENT, RECEIVED, READ
    val isAiGenerated: Boolean = false,
    
    // Rich Communication Module extensions
    val replyToMessageId: Long? = null,
    val replyToText: String? = null,
    val reactions: String = "", // JSON string of reactions map, e.g. {"👍":["user_me"]}
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val mediaType: String = "TEXT", // TEXT, STICKER, GIF, VOICE, IMAGE, VIDEO, DOCUMENT, LOCATION, CONTACT, POLL, ALBUM
    val mediaUrl: String? = null,
    val documentName: String? = null,
    val documentSize: String? = null,
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val locationAddress: String? = null,
    val isLiveLocation: Boolean = false,
    val liveLocationDurationMinutes: Int? = null,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val contactAvatar: String? = null,
    val pollQuestion: String? = null,
    val pollOptionsJson: String? = null, // e.g. ["Option A", "Option B"]
    val pollVotesJson: String? = null,   // e.g. {"0":["user_me"], "1":["user_jean"]}
    val pollIsAnonymous: Boolean = false,
    val pollIsClosed: Boolean = false,
    val albumUrlsJson: String? = null    // e.g. ["url1", "url2", "url3"]
)

@Entity(tableName = "sticker_packs")
data class StickerPackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val description: String,
    val category: String,
    val language: String = "fr",
    val thumbnailUrl: String,
    val version: String = "1.0",
    val isOfficial: Boolean = true,
    val isPremium: Boolean = false,
    val stickerCount: Int = 12
)

@Entity(tableName = "stickers")
data class StickerEntity(
    @PrimaryKey val id: String,
    val packId: String,
    val imageUrl: String,
    val emoji: String = "😊",
    val isAnimated: Boolean = false,
    val isFavorite: Boolean = false,
    val isCustomCreated: Boolean = false
)

@Entity(tableName = "message_drafts")
data class MessageDraftEntity(
    @PrimaryKey val chatPartnerId: String,
    val text: String,
    val replyToMessageId: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scheduled_messages")
data class ScheduledMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatPartnerId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val mediaType: String = "TEXT",
    val mediaUrl: String? = null,
    val scheduledTimestamp: Long,
    val isSent: Boolean = false
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val rating: Float = 4.5f,
    val reviewsCount: Int = 12,
    val category: String,
    val sellerId: String,
    val sellerName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val details: String,
    val targetUser: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_rooms")
data class GameRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val gameType: String, // "Échecs", "Dames", "Othello", "Gomoku", "Puissance 4", "Tic-Tac-Toe", "Domino", "Backgammon", "Awalé"
    val hostId: String,
    val hostName: String,
    val isPrivate: Boolean = false,
    val accessCode: String? = null,
    val currentPlayers: Int = 1,
    val maxPlayers: Int = 2,
    val isRanked: Boolean = true,
    val region: String = "Mondial",
    val status: String = "WAITING", // WAITING, IN_PROGRESS, FINISHED
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_matches")
data class GameMatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameType: String,
    val player1Name: String,
    val player2Name: String,
    val winnerName: String,
    val movesHistory: String, // JSON or formatted text of moves
    val isAiOpponent: Boolean = false,
    val eloChange: Int = 15,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val gameType: String,
    val prizePool: String,
    val playersCount: Int = 8,
    val maxPlayers: Int = 16,
    val status: String = "REGISTRATION", // REGISTRATION, IN_PROGRESS, COMPLETED
    val winnerName: String? = null,
    val startDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_statistics")
data class GameStatisticEntity(
    @PrimaryKey val gameType: String,
    val eloRating: Int = 1200,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val winStreak: Int = 0,
    val trophiesCount: Int = 0
)

@Entity(tableName = "game_achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

// --- Productivity Module Entities ---

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val folderId: Long? = null,
    val category: String = "Général",
    val colorHex: String = "#3F51B5",
    val tagsJson: String = "[]", // e.g. ["Travail", "Idée"]
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isTrashed: Boolean = false,
    val isLocked: Boolean = false,
    val passwordPin: String? = null,
    val drawingsJson: String? = null,
    val voiceNoteUrl: String? = null,
    val voiceTranscription: String? = null,
    val voiceSummary: String? = null,
    val attachmentsJson: String? = null, // e.g. [{"name":"doc.pdf", "type":"PDF", "url":""}]
    val linkedWorldConnectType: String? = null, // "MESSAGE", "POST", "PRODUCT"
    val linkedWorldConnectId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "note_folders")
data class NoteFolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentFolderId: Long? = null,
    val colorHex: String = "#2196F3",
    val iconName: String = "folder",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: String = "MOYENNE", // ELEVEE, MOYENNE, BASSE, URGENTE
    val category: String = "Général",
    val dueDate: Long? = null,
    val dueTime: String? = null,
    val hasReminder: Boolean = false,
    val repeatFrequency: String = "NONE", // NONE, DAILY, WEEKLY, MONTHLY
    val status: String = "TODO", // TODO, IN_PROGRESS, DONE
    val progressPercent: Int = 0,
    val parentTaskId: Long? = null,
    val linkedNoteId: Long? = null,
    val linkedWorldConnectType: String? = null,
    val linkedWorldConnectId: String? = null,
    val subtasksJson: String? = null, // e.g. [{"id":1,"title":"Étape 1","isDone":true}]
    val commentsJson: String? = null, // e.g. [{"author":"Jean","text":"In progress"}]
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis() + 3600000,
    val isAllDay: Boolean = false,
    val location: String = "",
    val videoCallUrl: String? = null,
    val colorHex: String = "#4CAF50",
    val category: String = "Réunion", // Réunion, Rendez-vous, Anniversaire, Tâche
    val participantsJson: String = "[]",
    val repeatRule: String = "NONE",
    val hasReminder: Boolean = true,
    val linkedNoteId: Long? = null,
    val linkedWorldConnectType: String? = null,
    val linkedWorldConnectId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "productivity_reminders")
data class ProductivityReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val scheduledTime: Long,
    val targetType: String = "TASK", // TASK, EVENT, NOTE, COMMUNITY
    val targetId: Long = 0,
    val isFired: Boolean = false,
    val repeatRule: String = "NONE"
)

// --- Cloud Storage Module Entities ---

@Entity(tableName = "cloud_files")
data class CloudFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val fileType: String = "DOC", // IMAGE, VIDEO, PDF, DOC, AUDIO, ZIP, OTHER
    val extension: String = "",
    val sizeBytes: Long = 0,
    val folderId: Long? = null,
    val storageUrl: String = "",
    val localPath: String? = null,
    val isFavorite: Boolean = false,
    val isTrashed: Boolean = false,
    val trashedAt: Long? = null,
    val isOfflineAvailable: Boolean = false,
    val ocrText: String? = null,
    val aiSummary: String? = null,
    val tagsJson: String = "[]",
    val linkedModule: String? = null, // CHAT, SOCIAL, MARKETPLACE, NOTE, GAME
    val linkedEntityId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cloud_folders")
data class CloudFolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentFolderId: Long? = null,
    val colorHex: String = "#2196F3",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cloud_file_versions")
data class FileVersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileId: Long,
    val versionNumber: Int,
    val sizeBytes: Long,
    val changeNote: String = "Mise à jour automatique",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shared_cloud_files")
data class SharedCloudFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileId: Long,
    val sharedWithUserName: String,
    val permissionLevel: String = "READ", // READ, COMMENT, EDIT
    val shareLink: String? = null,
    val isPasswordProtected: Boolean = false,
    val expiresAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "storage_plans")
data class StoragePlanEntity(
    @PrimaryKey val planId: String, // free, plus, premium, business, enterprise
    val name: String,
    val quotaGb: Int,
    val priceMonthlyEur: Double,
    val description: String
)

@Entity(tableName = "user_quotas")
data class UserQuotaEntity(
    @PrimaryKey val userId: String,
    val currentPlanId: String = "free",
    val totalQuotaBytes: Long = 10L * 1024 * 1024 * 1024, // 10 GB
    val usedBytes: Long = 2L * 1024 * 1024 * 1024 + 400 * 1024 * 1024, // default ~2.4GB used for demo
    val autoBackupPhotos: Boolean = true,
    val autoBackupVideos: Boolean = false,
    val autoBackupDocs: Boolean = true
)



