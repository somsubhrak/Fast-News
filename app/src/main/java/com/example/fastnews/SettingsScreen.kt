package com.example.fastnews

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val preferredCountry by viewModel.preferredCountry.observeAsState()
    val preferredLanguage by viewModel.preferredLanguage.observeAsState()
    val darkModeEnabled by viewModel.darkModeEnabled.observeAsState()

    val countries = listOf( "us" to "United States", "gb" to "United Kingdom", "in" to "India", "ca" to "Canada", "au" to "Australia", "de" to "Germany", "fr" to "France" )
    val languages = listOf(
        Pair("ar", "Arabic"), Pair("de", "German"), Pair("en", "English"), Pair("es", "Spanish"),
        Pair("fr", "French"), Pair("he", "Hebrew"), Pair("it", "Italian"), Pair("nl", "Dutch"),
        Pair("no", "Norwegian"), Pair("pt", "Portuguese"), Pair("ru", "Russian"), Pair("sv", "Swedish"),
        Pair("ud", "Urdu"), Pair("zh", "Chinese")
    )

    var countryExpanded by remember { mutableStateOf(false) }
    var languageExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text("General", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Dark Mode", fontSize = 18.sp)
            Switch(
                checked = darkModeEnabled ?: isSystemInDarkTheme(),
                onCheckedChange = { viewModel.setDarkModeEnabled(it) }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("News Preferences", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Preferred News Region", fontSize = 18.sp)
            ExposedDropdownMenuBox(expanded = countryExpanded, onExpandedChange = { countryExpanded = it }) {
                TextField(
                    value = preferredCountry?.let { Locale("", it).displayCountry } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryExpanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(expanded = countryExpanded, onDismissRequest = { countryExpanded = false }) {
                    countries.forEach { (code, name) ->
                        DropdownMenuItem(text = { Text(name) }, onClick = {
                            viewModel.setPreferredCountry(code)
                            countryExpanded = false
                        })
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Preferred Language", fontSize = 18.sp)
            ExposedDropdownMenuBox(expanded = languageExpanded, onExpandedChange = { languageExpanded = it }) {
                TextField(
                    value = preferredLanguage?.let { langCode -> languages.find { it.first == langCode }?.second } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(expanded = languageExpanded, onDismissRequest = { languageExpanded = false }) {
                    languages.forEach { (code, name) ->
                        DropdownMenuItem(text = { Text(name) }, onClick = {
                            viewModel.setPreferredLanguage(code)
                            languageExpanded = false
                        })
                    }
                }
            }
        }
    }
}
