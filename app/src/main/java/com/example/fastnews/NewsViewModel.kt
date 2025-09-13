package com.example.fastnews

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kwabenaberko.newsapilib.NewsApiClient
import com.kwabenaberko.newsapilib.NewsApiClient.ArticlesResponseCallback
import com.kwabenaberko.newsapilib.models.Article
import com.kwabenaberko.newsapilib.models.request.EverythingRequest
import com.kwabenaberko.newsapilib.models.request.TopHeadlinesRequest
import com.kwabenaberko.newsapilib.models.response.ArticleResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


class NewsViewModel: ViewModel (){

    private val _articles = MutableLiveData<List<Article>>()
    val articles: LiveData<List<Article>> get() = _articles

    init {
        fetchNewsTopHeadlines()
    }

    fun fetchNewsTopHeadlines(category: String = "general"){
        // Create a logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Log request and response bodies
        }

        // Create a custom OkHttpClient
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val newsApiClient = NewsApiClient(Constant.apiKey)

        newsApiClient.getTopHeadlines(
            TopHeadlinesRequest.Builder().category(category)
                .language("en")
                .build(),
            object : ArticlesResponseCallback {
                override fun onSuccess(response: ArticleResponse) {
                   response?.articles?.let {
                       _articles.postValue(it)
                   }
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("NewsAPI Error", "Full error: ", throwable)
                }
            }
        )
    }
    fun fetchEverythingWithQuery(query: String){
        // Create a logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Log request and response bodies
        }

        // Create a custom OkHttpClient
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val newsApiClient = NewsApiClient(Constant.apiKey)

        newsApiClient.getEverything(
            EverythingRequest.Builder()
                .language("en")
                .q(query)
                .build(),
            object : ArticlesResponseCallback {
                override fun onSuccess(response: ArticleResponse) {
                    response?.articles?.let {
                        _articles.postValue(it)
                    }
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("NewsAPI Error", "Full error: ", throwable)
                }
            }
        )
    }
}