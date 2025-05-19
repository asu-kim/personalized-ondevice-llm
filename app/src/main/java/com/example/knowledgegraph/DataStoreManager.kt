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

    suspend fun saveCredential(context: Context, username: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[USERNAME_KEY] = username
            prefs[PASSWORD_KEY] = password
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
}