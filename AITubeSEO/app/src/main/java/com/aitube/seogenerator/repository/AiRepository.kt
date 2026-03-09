package com.aitube.seogenerator.repository

import com.aitube.seogenerator.models.*
import com.aitube.seogenerator.network.RetrofitClient
import com.aitube.seogenerator.utils.Constants
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException

class AiRepository {

    private val api = RetrofitClient.apiService

    // Custom exception to carry HTTP code through the fallback logic
    private class ApiException(val code: Int, msg: String) : Exception(msg)

    // ── Public API ────────────────────────────────────────────────────────────

    suspend fun generateSeoContent(topic: String): Result<SeoContent> {
        return callWithFallback { model ->
            val request = CerebrasRequest(
                model = model,
                maxTokens = Constants.MAX_TOKENS,
                temperature = 0.7,
                messages = listOf(
                    ChatMessage(
                        role = "system",
                        content = "You are a professional YouTube SEO expert who helps creators rank videos and increase CTR."
                    ),
                    ChatMessage(role = "user", content = buildSeoPrompt(topic))
                )
            )

            val response = api.generateContent(request)

            if (response.isSuccessful) {
                val raw = response.body()?.choices?.firstOrNull()?.message?.content
                    ?: throw Exception("AI returned an empty response. Please try again.")
                Result.success(parseSeo(raw, topic))
            } else {
                throw ApiException(response.code(), httpError(response.code()))
            }
        }
    }

    suspend fun generateShortsTitles(topic: String): Result<ShortsTitles> {
        return callWithFallback { model ->
            val request = CerebrasRequest(
                model = model,
                maxTokens = Constants.MAX_TOKENS,
                temperature = 0.7,
                messages = listOf(
                    ChatMessage(
                        role = "system",
                        content = "You are a viral YouTube Shorts expert. Respond ONLY with valid compact JSON."
                    ),
                    ChatMessage(role = "user", content = buildShortsPrompt(topic))
                )
            )

            val response = api.generateContent(request)

            if (response.isSuccessful) {
                val raw = response.body()?.choices?.firstOrNull()?.message?.content
                    ?: throw Exception("AI returned an empty response. Please try again.")
                Result.success(parseShorts(raw))
            } else {
                throw ApiException(response.code(), httpError(response.code()))
            }
        }
    }

    // ── Fallback logic ────────────────────────────────────────────────────────

    private suspend fun <T> callWithFallback(
        block: suspend (model: String) -> Result<T>
    ): Result<T> {
        return try {
            block(Constants.CEREBRAS_MODEL)
        } catch (e: CancellationException) {
            throw e
        } catch (e: ApiException) {
            if (e.code == 404) {
                try {
                    block(Constants.CEREBRAS_MODEL_FALLBACK)
                } catch (e2: CancellationException) {
                    throw e2
                } catch (e2: ApiException) {
                    Result.failure(Exception(e2.message))
                } catch (e2: Exception) {
                    Result.failure(Exception(friendlyError(e2)))
                }
            } else {
                Result.failure(Exception(e.message))
            }
        } catch (e: Exception) {
            Result.failure(Exception(friendlyError(e)))
        }
    }

    // ── Prompts ───────────────────────────────────────────────────────────────

    private fun buildSeoPrompt(topic: String) = """
        Generate YouTube SEO optimized content for this video topic.

        Topic: $topic

        Return ONLY JSON (no markdown, no explanations):

        {
        "title":"...",
        "description":"...",
        "tags":"...",
        "hashtags":"..."
        }

        Rules:
        - Title must be highly clickable and SEO optimized (max 70 characters)
        - Description must be YouTube style with keywords and call-to-action
        - Description should include engaging intro + keywords + CTA
        - Tags must be exactly 20 SEO keywords separated by commas
        - Hashtags must be exactly 15 hashtags separated by commas
        - Focus on YouTube search ranking and CTR
    """.trimIndent()

    private fun buildShortsPrompt(topic: String) = """
        Generate 10 viral YouTube Shorts titles for: $topic

        Rules: max 60 chars each, emotional hooks, curiosity gaps, emojis, high CTR.

        Return ONLY this JSON (no markdown):
        {"titles":["title1","title2","title3","title4","title5","title6","title7","title8","title9","title10"]}
    """.trimIndent()

    // ── Parsers ───────────────────────────────────────────────────────────────

    private fun parseSeo(raw: String, topic: String): SeoContent {
        return try {
            val obj = JsonParser.parseString(cleanJson(raw)).asJsonObject
            SeoContent(
                title = obj.get("title")?.asString?.take(200).orEmpty(),
                description = obj.get("description")?.asString.orEmpty(),
                tags = obj.get("tags")?.asString.orEmpty(),
                hashtags = obj.get("hashtags")?.asString.orEmpty()
            )
        } catch (e: Exception) {
            SeoContent(title = "SEO for: $topic", description = raw.take(2000))
        }
    }

    private fun parseShorts(raw: String): ShortsTitles {
        return try {
            val arr = JsonParser.parseString(cleanJson(raw))
                .asJsonObject.getAsJsonArray("titles")
            val titles = arr?.mapNotNull { it?.asString?.trim() }
                ?.filter { it.isNotEmpty() } ?: emptyList()

            if (titles.isEmpty()) ShortsTitles(listOf("Could not parse titles. Try again."))
            else ShortsTitles(titles)

        } catch (e: Exception) {
            ShortsTitles(listOf("Parse error. Please try again."))
        }
    }

    private fun cleanJson(raw: String) =
        raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

    // ── Error helpers ─────────────────────────────────────────────────────────

    private fun httpError(code: Int) = when (code) {
        401 -> "Invalid API key. Please contact support."
        403 -> "API access denied. Check your API key."
        404 -> "AI model not found (404). Retrying with fallback model..."
        422 -> "Invalid request. Please try again."
        429 -> "Rate limit exceeded. Please wait a moment."
        500, 502, 503 -> "AI server unavailable. Please try again."
        524 -> "Request timed out. Please retry."
        else -> "Request failed (Error $code). Please try again."
    }

    private fun friendlyError(e: Exception): String {
        val msg = e.message?.lowercase().orEmpty()
        return when {
            "unable to resolve host" in msg || "failed to connect" in msg ||
            "network" in msg -> "No internet connection. Please check your network."
            "timeout" in msg || "timed out" in msg -> "Request timed out. Please retry."
            "ssl" in msg || "certificate" in msg -> "Secure connection failed. Please try again."
            else -> e.message ?: "Unexpected error. Please try again."
        }
    }
}
