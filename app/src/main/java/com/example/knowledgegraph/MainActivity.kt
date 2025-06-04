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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        HomeScreen(name = userName, navController = navController)
                    }
                }
            }
        }
    }
}