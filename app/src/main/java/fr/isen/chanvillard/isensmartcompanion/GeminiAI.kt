package fr.isen.chanvillard.isensmartcompanion

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiAI {
    private const val API_KEY = "AIzaSyDyrL1QqsgY6zBBR7Wb3Gl_0E1onoY1tc4"

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = API_KEY
    )

    suspend fun generateResponse(question: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = model.generateContent(question)
                response.text ?: "Je n'ai pas compris votre question."
            } catch (e: Exception) {
                "Erreur: ${e.localizedMessage}"
            }
        }
    }
}