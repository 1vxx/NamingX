package com.v.namingx.service

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.v.namingx.AppSettings
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

data class Suggestion(val name: String, val explanation: String)

class GeneratorService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    fun generateVariables(description: String, onSuccess: (List<Suggestion>) -> Unit, onError: (String) -> Unit) {
        val settings = AppSettings.instance
        val apiKey = settings.apiKey
        val apiUrl = settings.apiUrl
        val model = settings.modelName
        val namingConvention = settings.namingConvention

        if (apiKey.isBlank()) {
            onError("Please set OpenAI API Key in Settings.")
            return
        }

        if (apiUrl.isBlank()) {
            onError("Please set API URL in Settings.")
            return
        }

        if (model.isBlank()) {
            onError("Please set Model Name in Settings.")
            return
        }

        val promptBuilder = StringBuilder()
        promptBuilder.append("You are a witty and helpful coding assistant. Generate 5 distinct, best-practice variable names for this description: \"$description\".\n")
        promptBuilder.append("The variable names MUST follow the \"$namingConvention\" naming convention.\n")
        promptBuilder.append("For each name, provide a **humorous and brief explanation in Simplified Chinese** (简体中文) of why this name is good or what \"vibe\" it has (keep it short, max 1 sentence).\n")
        promptBuilder.append("The explanation MUST be witty, concise, and professional, with subtle programmer humor. Avoid clichés.\n")
        promptBuilder.append("Output ONLY a JSON array of objects, like this: [{\"name\": \"var1\", \"explanation\": \"Short and snappy!\"}]. Do not output markdown code blocks.\n")

        val prompt = promptBuilder.toString().trimIndent()

        val jsonBody = JsonObject().apply {
            addProperty("model", model)
            add("messages", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "system")
                    addProperty("content", "You are a variable name generator. Return clean JSON.")
                })
                add(JsonObject().apply {
                    addProperty("role", "user")
                    addProperty("content", prompt)
                })
            })
            addProperty("temperature", 0.7)
        }

        val request = Request.Builder()
            .url(apiUrl)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        // Async call
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Network Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onError("API Error: ${response.code} ${response.message}")
                    return
                }

                val bodyStr = response.body?.string()
                if (bodyStr == null) {
                    onError("Empty response body")
                    return
                }

                try {
                    val root = JsonParser.parseString(bodyStr).asJsonObject
                    val content = root.getAsJsonArray("choices")
                        .get(0).asJsonObject
                        .getAsJsonObject("message")
                        .get("content").asString.trim()

                    // Clean up potential markdown formatting if the model disobeys
                    val cleanContent = content.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

                    val suggestions = gson.fromJson(cleanContent, Array<Suggestion>::class.java).toList()
                    onSuccess(suggestions)
                } catch (e: Exception) {
                    onError("Parse Error: ${e.message}")
                }
            }
        })
    }

    fun testConnection(apiKey: String, apiUrl: String, model: String, callback: (Boolean, String) -> Unit) {
        if (apiKey.isBlank()) {
            callback(false, "API Key 不能为空")
            return
        }
        if (apiUrl.isBlank()) {
            callback(false, "API URL 不能为空")
            return
        }

        val testModel = if (model.isBlank()) "gpt-3.5-turbo" else model

        val jsonBody = JsonObject().apply {
            addProperty("model", testModel)
            add("messages", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "user")
                    addProperty("content", "Hello")
                })
            })
            addProperty("max_tokens", 1)
        }

        val request = Request.Builder()
            .url(apiUrl)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "连接失败: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback(true, "连接成功！")
                } else {
                    callback(false, "API 错误: ${response.code} ${response.message}")
                }
                response.close()
            }
        })
    }
}
