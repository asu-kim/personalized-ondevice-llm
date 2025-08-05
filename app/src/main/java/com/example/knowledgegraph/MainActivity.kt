package com.example.knowledgegraph

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.knowledgegraph.ui.theme.KnowledgeGraphTheme
import androidx.compose.runtime.produceState
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
//for gmail
//import android.util.Log
//import androidx.activity.result.contract.ActivityResultContracts
//import com.google.android.gms.auth.GoogleAuthUtil
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import com.google.android.gms.common.api.Scope
//import kotlinx.coroutines.*
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import org.json.JSONObject
//import com.example.knowledgegraph.GmailViewModel
//import android.accounts.Account

class MainActivity : ComponentActivity() {

//    private lateinit var gmailViewModel: GmailViewModel
//    private val coroutineScope = CoroutineScope(Dispatchers.Main)
//
//    private val signInLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        Log.d("GMAIL", "Sign-in launcher triggered")
//
//        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//        if (task.isSuccessful) {
//            val account = task.result
//            Log.d("GMAIL", "Signed in account: ${account?.email}")
//
//            val acct = Account(account.email, "com.google")
//            Log.d("GMAIL", "Account name: ${acct?.name}")
//
//            val scopeString = "oauth2:https://www.googleapis.com/auth/gmail.readonly"
//
//            coroutineScope.launch(Dispatchers.IO) {
//                try {
//                    Log.d("GMAIL", "Fetching token...")
//                    val token = GoogleAuthUtil.getToken(this@MainActivity, acct, scopeString)
//                    Log.d("GMAIL", "Token received: $token")
//
//                    val messages = fetchGmailMessages(token)
//                    Log.d("GMAIL", "Messages received: ${messages.size}")
//
//
//                    withContext(Dispatchers.Main) {
//                        gmailViewModel.setSnippets(messages)
//                    }
//                } catch (e: Exception) {
//                    Log.e("GMAIL", "Failed to fetch Gmail: ${e.message}", e)
//                }
//            }
//        } else {
//            Log.e("GMAIL", "Sign-in failed: ${task.exception?.message}", task.exception)
//        }
//    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
//        gmailViewModel = ViewModelProvider(this)[GmailViewModel::class.java]
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestEmail()
//            .requestScopes(Scope("https://www.googleapis.com/auth/gmail.readonly"))
//            .build()
//
//        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
//        signInLauncher.launch(mGoogleSignInClient.signInIntent)
        enableEdgeToEdge()
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
//        }
        setContent {
            KnowledgeGraphTheme {
                val navController = rememberNavController()
                val context = applicationContext

                val isUserRegistered = produceState(initialValue = false) {
                    value = DataStoreManager.isUserRegistered(context)
                }

                val startDestination = if (isUserRegistered.value) "login" else "signup"

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("signup") {
                        SignUpScreen(onSignUpComplete = {
                            navController.navigate("login") {
                                popUpTo("signup") { inclusive = true }
                            }
                        })
                    }
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { userName ->
                                navController.navigate("home/$userName")
                            },
                            onForgotPassword = {
                                navController.navigate("forgot_password")
                            }
                        )
                    }
                    composable("forgot_password") {
                        ForgotPasswordScreen(
                            onPasswordReset = {
                                navController.navigate("login") {
                                    popUpTo("forgot_password") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(
                        route = "home/{userName}",
                        arguments = listOf(navArgument("userName") { type = NavType.StringType })
                    ) {
                        val userName = it.arguments?.getString("userName") ?: ""
//                        HomeScreen(name = userName, navController = navController, gmailViewModel = gmailViewModel)
                        HomeScreen(name = userName, navController = navController)
                    }
                }
            }
        }
    }
}
//suspend fun fetchGmailMessages(token: String): List<String> {
//    val client = OkHttpClient()
//    val request = Request.Builder()
//        .url("https://gmail.googleapis.com/gmail/v1/users/me/messages?maxResults=5")
//        .header("Authorization", "Bearer $token")
//        .build()
//
//    client.newCall(request).execute().use { response ->
//        if (!response.isSuccessful) return listOf("Failed to fetch messages")
//
//        val json = JSONObject(response.body!!.string())
//        val messages = json.getJSONArray("messages")
//
//        val snippets = mutableListOf<String>()
//        for (i in 0 until messages.length()) {
//            val id = messages.getJSONObject(i).getString("id")
//            val messageRequest = Request.Builder()
//                .url("https://gmail.googleapis.com/gmail/v1/users/me/messages/$id?format=full")
//                .header("Authorization", "Bearer $token")
//                .build()
//            client.newCall(messageRequest).execute().use { msgRes ->
//                if (msgRes.isSuccessful) {
//                    val msgJson = JSONObject(msgRes.body!!.string())
//                    val snippet = msgJson.optString("snippet", "")
//                    snippets.add(snippet)
//                }
//            }
//        }
//        return snippets
//    }
//}