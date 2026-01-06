package com.example.fastnews

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.fastnews.data.BookmarkedArticle
import com.kwabenaberko.newsapilib.models.Article
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(newsViewModel: NewsViewModel, settingsViewModel: SettingsViewModel, navController: NavHostController) {
    val articles by newsViewModel.articles.observeAsState(initial = emptyList())
    val bookmarkedArticles by settingsViewModel.bookmarkedArticles.observeAsState(initial = emptyList())
    val summaryState by newsViewModel.summaryState.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CategoriesBar(newsViewModel)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(articles) {
                    article ->
                    val isBookmarked = bookmarkedArticles.any { it.url == article.url }
                    ArticleItem(
                        article = article,
                        isBookmarked = isBookmarked,
                        navController = navController,
                        onBookmarkClicked = {
                            val bookmarkedArticle = BookmarkedArticle(
                                title = article.title ?: "No Title",
                                url = article.url,
                                imageUrl = article.urlToImage,
                                source = article.source?.name
                            )
                            if (isBookmarked) {
                                settingsViewModel.removeBookmark(bookmarkedArticle)
                            } else {
                                settingsViewModel.addBookmark(bookmarkedArticle)
                            }
                        },
                        onSummarizeClicked = { newsViewModel.summarizeArticle(article) }
                    )
                }
            }
        }

        if (summaryState !is NewsViewModel.SummaryState.Idle) {
            ModalBottomSheet(
                onDismissRequest = { newsViewModel.dismissSummary() },
                sheetState = sheetState
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (val state = summaryState) {
                        is NewsViewModel.SummaryState.Loading -> CircularProgressIndicator()
                        is NewsViewModel.SummaryState.Success -> Text(text = state.summary)
                        is NewsViewModel.SummaryState.Error -> Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        else -> Unit
                    }
                }
            }
        }
    }
}

private fun formatPublishedAt(publishedAt: String?): String {
    if (publishedAt == null) {
        return "Unknown date"
    }
    return try {
        val odt = OffsetDateTime.parse(publishedAt)
        val localDate = odt.toLocalDate() // Convert to LocalDate
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) // Formatter for date only
        localDate.format(formatter)
    } catch (e: DateTimeParseException) {
        // Fallback for potentially different date formats or if parsing fails
        // You might want to try other common formats or return a simpler date part
        try {
            // Attempt to parse as just a date if datetime fails
            val dateOnlyFormatter = DateTimeFormatter.ISO_LOCAL_DATE
            val date = OffsetDateTime.parse(publishedAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate()
            date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        } catch (e2: DateTimeParseException) {
            "Invalid date format"
        }
    }
}

@Composable
fun ArticleItem(
    article: Article,
    isBookmarked: Boolean,
    navController: NavHostController,
    onBookmarkClicked: () -> Unit,
    onSummarizeClicked: () -> Unit
) {
    val formattedDate = formatPublishedAt(article.publishedAt)
    Card (modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = {
            navController.navigate(NewsArticleScreen(article.url))
        }
    ){
        Column {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                AsyncImage(model = article.urlToImage?:"https://cdn.iconscout.com/icon/premium/png-256-thumb/no-image-1753539-1493784.png",
                    contentDescription = "Article Image",
                    modifier = Modifier
                        .size(80.dp)
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
                Column (modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp))
                {
                    Text(text = article.title ?: "No Title", maxLines = 3, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) { // New Row for source and date
                        Text(
                            text = article.source?.name ?: "Unknown Source",
                            maxLines = 1,
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.bodySmall
                        )
                        // Conditionally display the date part
                        if (formattedDate.isNotBlank() && formattedDate != "Unknown date" && formattedDate != "Invalid date format") {
                            Text(
                                text = " - $formattedDate", // Added separator and date
                                maxLines = 1,
                                fontSize = 12.sp,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onSummarizeClicked) {
                    Text("Summarize")
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onBookmarkClicked) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "Bookmark"
                    )
                }
            }
        }
    }
}

@Composable
fun CategoriesBar(newsViewModel: NewsViewModel) {

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("general") }

    QueryTextField(
        query = searchQuery,
        onQueryChanged = { searchQuery = it },
        onSearch = {
            if (searchQuery.isNotEmpty()) {
                newsViewModel.onSearchQuery(searchQuery)
            }
            // Keyboard hiding is handled within QueryTextField
        },
        onSearchCancelled = { // New lambda for search cancellation
            selectedCategory = "general"// searchQuery is cleared by onQueryChanged("") inside QueryTextField
            newsViewModel.onCategorySelected("general") // Fetch general news
        },
        label = "Search" // This is used as placeholder text
    )

    val categoriesList = listOf(
        "general","business", "entertainment","health","science","sports","technology"
    )

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically

    ) {

        categoriesList.forEach { category ->
            val isSelected = category == selectedCategory
            Button(
                onClick = {
                    selectedCategory = category
                    newsViewModel.onCategorySelected(category)
                },
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                )
            ){
                Text(text = category)
            }

        }
    }
}

@Composable
fun QueryTextField(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onSearchCancelled: () -> Unit, // New parameter
    modifier: Modifier = Modifier,
    label: String = "Search" // This string is used as placeholder
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp) // Standard Material Design height
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp)),
        placeholder = { Text(text = label) },
        leadingIcon = { // Added leadingIcon
            if (query.isNotEmpty()) {
                IconButton(onClick = {
                    onQueryChanged("") // Clear the query
                    onSearchCancelled()    // Notify cancellation
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Clear search"
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                if (query.isNotEmpty()) {
                    onSearch()
                }
                keyboardController?.hide()
                focusManager.clearFocus()

            }
        ),
        trailingIcon = {
            IconButton(onClick = {
                if (query.isNotEmpty()) {
                    onSearch()
                }
                keyboardController?.hide()
                focusManager.clearFocus()
            }) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            }
        },
        singleLine = true
    )
}
