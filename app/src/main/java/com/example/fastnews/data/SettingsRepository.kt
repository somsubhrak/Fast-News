package com.example.fastnews.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Serializable
data class BookmarkedArticle(
    val title: String,
    val url: String,
    val imageUrl: String?,
    val source: String?
)

class SettingsRepository(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val PREFERRED_COUNTRY = stringPreferencesKey("preferred_country")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val BOOKMARKED_ARTICLES = stringPreferencesKey("bookmarked_articles")
    }

    val preferredCountryFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[PREFERRED_COUNTRY] ?: "us" // Default to "us"
    }

    val preferredLanguageFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[PREFERRED_LANGUAGE] ?: "en" // Default to "en"
    }

    val darkModeEnabledFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DARK_MODE_ENABLED] ?: false // Default to light mode
    }

    val bookmarkedArticlesFlow: Flow<List<BookmarkedArticle>> = dataStore.data.map { preferences ->
        val json = preferences[BOOKMARKED_ARTICLES] ?: "[]"
        Json.decodeFromString(json)
    }

    suspend fun setPreferredCountry(country: String) {
        dataStore.edit { settings ->
            settings[PREFERRED_COUNTRY] = country
        }
    }

    suspend fun setPreferredLanguage(language: String) {
        dataStore.edit { settings ->
            settings[PREFERRED_LANGUAGE] = language
        }
    }

    suspend fun setDarkModeEnabled(isEnabled: Boolean) {
        dataStore.edit { settings ->
            settings[DARK_MODE_ENABLED] = isEnabled
        }
    }

    suspend fun addBookmark(article: BookmarkedArticle) {
        dataStore.edit { settings ->
            val currentBookmarksJson = settings[BOOKMARKED_ARTICLES] ?: "[]"
            val currentBookmarks = Json.decodeFromString<MutableList<BookmarkedArticle>>(currentBookmarksJson)
            if (currentBookmarks.none { it.url == article.url }) {
                currentBookmarks.add(article)
                settings[BOOKMARKED_ARTICLES] = Json.encodeToString(currentBookmarks)
            }
        }
    }

    suspend fun removeBookmark(article: BookmarkedArticle) {
        dataStore.edit { settings ->
            val currentBookmarksJson = settings[BOOKMARKED_ARTICLES] ?: "[]"
            val currentBookmarks = Json.decodeFromString<MutableList<BookmarkedArticle>>(currentBookmarksJson)
            if (currentBookmarks.removeAll { it.url == article.url }) {
                settings[BOOKMARKED_ARTICLES] = Json.encodeToString(currentBookmarks)
            }
        }
    }
}
