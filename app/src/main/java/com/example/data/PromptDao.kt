package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts ORDER BY timestamp DESC")
    fun getAllPrompts(): Flow<List<Prompt>>

    @Query("SELECT * FROM prompts WHERE category = :category ORDER BY timestamp DESC")
    fun getPromptsByCategory(category: String): Flow<List<Prompt>>

    @Query("SELECT * FROM prompts WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedPrompts(): Flow<List<Prompt>>

    @Query("SELECT * FROM prompts WHERE id = :id LIMIT 1")
    suspend fun getPromptById(id: String): Prompt?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompts(prompts: List<Prompt>)

    @Update
    suspend fun updatePrompt(prompt: Prompt)

    @Query("SELECT * FROM my_creations ORDER BY timestamp DESC")
    fun getMyCreations(): Flow<List<MyCreation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyCreation(creation: MyCreation)

    @Delete
    suspend fun deleteMyCreation(creation: MyCreation)

    @Query("DELETE FROM prompts")
    suspend fun clearAllPrompts()
}
