package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- User Profiles ---
    @Query("SELECT * FROM user_profiles WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE isCurrentUser = 0 ORDER BY name ASC")
    fun getAllContactsFlow(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles ORDER BY name ASC")
    suspend fun getAllUsers(): List<UserProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfileEntity)

    @Update
    suspend fun updateUser(user: UserProfileEntity)

    // --- Social Posts ---
    @Query("SELECT * FROM posts WHERE isReported = 0 ORDER BY timestamp DESC")
    fun getFeedPostsChronological(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE isReported = 1 ORDER BY timestamp DESC")
    fun getReportedPosts(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: Long)

    @Query("UPDATE posts SET isReported = 1, reportReason = :reason WHERE id = :postId")
    suspend fun reportPost(postId: Long, reason: String)

    // --- Comments ---
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    // --- Messages ---
    @Query("SELECT * FROM messages WHERE chatPartnerId = :chatPartnerId ORDER BY timestamp ASC")
    fun getMessagesWithPartner(chatPartnerId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Long): MessageEntity?

    @Query("SELECT * FROM messages WHERE chatPartnerId = :chatPartnerId AND isPinned = 1 ORDER BY timestamp DESC")
    fun getPinnedMessages(chatPartnerId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE text LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(query: String): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET status = 'READ' WHERE chatPartnerId = :chatPartnerId AND senderId = :chatPartnerId")
    suspend fun markMessagesAsRead(chatPartnerId: String)

    // --- Stickers & Drafts & Scheduled ---
    @Query("SELECT * FROM sticker_packs")
    fun getAllStickerPacks(): Flow<List<StickerPackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStickerPack(pack: StickerPackEntity)

    @Query("SELECT * FROM stickers WHERE packId = :packId")
    fun getStickersByPack(packId: String): Flow<List<StickerEntity>>

    @Query("SELECT * FROM stickers")
    fun getAllStickers(): Flow<List<StickerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSticker(sticker: StickerEntity)

    @Query("SELECT * FROM message_drafts WHERE chatPartnerId = :chatPartnerId LIMIT 1")
    fun getDraft(chatPartnerId: String): Flow<MessageDraftEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: MessageDraftEntity)

    @Query("DELETE FROM message_drafts WHERE chatPartnerId = :chatPartnerId")
    suspend fun deleteDraft(chatPartnerId: String)

    @Query("SELECT * FROM scheduled_messages WHERE isSent = 0 ORDER BY scheduledTimestamp ASC")
    fun getScheduledMessages(): Flow<List<ScheduledMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledMessage(msg: ScheduledMessageEntity)

    @Query("DELETE FROM scheduled_messages WHERE id = :id")
    suspend fun deleteScheduledMessage(id: Long)

    // --- Marketplace ---
    @Query("SELECT * FROM products ORDER BY timestamp DESC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProduct(productId: Long)

    // --- Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // --- WorldConnect Games ---
    @Query("SELECT * FROM game_rooms ORDER BY timestamp DESC")
    fun getAllGameRooms(): Flow<List<GameRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameRoom(room: GameRoomEntity)

    @Query("DELETE FROM game_rooms WHERE id = :roomId")
    suspend fun deleteGameRoom(roomId: Long)

    @Query("SELECT * FROM game_matches ORDER BY timestamp DESC LIMIT 50")
    fun getRecentMatches(): Flow<List<GameMatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameMatch(match: GameMatchEntity)

    @Query("SELECT * FROM tournaments ORDER BY startDate DESC")
    fun getAllTournaments(): Flow<List<TournamentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity)

    @Query("SELECT * FROM game_statistics")
    fun getAllGameStats(): Flow<List<GameStatisticEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGameStat(stat: GameStatisticEntity)

    @Query("SELECT * FROM game_achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    // --- Productivity Module: Notes ---
    @Query("SELECT * FROM notes WHERE isTrashed = 0 AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotesFlow(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isTrashed = 0 AND isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedNotesFlow(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isTrashed = 1 ORDER BY updatedAt DESC")
    fun getTrashedNotesFlow(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE folderId = :folderId AND isTrashed = 0 ORDER BY updatedAt DESC")
    fun getNotesByFolderFlow(folderId: Long): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNotePermanently(id: Long)

    // --- Productivity Module: Folders ---
    @Query("SELECT * FROM note_folders ORDER BY name ASC")
    fun getAllFoldersFlow(): Flow<List<NoteFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: NoteFolderEntity): Long

    @Query("DELETE FROM note_folders WHERE id = :id")
    suspend fun deleteFolder(id: Long)

    // --- Productivity Module: Tasks ---
    @Query("SELECT * FROM tasks ORDER BY status ASC, priority DESC, dueDate ASC, updatedAt DESC")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    // --- Productivity Module: Calendar Events ---
    @Query("SELECT * FROM calendar_events ORDER BY startTime ASC")
    fun getAllCalendarEventsFlow(): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE startTime >= :startMs AND endTime <= :endMs ORDER BY startTime ASC")
    fun getCalendarEventsInRangeFlow(startMs: Long, endMs: Long): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE id = :id LIMIT 1")
    suspend fun getCalendarEventById(id: Long): CalendarEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarEvent(event: CalendarEventEntity): Long

    @Update
    suspend fun updateCalendarEvent(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteCalendarEvent(id: Long)

    // --- Productivity Module: Reminders ---
    @Query("SELECT * FROM productivity_reminders ORDER BY scheduledTime ASC")
    fun getAllRemindersFlow(): Flow<List<ProductivityReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ProductivityReminderEntity): Long

    @Query("DELETE FROM productivity_reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)

    // --- Cloud Storage Module: Files ---
    @Query("SELECT * FROM cloud_files WHERE isTrashed = 0 ORDER BY updatedAt DESC")
    fun getAllCloudFilesFlow(): Flow<List<CloudFileEntity>>

    @Query("SELECT * FROM cloud_files WHERE isTrashed = 1 ORDER BY trashedAt DESC")
    fun getTrashedCloudFilesFlow(): Flow<List<CloudFileEntity>>

    @Query("SELECT * FROM cloud_files WHERE isFavorite = 1 AND isTrashed = 0 ORDER BY updatedAt DESC")
    fun getFavoriteCloudFilesFlow(): Flow<List<CloudFileEntity>>

    @Query("SELECT * FROM cloud_files WHERE folderId = :folderId AND isTrashed = 0 ORDER BY updatedAt DESC")
    fun getCloudFilesByFolderFlow(folderId: Long?): Flow<List<CloudFileEntity>>

    @Query("SELECT * FROM cloud_files WHERE id = :id LIMIT 1")
    suspend fun getCloudFileById(id: Long): CloudFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCloudFile(file: CloudFileEntity): Long

    @Update
    suspend fun updateCloudFile(file: CloudFileEntity)

    @Query("DELETE FROM cloud_files WHERE id = :id")
    suspend fun deleteCloudFilePermanently(id: Long)

    @Query("UPDATE cloud_files SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleCloudFileFavorite(id: Long)

    @Query("UPDATE cloud_files SET isTrashed = 1, trashedAt = :trashedAt WHERE id = :id")
    suspend fun trashCloudFile(id: Long, trashedAt: Long = System.currentTimeMillis())

    @Query("UPDATE cloud_files SET isTrashed = 0, trashedAt = NULL WHERE id = :id")
    suspend fun restoreCloudFile(id: Long)

    @Query("UPDATE cloud_files SET isOfflineAvailable = NOT isOfflineAvailable WHERE id = :id")
    suspend fun toggleCloudFileOffline(id: Long)

    // --- Cloud Storage Module: Folders ---
    @Query("SELECT * FROM cloud_folders ORDER BY name ASC")
    fun getAllCloudFoldersFlow(): Flow<List<CloudFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCloudFolder(folder: CloudFolderEntity): Long

    @Query("DELETE FROM cloud_folders WHERE id = :id")
    suspend fun deleteCloudFolder(id: Long)

    // --- Cloud Storage Module: Shared Files ---
    @Query("SELECT * FROM shared_cloud_files ORDER BY createdAt DESC")
    fun getAllSharedFilesFlow(): Flow<List<SharedCloudFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedFile(shared: SharedCloudFileEntity): Long

    // --- Cloud Storage Module: Storage Quota & Plans ---
    @Query("SELECT * FROM user_quotas WHERE userId = :userId LIMIT 1")
    fun getUserQuotaFlow(userId: String): Flow<UserQuotaEntity?>

    @Query("SELECT * FROM user_quotas WHERE userId = :userId LIMIT 1")
    suspend fun getUserQuota(userId: String): UserQuotaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateQuota(quota: UserQuotaEntity)

    @Query("SELECT * FROM storage_plans")
    fun getAllStoragePlansFlow(): Flow<List<StoragePlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoragePlans(plans: List<StoragePlanEntity>)

    // --- Cloud Storage Module: File Versions ---
    @Query("SELECT * FROM cloud_file_versions WHERE fileId = :fileId ORDER BY versionNumber DESC")
    fun getFileVersionsFlow(fileId: Long): Flow<List<FileVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFileVersion(version: FileVersionEntity): Long
}


