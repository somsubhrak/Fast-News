package com.example.fastnews

import kotlinx.serialization.Serializable

@Serializable
object HomePageScreen

@Serializable
data class NewsArticleScreen(
    val url: String
)

@Serializable
object BookmarksScreen

@Serializable
object SettingsScreen