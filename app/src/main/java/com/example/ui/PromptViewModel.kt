package com.example.ui

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MyCreation
import com.example.data.Prompt
import com.example.data.PromptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InAppNotification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val isRead: Boolean = false
)

class PromptViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = PromptRepository(database.promptDao())

    // UI state streams
    val allPrompts: StateFlow<List<Prompt>> = repository.allPrompts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedPrompts: StateFlow<List<Prompt>> = repository.bookmarkedPrompts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myCreations: StateFlow<List<MyCreation>> = repository.myCreations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selection states
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val selectedEngine = MutableStateFlow("Midjourney") // For generator screen
    
    // Premium and Monetization states
    val isPremiumUser = MutableStateFlow(false)
    val showUpgradeDialog = MutableStateFlow(false)

    // User streaks
    val streakCount = MutableStateFlow(3) // Seed with a streak of 3 days
    val hasClaimedStreakToday = MutableStateFlow(false)

    // Async states
    val isSyncing = MutableStateFlow(false)
    val aiOptimizing = MutableStateFlow(false)
    val aiOptimizedResult = MutableStateFlow("")

    // Notification states
    val unreadNotificationCount = MutableStateFlow(2)
    val notifications = MutableStateFlow(
        listOf(
            InAppNotification(
                "1",
                "🔥 Viral Prompt Alert",
                "The 'Ultra Realistic Rain Portrait' prompt is currently blowing up on Instagram!",
                "10m ago"
            ),
            InAppNotification(
                "2",
                "🆕 Trend Detected",
                "A major flux of Ghibli-Style Anime illustrations was spotted in viral Reels today.",
                "2h ago"
            ),
            InAppNotification(
                "3",
                "💡 Weekly Top Prompts",
                "Explore the curated list of top performing Midjourney cinematic parameters.",
                "1d ago"
            )
        )
    )

    // Filtered Feed using combination of allPrompts, selectedCategory and searchQuery
    val filteredPrompts: StateFlow<List<Prompt>> = combine(
        allPrompts,
        selectedCategory,
        searchQuery
    ) { prompts, category, query ->
        prompts.filter { prompt ->
            val matchesCategory = category == "All" || prompt.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isEmpty() ||
                    prompt.title.contains(query, ignoreCase = true) ||
                    prompt.promptText.contains(query, ignoreCase = true) ||
                    prompt.tags.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recommended Prompts (tailored to user's favorite category, or highest score)
    val recommendedPrompts: StateFlow<List<Prompt>> = allPrompts
        .combine(bookmarkedPrompts) { prompts, bookmarked ->
            // If user has saved prompts, recommend from their saved categories. Otherwise recommend the top trend scores.
            val favoredCategories = bookmarked.map { it.category }.distinct()
            prompts.filter { prompt ->
                if (favoredCategories.isNotEmpty()) {
                    favoredCategories.contains(prompt.category) && !prompt.isBookmarked
                } else {
                    prompt.trendScore >= 96
                }
            }.take(4)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Seed database with mock viral prompts on startup
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // Toggle Bookmarking (saving) with monetization logic
    fun toggleBookmark(context: Context, prompt: Prompt) {
        viewModelScope.launch {
            if (prompt.isBookmarked) {
                // Remove bookmark
                val updated = prompt.copy(isBookmarked = false)
                repository.updatePrompt(updated)
                Toast.makeText(context, "Removed from Saved Prompts", Toast.LENGTH_SHORT).show()
            } else {
                // Add bookmark: Check Free save limit
                val currentSavedCount = bookmarkedPrompts.value.size
                if (!isPremiumUser.value && currentSavedCount >= 5) {
                    showUpgradeDialog.value = true
                } else {
                    val updated = prompt.copy(isBookmarked = true)
                    repository.updatePrompt(updated)
                    Toast.makeText(context, "Saved to collections!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Sync Live trends using Gemini
    fun syncTrends(context: Context) {
        if (isSyncing.value) return
        isSyncing.value = true
        viewModelScope.launch {
            val success = repository.syncLiveTrends()
            isSyncing.value = false
            if (success) {
                Toast.makeText(context, "✨ Hourly Trends Synced with Gemini!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "✨ Simulated Hourly Trends Updated!", Toast.LENGTH_LONG).show()
                // Insert a dynamic trending prompt manually to ensure user sees progress even without key
                val id = "dyn_${System.currentTimeMillis()}"
                val dynamicPrompt = Prompt(
                    id = id,
                    title = "Retro 3D Cyber Sneaker",
                    category = "Product Photography",
                    trendScore = 99,
                    promptText = "A high-end studio product shoot of a modular futuristic runner sneaker glowing with internal neon wires, liquid chrome accents, volumetric mist background, Hasselblad 100MP photography.",
                    tags = "sneaker, neon, realistic, product, luxury",
                    isPremium = false,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertPrompt(dynamicPrompt)
            }
        }
    }

    // Generate/Refine Prompt with Gemini AI
    fun generateRefinedPrompt(context: Context, concept: String) {
        if (concept.trim().isEmpty()) {
            Toast.makeText(context, "Please enter a core prompt concept!", Toast.LENGTH_SHORT).show()
            return
        }
        aiOptimizing.value = true
        aiOptimizedResult.value = ""
        viewModelScope.launch {
            val result = repository.createAiPrompt(concept, selectedEngine.value)
            aiOptimizedResult.value = result
            aiOptimizing.value = false
        }
    }

    // Claim daily streak
    fun claimDailyStreak(context: Context) {
        if (hasClaimedStreakToday.value) return
        streakCount.value += 1
        hasClaimedStreakToday.value = true
        Toast.makeText(context, "🔥 Claimed today's streak! Streak level: ${streakCount.value} days!", Toast.LENGTH_SHORT).show()
    }

    // Vote on prompts
    fun voteOnPrompt(context: Context, prompt: Prompt) {
        viewModelScope.launch {
            val updated = prompt.copy(voteCount = prompt.voteCount + 1)
            repository.updatePrompt(updated)
            Toast.makeText(context, "Voted! Current votes: ${updated.voteCount}", Toast.LENGTH_SHORT).show()
        }
    }

    // Purchase Premium
    fun upgradeToPremium(context: Context) {
        isPremiumUser.value = true
        showUpgradeDialog.value = false
        Toast.makeText(context, "👑 Upgraded to Trend Setter PREMIUM!", Toast.LENGTH_LONG).show()
    }

    // Clear notifications
    fun markAllNotificationsRead() {
        unreadNotificationCount.value = 0
        notifications.value = notifications.value.map { it.copy(isRead = true) }
    }
}
