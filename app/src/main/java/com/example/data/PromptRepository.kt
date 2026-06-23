package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class PromptRepository(
    private val promptDao: PromptDao,
    private val geminiService: GeminiService = GeminiService()
) {
    val allPrompts: Flow<List<Prompt>> = promptDao.getAllPrompts()
    val bookmarkedPrompts: Flow<List<Prompt>> = promptDao.getBookmarkedPrompts()
    val myCreations: Flow<List<MyCreation>> = promptDao.getMyCreations()

    fun getPromptsByCategory(category: String): Flow<List<Prompt>> {
        return promptDao.getPromptsByCategory(category)
    }

    suspend fun insertPrompt(prompt: Prompt) = withContext(Dispatchers.IO) {
        promptDao.insertPrompts(listOf(prompt))
    }

    suspend fun updatePrompt(prompt: Prompt) = withContext(Dispatchers.IO) {
        promptDao.updatePrompt(prompt)
    }

    suspend fun createAiPrompt(userConcept: String, targetEngine: String): String = withContext(Dispatchers.IO) {
        val refined = geminiService.generatePrompt(userConcept, targetEngine)
        // Auto-save the user's generated prompt to creations
        promptDao.insertMyCreation(
            MyCreation(
                title = if (userConcept.length > 25) userConcept.take(22) + "..." else userConcept,
                category = targetEngine,
                promptText = refined
            )
        )
        refined
    }

    suspend fun syncLiveTrends(): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonString = geminiService.syncTrendingPrompts()
            if (jsonString.isNotEmpty()) {
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
                val adapter = moshi.adapter<Map<String, Any>>(type)
                val parsed = adapter.fromJson(jsonString)
                val promptsList = parsed?.get("prompts") as? List<Map<String, Any>>
                if (promptsList != null) {
                    val newPrompts = promptsList.mapNotNull { map ->
                        try {
                            Prompt(
                                id = map["id"] as? String ?: "gen_${System.currentTimeMillis()}_${map["title"].hashCode()}",
                                title = map["title"] as? String ?: "Trending Prompt",
                                category = map["category"] as? String ?: "Trending",
                                trendScore = (map["trendScore"] as? Double ?: map["trendScore"] as? Int ?: 94).toString().toDoubleOrNull()?.toInt() ?: 94,
                                promptText = map["promptText"] as? String ?: "",
                                tags = map["tags"] as? String ?: "ai, viral",
                                isPremium = map["isPremium"] as? Boolean ?: false,
                                isBookmarked = false,
                                timestamp = System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (newPrompts.isNotEmpty()) {
                        promptDao.insertPrompts(newPrompts)
                        return@withContext true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext false
    }

    suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existing = promptDao.getAllPrompts().first()
        if (existing.isEmpty()) {
            val seedList = listOf(
                Prompt(
                    id = "seed_1",
                    title = "Ultra Realistic Rain Portrait",
                    category = "Cinematic Portraits",
                    trendScore = 98,
                    promptText = "A cinematic ultra realistic portrait of a young man standing in heavy rain, dramatic lighting, detailed skin texture, shallow depth of field, 8K photography, masterpiece.",
                    tags = "cinematic, realistic, rain, dark, portrait, male",
                    isPremium = false,
                    timestamp = System.currentTimeMillis()
                ),
                Prompt(
                    id = "seed_2",
                    title = "Ghibli Magic Forest Cottage",
                    category = "Ghibli Style",
                    trendScore = 96,
                    promptText = "Whimsical studio ghibli anime style illustration of a magical cottage tucked inside a glowing enchanted forest, ancient massive trees with moss, floating bioluminescent dust particles, warm light glowing from the windows, detailed, masterpiece.",
                    tags = "ghibli, anime, magical, cottage, forest, glowing, illustration",
                    isPremium = false,
                    timestamp = System.currentTimeMillis() - 600000
                ),
                Prompt(
                    id = "seed_3",
                    title = "Hyperrealistic Neon Glass Sneaker",
                    category = "Product Photography",
                    trendScore = 97,
                    promptText = "Commercial product photography of a floating futuristic sneaker crafted from glowing transparent glass, carbon fiber, and fiber optics. Splashes of neon water, professional studio lighting, clean dark background, 8k, photorealistic.",
                    tags = "product, sneaker, photorealistic, luxury, light, neon",
                    isPremium = true,
                    timestamp = System.currentTimeMillis() - 1200000
                ),
                Prompt(
                    id = "seed_4",
                    title = "Deep Work Cognitive Framework",
                    category = "Gemini",
                    trendScore = 95,
                    promptText = "Act as an elite executive cognitive coach. Help me structure a 4-hour deep work block today. I need to complete a complex programming module. Provide a structured hour-by-hour breakdown utilizing high-efficiency focus cycles, micro-breaks, and cognitive offloading protocols.",
                    tags = "chatgpt, gemini, productivity, planning, executive, study",
                    isPremium = false,
                    timestamp = System.currentTimeMillis() - 1800000
                ),
                Prompt(
                    id = "seed_5",
                    title = "Cyberpunk Neo-Tokyo Alley",
                    category = "Midjourney",
                    trendScore = 99,
                    promptText = "A vertical wide-angle cinematic shot of a narrow cyber-street in Neo-Tokyo, pouring rain, glowing neon signs reflecting on puddles, hovering delivery drone passing by, highly detailed cyberpunk aesthetics, Unreal Engine 5 render, cinematic lighting.",
                    tags = "midjourney, cyberpunk, street, neon, dark, realistic, futuristic",
                    isPremium = true,
                    timestamp = System.currentTimeMillis() - 2400000
                ),
                Prompt(
                    id = "seed_6",
                    title = "Instagram Tech Reels Hook",
                    category = "Instagram Viral",
                    trendScore = 94,
                    promptText = "Generate 5 highly addictive, clickbait-style but professional hooks for a 15-second tech content Instagram Reel about 'AI tools you did not know existed'. Each hook must target creators and students, triggering curiosity and a sense of urgency. Include text overlay suggestions and b-roll visual descriptions.",
                    tags = "instagram, reels, viral, tech, video, writing, chatgpt",
                    isPremium = false,
                    timestamp = System.currentTimeMillis() - 3000000
                ),
                Prompt(
                    id = "seed_7",
                    title = "Minimalist Cyberpunk Vector Logo",
                    category = "Logo Design",
                    trendScore = 92,
                    promptText = "A minimalist cyberpunk style vector logo of a wolf's head, sharp geometric lines, neon cyan on a pure black background, flat design, modern tech startup style, scalable graphic, masterpiece.",
                    tags = "logo, vector, minimalist, cyberpunk, flat, design",
                    isPremium = false,
                    timestamp = System.currentTimeMillis() - 3600000
                ),
                Prompt(
                    id = "seed_8",
                    title = "Cinematic Dune Nomad Portrait",
                    category = "Cinematic Portraits",
                    trendScore = 93,
                    promptText = "A cinematic medium shot of a desert nomad with glowing blue eyes, wearing a weathered linen hood, standing amidst towering sand dunes, strong sun backlight, flying dust motes, anamorphic lens flare, inspired by Dune, 8k.",
                    tags = "portrait, desert, cinematic, movie, light, detailed",
                    isPremium = true,
                    timestamp = System.currentTimeMillis() - 4200000
                )
            )
            promptDao.insertPrompts(seedList)
        }
    }
}
