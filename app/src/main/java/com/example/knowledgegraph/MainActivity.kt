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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KnowledgeGraphTheme {
                val navController = rememberNavController()
                val context = applicationContext
                val passwordSet = produceState(initialValue = false) {
                    value = DataStoreManager.isPasswordSet(context)
                }

                val startDestination = if (passwordSet.value) "login" else "create_password"

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("create_password") {
                        CreatePasswordScreen(
                            onPasswordSet = {
                                navController.navigate("login") {
                                    popUpTo("create_password") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("login") {
                        LoginScreen(onLoginSuccess = { userName ->
                            navController.navigate("home/$userName")
                        })
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