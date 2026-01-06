package com.example.fastnews

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastnews.data.SettingsRepository
import com.kwabenaberko.newsapilib.NewsApiClient
import com.kwabenaberko.newsapilib.NewsApiClient.ArticlesResponseCallback
import com.kwabenaberko.newsapilib.models.Article
import com.kwabenaberko.newsapilib.models.request.EverythingRequest
import com.kwabenaberko.newsapilib.models.request.TopHeadlinesRequest
import com.kwabenaberko.newsapilib.models.response.ArticleResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NewsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // --- UI State ---
    private val _articles = MutableLiveData<List<Article>>()
    val articles: LiveData<List<Article>> get() = _articles

    private val category = MutableStateFlow("general")

    // --- Summarization State ---
    sealed class SummaryState {
        object Idle : SummaryState()
        object Loading : SummaryState()
        data class Success(val summary: String) : SummaryState()
        data class Error(val message: String) : SummaryState()
    }

    private val _summaryState = MutableStateFlow<SummaryState>(SummaryState.Idle)
    val summaryState: StateFlow<SummaryState> = _summaryState.asStateFlow()

    private val newsContentRepository = NewsContentRepository()

    // --- Init ---
    init {
        viewModelScope.launch {
            combine(
                settingsRepository.preferredCountryFlow,
                settingsRepository.preferredLanguageFlow,
                category
            ) { country, language, category ->
                Triple(country, language, category)
            }.collectLatest { (country, language, category) ->
                fetchNews(country, language, category)
            }
        }
    }

    // --- UI Events ---
    fun onCategorySelected(category: String) {
        this.category.value = category
    }

    fun onSearchQuery(query: String) {
        viewModelScope.launch {
            val language = settingsRepository.preferredLanguageFlow.first()
            fetchEverything(query, language)
        }
    }

    fun summarizeArticle(article: Article) {
        viewModelScope.launch {
            _summaryState.value = SummaryState.Loading

            val url = article.url
            if (url.isNullOrBlank()) {
                _summaryState.value = SummaryState.Error("Article has no URL to summarize.")
                return@launch
            }

            val result = newsContentRepository.summarizeUrl(url)

            if (result.startsWith("Error:")) {
                _summaryState.value = SummaryState.Error(result)
            } else {
                _summaryState.value = SummaryState.Success(result)
            }
        }
    }

    fun dismissSummary() {
        _summaryState.value = SummaryState.Idle
    }

    // --- Core Fetch Logic ---
    private fun fetchNews(country: String, language: String, category: String) {
        val client = NewsApiClient(Constant.apiKey)

        if (category.equals("general", ignoreCase = true) || country.equals("us", ignoreCase = true)) {
            val requestBuilder = TopHeadlinesRequest.Builder()
                .language(language)

            if (country.equals("us", ignoreCase = true)) {
                requestBuilder.country(country)
                if (!category.equals("general", ignoreCase = true)) {
                    requestBuilder.category(category)
                }
            } else { // general category for other countries
                requestBuilder.country(country)
            }

            client.getTopHeadlines(
                requestBuilder.build(),
                object : ArticlesResponseCallback {
                    override fun onSuccess(response: ArticleResponse) {
                        _articles.postValue(response.articles ?: emptyList())
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.e("NewsAPI Error", "Failed to fetch top headlines: ", throwable)
                        _articles.postValue(emptyList())
                    }
                }
            )
        } else {
            fetchEverything(category, language)
        }
    }

    private fun fetchEverything(query: String, language: String) {
        val client = NewsApiClient(Constant.apiKey)

        client.getEverything(
            EverythingRequest.Builder()
                .q(query)
                .language(language.lowercase())
                .sortBy("publishedAt")
                .pageSize(20)
                .build(),
            object : ArticlesResponseCallback {
                override fun onSuccess(response: ArticleResponse) {
                    _articles.postValue(response.articles ?: emptyList())
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("NewsAPI", "Everything fetch failed", throwable)
                    _articles.postValue(emptyList())
                }
            }
        )
    }
}
