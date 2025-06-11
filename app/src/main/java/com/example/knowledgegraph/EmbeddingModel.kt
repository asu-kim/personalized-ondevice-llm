package com.example.knowledgegraph

import android.content.Context
import android.util.Log
import ai.onnxruntime.*
import java.io.File

class EmbeddingModel(private val context: Context) {
    private lateinit var session: OrtSession
    private lateinit var env: OrtEnvironment
    private val tokenizer = TokenizerLoader.getInstance(context)

    init {
        try {
            env = OrtEnvironment.getEnvironment()
            val modelPath = copyAssetToCache("all-minilm-l6-v2/model.onnx")
            session = env.createSession(modelPath, OrtSession.SessionOptions())
        } catch (e: Exception) {
            Log.e("EmbeddingModel", "Failed to load model: ${e.message}")
        }
    }

    private fun copyAssetToCache(assetPath: String): String {
        val file = File(context.cacheDir, File(assetPath).name)
        if (!file.exists()) {
            context.assets.open(assetPath).use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        return file.absolutePath
    }

    fun getEmbedding(text: String): FloatArray? {
        val (inputIds, attentionMask) = tokenizer.encode(text)

        val inputIds2D = arrayOf(inputIds)
        val attentionMask2D = arrayOf(attentionMask)
        val tokenTypeIds2D = arrayOf(LongArray(inputIds.size) { 0L }) // Added this line

        val inputIdTensor = OnnxTensor.createTensor(env, inputIds2D)
        val attentionMaskTensor = OnnxTensor.createTensor(env, attentionMask2D)
        val tokenTypeTensor = OnnxTensor.createTensor(env, tokenTypeIds2D) // Added this line

        val inputs = mapOf(
            "input_ids" to inputIdTensor,
            "attention_mask" to attentionMaskTensor,
            "token_type_ids" to tokenTypeTensor // Added this line
        )

        val results = session.run(inputs)
        val output3D = results[0].value as Array<Array<FloatArray>>
        val clsEmbedding = output3D[0][0] // [batch][CLS token]
        return clsEmbedding
    }
    fun generateEmbeddingsFromKG(): String {
        val kgFile = File(context.getExternalFilesDir(null), "Knowledge_graph.csv")
        val vecFile = File(context.getExternalFilesDir(null), "Knowledge_graph.vec")

        if (!kgFile.exists()) {
            Log.e("EmbeddingKG", "Knowledge_graph.csv not found!")
            return "KG CSV file not found!"
        }

        // Skip regeneration if .vec is up-to-date
        if (vecFile.exists() && vecFile.lastModified() >= kgFile.lastModified()) {
            Log.d("EmbeddingKG", ".vec file is up-to-date. Skipping regeneration.")
            return "Embeddings already up-to-date."
        }

        val lines = kgFile.readLines().filter { it.isNotBlank() }
        var appendedCount = 0
        vecFile.writeText("") // Clear previous contents

        for (line in lines) {
            val parts = Regex("""(?:[^,"']+|"(?:\\.|[^"])*")+""").findAll(line).map { it.value }.toList()
            if (parts.size < 3) continue

            val subject = parts[0].trim('"')
            val predicate = parts[1].trim('"')
            val obj = parts.subList(2, parts.size).joinToString(" ").trim('"')

            val text = "$subject $predicate $obj"
            val embedding = getEmbedding(text)

            if (embedding != null) {
                val embeddingStr = embedding.joinToString(",")
                vecFile.appendText("$embeddingStr\n")
                appendedCount++
            }
        }

        val message = "Generated $appendedCount embeddings to: ${vecFile.name}"
        Log.d("EmbeddingKG", message)
        return message
    }
}