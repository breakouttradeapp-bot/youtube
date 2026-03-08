package com.aitube.seogenerator.repository

import com.aitube.seogenerator.models.*
import com.aitube.seogenerator.network.RetrofitClient
import com.aitube.seogenerator.utils.Constants
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException

class AiRepository {

    private val api = RetrofitClient.apiService

    suspend fun generateSeoContent(topic: String): Result<SeoContent> {
        return try {
            val request = CerebrasRequest(
                model = Constants.CEREBRAS_MODEL,
                maxTokens = Constants.MAX_TOKENS,
                messages = listOf(
                    ChatMessage("system",
                        "You are a YouTube SEO expert. Respond ONLY with valid compact JSON. No markdown, no explanation."),
                    ChatMessage("user", buildSeoPrompt(topic))
                )
            )

            val response = api.generateContent("Bearer ${Constants.CEREBRAS_API_KEY}", request)

            if (response.isSuccessful) {
                val raw = response.body()?.choices
                    ?.firstOrNull()?.message?.content
                    ?: return Result.failure(Exception("AI returned an empty response. Please try again."))
                Result.success(parseSeo(raw, topic))
            } else {
                Result.failure(Exception(httpError(response.code())))
            }
        } catch (e: CancellationException) {
            // Always re-throw coroutine cancellation
            throw e
        } catch (e: Exception) {
            Result.failure(Exception(friendlyError(e)))
        }
    }

    suspend fun generateShortsTitles(topic: String): Result<ShortsTitles> {
        return try {
            val request = CerebrasRequest(
                model = Constants.CEREBRAS_MODEL,
                maxTokens = Constants.MAX_TOKENS,
                messages = listOf(
                    ChatMessage("system",
                        "You are a viral YouTube Shorts expert. Respond ONLY with valid compact JSON."),
                    ChatMessage("user", buildShortsPrompt(topic))
                )
            )

            val response = api.generateContent("Bearer ${Constants.CEREBRAS_API_KEY}", request)

            if (response.isSuccessful) {
                val raw = response.body()?.choices
                    ?.firstOrNull()?.message?.content
                    ?: return Result.failure(Exception("AI returned an empty response. Please try again."))
                Result.success(parseShorts(raw))
            } else {
                Result.failure(Exception(httpError(response.code())))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception(friendlyError(e)))
        }
    }

    // ── Prompt builders ──────────────────────────────────────
    private fun buildSeoPrompt(topic: String) = """
        Generate YouTube SEO content for this topic:
        Topic: $topic
        
        Return ONLY this JSON (no markdown, no extra text):
        {"title":"...","description":"...","tags":"tag1, tag2, tag3","hashtags":"#h1, #h2, #h3"}
        
        Rules:
        - title: max 70 chars, keyword-rich
        - description: 300-500 words, natural keywords, call to action
        - tags: exactly 20 tags, comma separated, no # symbol
        - hashtags: exactly 15 hashtags, with # symbol, comma separated
    """.trimIndent()

    private fun buildShortsPrompt(topic: String) = """
        Generate 10 viral YouTube Shorts titles for: $topic
        
        Rules: max 60 chars each, emotional hooks, curiosity gaps, emojis, high CTR.
        
        Return ONLY this JSON (no markdown):
        {"titles":["title1","title2","title3","title4","title5","title6","title7","title8","title9","title10"]}
    """.trimIndent()

    // ── Parsers ──────────────────────────────────────────────
    private fun parseSeo(raw: String, topic: String): SeoContent {
        val cleaned = cleanJson(raw)
        return try {
            val obj = JsonParser.parseString(cleaned).asJsonObject
            SeoContent(
                title = obj.get("title")?.asString?.take(200).orEmpty(),
                description = obj.get("description")?.asString.orEmpty(),
                tags = obj.get("tags")?.asString.orEmpty(),
                hashtags = obj.get("hashtags")?.asString.orEmpty()
            )
        } catch (e: JsonParseException) {
            // Graceful fallback: return whatever the AI said as description
            SeoContent(
                title = "SEO content for: $topic",
                description = raw.take(2000),
                tags = "",
                hashtags = ""
            )
        } catch (e: Exception) {
            SeoContent(title = "SEO content for: $topic", description = raw.take(500))
        }
    }

    private fun parseShorts(raw: String): ShortsTitles {
        val cleaned = cleanJson(raw)
        return try {
            val arr = JsonParser.parseString(cleaned)
                .asJsonObject
                .getAsJsonArray("titles")
            val titles = mutableListOf<String>()
            arr?.forEach { el ->
                val s = el?.asString?.trim()
                if (!s.isNullOrEmpty()) titles.add(s)
            }
            if (titles.isEmpty()) {
                ShortsTitles(titles = listOf("Could not parse titles. Please try again."))
            } else {
                ShortsTitles(titles = titles)
            }
        } catch (e: Exception) {
            ShortsTitles(titles = listOf("Generation error. Please try again."))
        }
    }

    private fun cleanJson(raw: String): String =
        raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

    // ── Error messages ────────────────────────────────────────
    private fun httpError(code: Int) = when (code) {
        401 -> "Invalid API key. Please contact support."
        403 -> "Access denied. Please check your API key."
        429 -> "Rate limit exceeded. Please wait a moment and try again."
        500, 502, 503 -> "AI server is temporarily unavailable. Please try again."
        524 -> "Request timed out. The AI is busy — please retry."
        else -> "Request failed (Error $code). Please try again."
    }

    private fun friendlyError(e: Exception): String {
        val msg = e.message?.lowercase() ?: ""
        return when {
            "unable to resolve host" in msg || "failed to connect" in msg ||
            "network" in msg || "nodename nor servname" in msg ->
                "No internet connection. Please check your network and try again."
            "timeout" in msg || "timed out" in msg ->
                "Request timed out. Please check your connection and retry."
            "ssl" in msg || "certificate" in msg ->
                "Secure connection failed. Please try again."
            "json" in msg || "parse" in msg ->
                "Received unexpected response from AI. Please try again."
            else -> e.message ?: "An unexpected error occurred. Please try again."
        }
    }
}
