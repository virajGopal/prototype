package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.GeminiRepository
import kotlinx.coroutines.launch

@Composable
fun AiScannerScreen() {
    val repository = remember { GeminiRepository() }
    val coroutineScope = rememberCoroutineScope()
    
    var selectedClass by remember { mutableStateOf("FY CO") }
    val classes = listOf("FY CO", "IT", "Electronics", "Mechanical", "Civil", "Auto")
    
    var generatedImageBase64 by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var selectedSize by remember { mutableStateOf("1K") }
    var customPrompt by remember { mutableStateOf("A classroom with students sitting at desks. One student is looking at a phone, one is distracted looking away, and others are focused on the teacher.") }
    
    var scanStatus by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("AI Classroom Scanner", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        Text("Select Class:", style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(classes) { className ->
                FilterChip(
                    selected = selectedClass == className,
                    onClick = { selectedClass = className },
                    label = { Text(className) }
                )
            }
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (generatedImageBase64 != null) {
                    val bytes = Base64.decode(generatedImageBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "CCTV Frame",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    if (scanStatus != null) {
                        // Drawing overlay mock
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Scan Complete", color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Badge(containerColor = com.example.ui.theme.GreenFocus) { Text("Focused: 12") }
                                    Badge(containerColor = com.example.ui.theme.YellowDistracted) { Text("Distracted: 3") }
                                    Badge(containerColor = com.example.ui.theme.RedPhone) { Text("Phone: 1") }
                                }
                            }
                        }
                    }
                } else {
                    Text("No CCTV Feed. Generate a mock frame.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                if (isGenerating || isScanning) {
                    CircularProgressIndicator()
                }
            }
        }

        Button(
            onClick = {
                isScanning = true
                coroutineScope.launch {
                    // Simulate scanning delay
                    kotlinx.coroutines.delay(2000)
                    scanStatus = "Done"
                    isScanning = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = generatedImageBase64 != null && !isGenerating && !isScanning
        ) {
            Text("SCAN THE CLASSROOM")
        }

        Divider()
        
        Text("Mock CCTV Frame Generation", style = MaterialTheme.typography.titleMedium)
        
        OutlinedTextField(
            value = customPrompt,
            onValueChange = { customPrompt = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Image Prompt") },
            maxLines = 3
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Size: ", style = MaterialTheme.typography.bodyMedium)
            listOf("1K", "2K", "4K").forEach { size ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedSize == size,
                        onClick = { selectedSize = size }
                    )
                    Text(size, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        
        Button(
            onClick = {
                isGenerating = true
                scanStatus = null
                coroutineScope.launch {
                    try {
                        val base64 = repository.generateImage(customPrompt, selectedSize)
                        generatedImageBase64 = base64
                    } catch (e: Exception) {
                        // Handle error
                        e.printStackTrace()
                    } finally {
                        isGenerating = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isGenerating && customPrompt.isNotBlank()
        ) {
            Text("Generate CCTV Frame")
        }
        
        if (scanStatus != null) {
            // High Thinking Mode request
            var aiAnalysis by remember { mutableStateOf<String?>(null) }
            var isThinking by remember { mutableStateOf(false) }
            
            Button(
                onClick = {
                    isThinking = true
                    coroutineScope.launch {
                        try {
                            aiAnalysis = repository.generateText(
                                prompt = "Analyze the simulated classroom scan data: 12 focused, 3 distracted, 1 on phone. Provide a detailed pedagogical recommendation for the teacher to improve engagement. You must think deeply about this.",
                                highThinking = true,
                                model = "gemini-3.1-pro-preview"
                            )
                        } finally {
                            isThinking = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(if (isThinking) "Thinking..." else "Deep Analysis (High Thinking)")
            }
            
            if (aiAnalysis != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(aiAnalysis!!, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
