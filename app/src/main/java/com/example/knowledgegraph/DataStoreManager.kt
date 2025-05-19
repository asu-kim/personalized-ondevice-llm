package com.example.knowledgegraph

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("user_prefs")

object DataStoreManager {
    private val PASSWORD_KEY = stringPreferencesKey("user_password")

    suspend fun savePassword(context: Context, password: String) {
        context.dataStore.edit { prefs ->
            prefs[PASSWORD_KEY] = password
        }
    }

    suspend fun getPassword(context: Context): String? {
        return context.dataStore.data
            .map { it[PASSWORD_KEY] }
            .first()
    }

    suspend fun isPasswordSet(context: Context): Boolean {
        return getPassword(context) != null
    }
}