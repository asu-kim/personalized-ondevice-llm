package com.example.knowledgegraph


// UI imports
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext

// App + MLC LLM
import android.util.Log
import java.io.File
import ai.mlc.mlcchat.MLCChatModule

@Composable
fun CustomLLM() {
    val context = LocalContext.current
    var modelOutput by remember { mutableStateOf("Loading...") }

    LaunchedEffect(Unit) {
        try {
            val csvFile = File(context.filesDir, "Knowledge_graph.csv")
            val csvText = csvFile.readText()
            val userQuestion = "What is on Sat May 09 17:00:00 PDT 2026?"

            val prompt = """
                Using the following knowledge graph:
                $csvText
                
                $userQuestion
            """.trimIndent()

            val modelPath = File(context.filesDir, "models/Llama-3.2-3B-Instruct-q4f16_0-MLC").absolutePath
            val configPath = File(context.filesDir, "mlc-app-config.json").absolutePath

            val chatModule = MLCChatModule.load(modelPath, configPath)
            chatModule.resetChat()

            chatModule.generate(prompt) { output ->
                Log.d("MLC", "Model Output: $output")
                modelOutput = output
            }
        } catch (e: Exception) {
            modelOutput = "Error: ${e.localizedMessage}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "MLC LLM Output:",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = modelOutput,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}