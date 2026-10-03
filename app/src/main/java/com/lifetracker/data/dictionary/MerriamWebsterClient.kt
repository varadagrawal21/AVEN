package com.lifetracker.data.dictionary

import com.lifetracker.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import javax.inject.Inject


data class DictionaryEntry(
    val word: String,
    val meaning: String,
    val exampleSentence: String,
    val partOfSpeech: String,
    val pronunciationAudioUrl: String
)

class MerriamWebsterClient @Inject constructor() {
    suspend fun lookup(word: String): Result<DictionaryEntry> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.MW_DICTIONARY_API_KEY
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Add mwDictionaryApiKey to gradle.properties to enable dictionary lookup.")
            )
        }

        val encodedWord = URLEncoder.encode(word.trim(), Charsets.UTF_8.name())
        val url = URL(
            "https://www.dictionaryapi.com/api/v3/references/collegiate/json/" +
                "$encodedWord?key=$apiKey"
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
        }

        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                return@withContext Result.failure(IOException("Dictionary request failed ($responseCode)."))
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            parseEntry(response, word.trim())
        } catch (exception: Exception) {
            Result.failure(exception)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseEntry(response: String, requestedWord: String): Result<DictionaryEntry> {
        val results = JSONArray(response)
        if (results.length() == 0 || results.opt(0) !is JSONObject) {
            return Result.failure(NoSuchElementException("No dictionary entry found for $requestedWord."))
        }

        val entry = results.getJSONObject(0)
        val meanings = entry.optJSONArray("shortdef") ?: JSONArray()
        if (meanings.length() == 0) {
            return Result.failure(NoSuchElementException("No definition found for $requestedWord."))
        }

        val pronunciation = entry.optJSONObject("hwi")
            ?.optJSONArray("prs")
            ?.let { it.optJSONObject(0) }
        val audioFile = pronunciation?.optJSONObject("sound")?.optString("audio").orEmpty()

        return Result.success(
            DictionaryEntry(
                word = requestedWord,
                meaning = (0 until meanings.length()).joinToString("; ") { meanings.optString(it) },
                exampleSentence = extractExample(entry),
                partOfSpeech = entry.optString("fl"),
                pronunciationAudioUrl = buildAudioUrl(audioFile)
            )
        )
    }

    private fun extractExample(entry: JSONObject): String {
        val senses = entry.optJSONArray("def")?.optJSONObject(0)
            ?.optJSONArray("sseq") ?: return ""
        for (index in 0 until senses.length()) {
            val senseGroup = senses.optJSONArray(index) ?: continue
            for (groupIndex in 0 until senseGroup.length()) {
                val sense = senseGroup.optJSONArray(groupIndex)?.optJSONObject(1) ?: continue
                val examples = sense.optJSONArray("dt") ?: continue
                for (exampleIndex in 0 until examples.length()) {
                    val item = examples.optJSONArray(exampleIndex) ?: continue
                    if (item.optString(0) == "vis") {
                        return item.optJSONObject(1)?.optString("t").orEmpty()
                    }
                }
            }
        }
        return ""
    }

    private fun buildAudioUrl(audioFile: String): String {
        if (audioFile.isBlank()) return ""
        val directory = when {
            audioFile.startsWith("bix") -> "bix"
            audioFile.startsWith("gg") -> "gg"
            audioFile.startsWith("mwa") -> "mwa"
            audioFile.first().isDigit() -> "number"
            else -> audioFile.first().lowercaseChar().toString()
        }
        return "https://media.merriam-webster.com/audio/prons/en/us/mp3/$directory/$audioFile.mp3"
    }
}
