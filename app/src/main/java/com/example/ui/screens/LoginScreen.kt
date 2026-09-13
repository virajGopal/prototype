package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.FirebaseManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    
    // Check if already logged in or if Firebase is missing
    LaunchedEffect(Unit) {
        if (!FirebaseManager.isInitialized) {
            Toast.makeText(context, "Firebase not configured. Please add google-services.json", Toast.LENGTH_LONG).show()
        } else if (FirebaseManager.auth?.currentUser != null) {
            onLoginSuccess()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("OLIVE", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Smart Classroom Analyser", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(48.dp))
            
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (!FirebaseManager.isInitialized) {
                            Toast.makeText(context, "Firebase not configured. Please add google-services.json", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                signInWithGoogle(context) { success, message ->
                                    isLoading = false
                                    if (success) {
                                        onLoginSuccess()
                                    } else {
                                        Toast.makeText(context, "Login failed: $message\n(Ensure google-services.json is added and Web Client ID is set)", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Text("Sign in with Google", modifier = Modifier.padding(8.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { onLoginSuccess() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Preview App (Guest)", modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

private suspend fun signInWithGoogle(context: Context, onResult: (Boolean, String?) -> Unit) {
    val credentialManager = CredentialManager.create(context)
    val rawNonce = UUID.randomUUID().toString()
    val bytes = rawNonce.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

    // Placeholder Web Client ID - user MUST replace this in a real app or strings.xml
    val webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setNonce(hashedNonce)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, onResult)
    } catch (e: GetCredentialException) {
        onResult(false, e.message)
    }
}

private fun handleSignIn(result: GetCredentialResponse, onResult: (Boolean, String?) -> Unit) {
    val credential = result.credential
    if (credential is GoogleIdTokenCredential) {
        val idToken = credential.idToken
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseManager.auth?.signInWithCredential(firebaseCredential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true, null)
            } else {
                onResult(false, task.exception?.message)
            }
        } ?: onResult(false, "Firebase Auth not available")
    } else {
        onResult(false, "Unexpected credential type")
    }
}
