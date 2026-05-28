package com.example.knowledgegraph

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


@Composable
fun SignUpScreen(onSignUpComplete: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var timezone by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(
            text = "Create a new profile",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = role,
            onValueChange = { role = it },
            label = { Text("Role (e.g. Developer)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = timezone,
            onValueChange = { timezone = it },
            label = { Text("Timezone (e.g. MST)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (username.isNotBlank() &&
                    password == confirm &&
                    password.isNotBlank() &&
                    firstName.isNotBlank() &&
                    role.isNotBlank() &&
                    timezone.isNotBlank()
                ) {
                    coroutineScope.launch {
                        DataStoreManager.saveCredential(
                            context,
                            username,
                            password,
                            firstName,
                            role,
                            timezone
                        )
                        onSignUpComplete()
                    }
                } else {
                    error = "All fields are required and passwords must match."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Set Profile")
        }

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, color = Color.Red)
        }
    }
}