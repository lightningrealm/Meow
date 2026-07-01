package com.lr.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.searchHistoryDataStore by preferencesDataStore(name = "search_history_prefs")

class SearchHistoryStorage(private val context: Context) {

    companion object {
        private val KEY_HISTORY = stringPreferencesKey("search_history_list")
        private const val MAX_HISTORY_SIZE = 15
    }

    val historyFlow: Flow<List<String>> = context.searchHistoryDataStore.data.map { prefs ->
        val historyStr = prefs[KEY_HISTORY] ?: ""
        if (historyStr.isEmpty()) emptyList() else historyStr.split("||")
    }

    suspend fun addSearchQuery(query: String) {
        if (query.isBlank()) return
        
        context.searchHistoryDataStore.edit { prefs ->
            val currentStr = prefs[KEY_HISTORY] ?: ""
            val currentList = if (currentStr.isEmpty()) emptyList() else currentStr.split("||")
            
            // Remove duplicates and add to front
            val newList = listOf(query) + currentList.filter { it != query }
            
            // Limit size
            val trimmedList = newList.take(MAX_HISTORY_SIZE)
            
            prefs[KEY_HISTORY] = trimmedList.joinToString("||")
        }
    }

    suspend fun clearHistory() {
        context.searchHistoryDataStore.edit { prefs ->
            prefs.remove(KEY_HISTORY)
        }
    }
}
