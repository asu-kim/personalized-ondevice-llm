//package com.example.knowledgegraph
//import androidx.lifecycle.ViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//
//class GmailViewModel : ViewModel() {
//    private val _snippets = MutableStateFlow<List<String>>(emptyList())
//    val snippets: StateFlow<List<String>> = _snippets
//
//    fun setSnippets(data: List<String>) {
//        _snippets.value = data
//    }
//}