package org.fondationmerieux.labbooklite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.database.entity.PreferencesEntity

@Composable
fun PreferencesScreen(database: LabBookLiteDatabase) {
    var prefs by remember { mutableStateOf<List<PreferencesEntity>>(emptyList()) }

    // Preferences list to hide
    val hiddenKeys = listOf(
        "facturation_pat_hosp",
        "auto_logout",
        "qualite",
        "facturation"
    )

    LaunchedEffect(Unit) {
        prefs = withContext(Dispatchers.IO) {
            database.preferencesDao().getAll()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(prefs.filter { it.key !in hiddenKeys }) { pref ->
                PreferencesRow(pref)
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun PreferencesRow(pref: PreferencesEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = pref.label ?: pref.key.orEmpty(),
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = pref.value.orEmpty(),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
