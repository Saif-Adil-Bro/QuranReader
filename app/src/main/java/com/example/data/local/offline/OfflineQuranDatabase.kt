package com.example.data.local.offline

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SurahEntity::class, AyahEntity::class], version = 1, exportSchema = false)
abstract class OfflineQuranDatabase : RoomDatabase() {
    abstract fun offlineQuranDao(): OfflineQuranDao

    companion object {
        @Volatile
        private var INSTANCE: OfflineQuranDatabase? = null

        fun getDatabase(context: Context): OfflineQuranDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        private fun buildDatabase(context: Context): OfflineQuranDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                OfflineQuranDatabase::class.java,
                "offline_quran_database_v8"
            )
            .createFromAsset("databases/quran.db")
            .fallbackToDestructiveMigration()
            .build()
        }

        suspend fun ensureDatabasePopulated(context: Context): OfflineQuranDatabase {
            val db = getDatabase(context)
            try {
                val testAyah = db.offlineQuranDao().getAyahByGlobalNumber(1)
                if (testAyah == null) {
                    synchronized(this) {
                        try {
                            db.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        try {
                            val dbFile = context.applicationContext.getDatabasePath("offline_quran_database_v8")
                            if (dbFile.exists()) {
                                dbFile.delete()
                            }
                            val shmFile = context.applicationContext.getDatabasePath("offline_quran_database_v8-shm")
                            if (shmFile.exists()) shmFile.delete()
                            val walFile = context.applicationContext.getDatabasePath("offline_quran_database_v8-wal")
                            if (walFile.exists()) walFile.delete()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        val newInstance = buildDatabase(context)
                        INSTANCE = newInstance
                        return newInstance
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return db
        }
    }
}
