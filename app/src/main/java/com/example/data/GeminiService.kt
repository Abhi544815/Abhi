package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiService {
    suspend fun generatePrompt(userConcept: String, targetEngine: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return getFallbackRefinement(userConcept, targetEngine)
        }

        val prompt = "Refine and expand the following short concept into a highly descriptive, professional, viral AI prompt optimized for $targetEngine. Maintain the core essence but inject vivid details, artistic styling, camera angles, lighting conditions, or specialized parameters matching best practices for $targetEngine. Only return the final prompt text. Concept: $userConcept"

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = Content(parts = listOf(Part(text = "You are an expert prompt engineer specializing in crafting elite, viral AI prompts.")))
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: getFallbackRefinement(userConcept, targetEngine)
        } catch (e: Exception) {
            getFallbackRefinement(userConcept, targetEngine)
        }
    }

    suspend fun syncTrendingPrompts(): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return ""
        }

        val systemPrompt = "You are an automated AI trend analysis engine. Generate 4 ultra-creative, viral AI prompts. " +
                "Each prompt must be returned in raw JSON matching this schema: " +
                "{\"prompts\": [{\"id\": \"unique_id\", \"title\": \"Title\", \"category\": \"CategoryName\", \"trendScore\": 95, \"promptText\": \"The full prompt content.\", \"tags\": \"comma,separated,tags\", \"isPremium\": false}]} " +
                "Choose categories from: ChatGPT, Gemini, Midjourney, Flux, Instagram Viral, YouTube Content, Logo Design, Product Photography, Cinematic Portraits, Anime. " +
                "Ensure titles and descriptions are incredibly cool, futuristic, trendy, and social media appealing. Output ONLY the JSON."

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = "Generate the trending prompts JSON now.")))),
            generationConfig = GenerationConfig(temperature = 0.9f, responseMimeType = "application/json"),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun getFallbackRefinement(userConcept: String, targetEngine: String): String {
        return "A cinematic ultra-realistic rendering of '$userConcept' optimized for $targetEngine, featuring breathtaking volumetric lighting, detailed ray-traced textures, 8k resolution, shallow depth of field, dramatic color grading, highly trending on ArtStation."
    }
}
