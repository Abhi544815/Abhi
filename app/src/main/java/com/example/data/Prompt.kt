package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompts")
data class Prompt(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val trendScore: Int,
    val promptText: String,
    val isBookmarked: Boolean = false,
    val isPremium: Boolean = false,
    val tags: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val voteCount: Int = 0
)

@Entity(tableName = "my_creations")
data class MyCreation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val promptText: String,
    val timestamp: Long = System.currentTimeMillis()
)
