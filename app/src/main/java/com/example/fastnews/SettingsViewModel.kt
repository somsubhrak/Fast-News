package com.example.fastnews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.fastnews.data.BookmarkedArticle
import com.example.fastnews.data.SettingsRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val preferredCountry = settingsRepository.preferredCountryFlow.asLiveData()
    val preferredLanguage = settingsRepository.preferredLanguageFlow.asLiveData()
    val darkModeEnabled = settingsRepository.darkModeEnabledFlow.asLiveData()
    val bookmarkedArticles = settingsRepository.bookmarkedArticlesFlow.asLiveData()

    fun setPreferredCountry(country: String) {
        viewModelScope.launch {
            settingsRepository.setPreferredCountry(country)
        }
    }

    fun setPreferredLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.setPreferredLanguage(language)
        }
    }

    fun setDarkModeEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkModeEnabled(isEnabled)
        }
    }

    fun addBookmark(article: BookmarkedArticle) {
        viewModelScope.launch {
            settingsRepository.addBookmark(article)
        }
    }

    fun removeBookmark(article: BookmarkedArticle) {
        viewModelScope.launch {
            settingsRepository.removeBookmark(article)
        }
    }
}
