package com.example.knowledgegraph

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("user_prefs")

object DataStoreManager {
    private val USERNAME_KEY = stringPreferencesKey("user_name")
    private val PASSWORD_KEY = stringPreferencesKey("user_password")
    private val FIRST_NAME_KEY = stringPreferencesKey("first_name")
    private val ROLE_KEY = stringPreferencesKey("role")
    private val TIMEZONE_KEY = stringPreferencesKey("timezone")

    suspend fun saveCredential(
        context: Context,
        username: String,
        password: String,
        firstName: String,
        role: String,
        timezone: String,
    ) {
        context.dataStore.edit { prefs ->
            prefs[USERNAME_KEY] = username
            prefs[PASSWORD_KEY] = password
            prefs[FIRST_NAME_KEY] = firstName
            prefs[ROLE_KEY] = role
            prefs[TIMEZONE_KEY] = timezone
        }
    }

    suspend fun savePassword(context: Context, newPassword: String) {
        context.dataStore.edit { prefs ->
            prefs[PASSWORD_KEY] = newPassword
        }
    }

    suspend fun getCredential(context: Context): Pair<String?, String?> {
        val data = context.dataStore.data.first()
        return Pair(data[USERNAME_KEY], data[PASSWORD_KEY])
    }

    suspend fun getUsername(context: Context): String? {
        return context.dataStore.data.map { it[USERNAME_KEY] }.first()
    }

    suspend fun getPassword(context: Context): String? {
        return context.dataStore.data.map { it[PASSWORD_KEY] }.first()
    }

    suspend fun isUserRegistered(context: Context): Boolean {
        val (username, password) = getCredential(context)
        return !username.isNullOrEmpty() && !password.isNullOrEmpty()
    }

    data class UserProfile(
        val username: String?,
        val firstName: String?,
        val role: String?,
        val timezone: String?
    )

    suspend fun getUserProfile(context: Context): UserProfile {
        val data = context.dataStore.data.first()
        return UserProfile(
            username = data[USERNAME_KEY],
            firstName = data[FIRST_NAME_KEY],
            role = data[ROLE_KEY],
            timezone = data[TIMEZONE_KEY]
        )
    }
}