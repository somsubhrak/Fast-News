package com.example.fastnews

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class NewsContentRepository {

    private val generativeModel = GenerativeModel(
        // Corrected the model name to a stable, available version
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.5f
            maxOutputTokens = 4000
        }
    )

    suspend fun summarizeUrl(url: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Scrape the article text from the URL
                Log.d("NewsContentRepository", "Scraping URL: $url")
                val document = Jsoup.connect(url).get()
                val articleText = document.select("p").text()

                if (articleText.isBlank()) {
                    return@withContext "Error: Could not extract article content from the URL."
                }

                // 2. Summarize the extracted text
                Log.d("NewsContentRepository", "Summarizing with model: ${generativeModel.modelName}")
                val safeText = articleText.take(8000) // Avoid token limits

                val prompt = """
                    Summarize the following news article in 2–3 clear and concise sentences.
                    Keep the summary factual and neutral.

                    Article:
                    $safeText
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                response.text ?: "Summary is currently unavailable."
            } catch (e: Exception) {
                Log.e("NewsContentRepository", "Error fetching or summarizing content", e)
                "Error: ${e.localizedMessage}"
            }
        }
    }
}
