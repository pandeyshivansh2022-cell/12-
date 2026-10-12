package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.VisualCreation

@Database(
    entities = [
        ChatSession::class,
        ChatMessage::class,
        VisualCreation::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GangstreamDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun visualDao(): VisualDao

    companion object {
        @Volatile
        private var INSTANCE: GangstreamDatabase? = null

        fun getInstance(context: Context): GangstreamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GangstreamDatabase::class.java,
                    "gangstream_ai.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
