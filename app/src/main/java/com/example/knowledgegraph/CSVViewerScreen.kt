package com.example.knowledgegraph

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CSVViewerScreen(navController: NavController) {
    val context = LocalContext.current
    var triples by remember { mutableStateOf<List<KnowledgeTriple>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val file = File(context.getExternalFilesDir(null), "Knowledge_graph.csv")
        if (file.exists()) {
            val list = mutableListOf<KnowledgeTriple>()
            file.readLines().drop(1).forEach { line ->
                // Simple CSV parsing, similar to KnowledgeBase.kt
                val parts = Regex("""(?:[^,"']+|"(?:\\.|[^"])*")+""").findAll(line)
                    .map { it.value.trim('"') }
                    .toList()
                if (parts.size >= 3) {
                    list.add(KnowledgeTriple(parts[0], parts[1], parts[2]))
                }
            }
            triples = list
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Knowledge Graph Viewer") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (triples.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No data found in Knowledge_graph.csv")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(triples) { triple ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row {
                                Text("S: ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(triple.subject)
                            }
                            Row {
                                Text("P: ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Text(triple.predicate)
                            }
                            Row {
                                Text("O: ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                Text(triple.obj)
                            }
                        }
                    }
                }
            }
        }
    }
}
