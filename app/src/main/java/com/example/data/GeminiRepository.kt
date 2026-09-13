package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GeminiRepository {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val service = RetrofitClient.service

    suspend fun generateText(prompt: String, model: String = "gemini-3.5-flash", highThinking: Boolean = false, systemInstruction: String? = null): String {
        return kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                val generationConfig = if (highThinking) {
                    GenerationConfig(thinkingConfig = ThinkingConfig("high"))
                } else null
                
                val sysInstContent = systemInstruction?.let { Content(parts = listOf(Part(text = it))) }
                
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = generationConfig,
                    systemInstruction = sysInstContent
                )
                
                val response = service.generateContent(model, apiKey, request)
                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response"
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }
    }

    suspend fun generateImage(prompt: String, imageSize: String = "1K"): String {
        return kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(
                        imageConfig = ImageConfig(aspectRatio = "1:1", imageSize = imageSize),
                        responseModalities = listOf("TEXT", "IMAGE")
                    )
                )
                val response = service.generateContent("gemini-3-pro-image-preview", apiKey, request)
                
                // Parse the inline image data base64
                val candidates = response.candidates
                val parts = candidates?.firstOrNull()?.content?.parts
                val inlineData = parts?.firstOrNull { it.inlineData != null }?.inlineData
                inlineData?.data ?: throw Exception("No image generated")
            } catch (e: Exception) {
                throw e
            }
        }
    }
    
    suspend fun generateChat(history: List<Content>, systemInstruction: String? = null, model: String = "gemini-3.1-flash-lite-preview"): String {
        return kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                val sysInstContent = systemInstruction?.let { Content(parts = listOf(Part(text = it))) }
                val request = GenerateContentRequest(
                    contents = history,
                    systemInstruction = sysInstContent
                )
                val response = service.generateContent(model, apiKey, request)
                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response"
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }
    }
}
