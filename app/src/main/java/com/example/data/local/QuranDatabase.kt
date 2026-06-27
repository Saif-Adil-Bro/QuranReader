package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.dao.MemorizedPageDao
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.MemorizedPageEntity

@Database(
    entities = [BookmarkEntity::class, MemorizedPageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun memorizedPageDao(): MemorizedPageDao

    companion object {
        @Volatile
        private var INSTANCE: QuranDatabase? = null

        fun getDatabase(context: Context): QuranDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuranDatabase::class.java,
                    "quran_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
