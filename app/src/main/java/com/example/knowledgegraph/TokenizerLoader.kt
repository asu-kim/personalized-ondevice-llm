package com.example.knowledgegraph

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class TokenizerLoader private constructor(context: Context) {

    private val vocab: Map<String, Int> = loadVocab(context)

    companion object {
        @Volatile
        private var instance: TokenizerLoader? = null

        fun getInstance(context: Context): TokenizerLoader {
            return instance ?: synchronized(this) {
                instance ?: TokenizerLoader(context.applicationContext).also { instance = it }
            }
        }
    }

    private fun loadVocab(context: Context): Map<String, Int> {
        val reader = BufferedReader(InputStreamReader(context.assets.open("all-minilm-l6-v2/vocab.txt")))
        val vocab = mutableMapOf<String, Int>()
        var index = 0
        reader.useLines { lines ->
            lines.forEach { token ->
                vocab[token.trim()] = index++
            }
        }
        return vocab
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }
    }

    private fun convertTokensToIds(tokens: List<String>): List<Int> {
        return tokens.map { token -> vocab[token] ?: vocab["[UNK]"] ?: 100 }
    }

    fun encode(text: String, maxLen: Int = 128): Pair<LongArray, LongArray> {
        val tokens = tokenize(text).take(maxLen - 2)
        val inputTokens = listOf("[CLS]") + tokens + listOf("[SEP]")
        val inputIds = convertTokensToIds(inputTokens)

        val paddedInputIds = inputIds + List(maxLen - inputIds.size) { 0 }
        val attentionMask = List(inputIds.size) { 1L } + List(maxLen - inputIds.size) { 0L }

        return Pair(
            paddedInputIds.map { it.toLong() }.toLongArray(),
            attentionMask.toLongArray()
        )
    }
}