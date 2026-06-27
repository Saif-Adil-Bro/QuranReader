package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String, // "SURAH", "PAGE", "JUZ"
    val referenceId: Int, // Surah number, Page number, or Juz number
    val name: String, // e.g. "Surah Al-Fatihah" or "Page 1"
    val timestamp: Long = System.currentTimeMillis()
)
