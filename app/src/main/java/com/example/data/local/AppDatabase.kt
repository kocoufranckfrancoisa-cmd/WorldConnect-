package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        PostEntity::class,
        CommentEntity::class,
        MessageEntity::class,
        ProductEntity::class,
        AuditLogEntity::class,
        GameRoomEntity::class,
        GameMatchEntity::class,
        TournamentEntity::class,
        GameStatisticEntity::class,
        AchievementEntity::class,
        StickerPackEntity::class,
        StickerEntity::class,
        MessageDraftEntity::class,
        ScheduledMessageEntity::class,
        NoteEntity::class,
        NoteFolderEntity::class,
        TaskEntity::class,
        CalendarEventEntity::class,
        ProductivityReminderEntity::class,
        CloudFileEntity::class,
        CloudFolderEntity::class,
        FileVersionEntity::class,
        SharedCloudFileEntity::class,
        StoragePlanEntity::class,
        UserQuotaEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "worldconnect_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
