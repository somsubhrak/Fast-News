package com.example.fastnews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.fastnews.data.BookmarkedArticle

@Composable
fun BookmarksScreen(viewModel: SettingsViewModel, navController: NavHostController) {
    val bookmarkedArticles by viewModel.bookmarkedArticles.observeAsState(initial = emptyList())

    if (bookmarkedArticles.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No bookmarks yet.", fontSize = 20.sp)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(bookmarkedArticles) { article ->
                BookmarkedArticleItem(article = article, onRemove = { viewModel.removeBookmark(article) }) {
                    navController.navigate(NewsArticleScreen(article.url))
                }
            }
        }
    }
}

@Composable
fun BookmarkedArticleItem(article: BookmarkedArticle, onRemove: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = article.imageUrl ?: "https://cdn.iconscout.com/icon/premium/png-256-thumb/no-image-1753539-1493784.png",
                contentDescription = "Article Image",
                modifier = Modifier
                    .size(80.dp)
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(text = article.title, maxLines = 3, fontWeight = FontWeight.Bold)
                Text(
                    text = article.source ?: "Unknown Source",
                    maxLines = 1,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onRemove) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove Bookmark")
            }
        }
    }
}
