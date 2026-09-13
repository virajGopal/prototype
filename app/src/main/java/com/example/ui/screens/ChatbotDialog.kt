package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Content
import com.example.data.GeminiRepository
import com.example.data.Part
import kotlinx.coroutines.launch

@Composable
fun ChatbotDialog(onDismiss: () -> Unit) {
    val repository = remember { GeminiRepository() }
    val coroutineScope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    val history = remember { mutableStateListOf<Content>() }
    
    // Initial greeting
    LaunchedEffect(Unit) {
        if (history.isEmpty()) {
            history.add(Content(role = "model", parts = listOf(Part(text = "Hello! I am your AI assistant. How can I help you manage your classroom today?"))))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AI Assistant", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Divider()

                // Messages
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    reverseLayout = true
                ) {
                    items(history.reversed()) { message ->
                        val isUser = message.role == "user"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Text(
                                    text = message.parts.firstOrNull()?.text ?: "",
                                    modifier = Modifier.padding(12.dp),
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask something...") },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (input.isNotBlank()) {
                                val userText = input
                                history.add(Content(role = "user", parts = listOf(Part(text = userText))))
                                input = ""
                                
                                coroutineScope.launch {
                                    val currentHistory = history.toList()
                                    // Add placeholder for model response
                                    history.add(Content(role = "model", parts = listOf(Part(text = "..."))))
                                    val modelIndex = history.lastIndex
                                    
                                    val response = repository.generateChat(
                                        history = currentHistory,
                                        systemInstruction = "You are an AI assistant helping a teacher manage their classroom. Be concise, polite, and helpful."
                                    )
                                    history[modelIndex] = Content(role = "model", parts = listOf(Part(text = response)))
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}
