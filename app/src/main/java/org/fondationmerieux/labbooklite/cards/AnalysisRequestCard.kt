package org.fondationmerieux.labbooklite.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.fondationmerieux.labbooklite.database.model.AnalysisSelection
import org.fondationmerieux.labbooklite.database.model.AnalysisWithFamily

@Composable
fun AnalysisRequestCard(
    analysisSelection: AnalysisSelection,
    allAnalyses: List<AnalysisWithFamily>,
    onRemove: () -> Unit
) {
    val code = analysisSelection.code
    val name = analysisSelection.name

    val ana = allAnalyses.firstOrNull { it.id == analysisSelection.id }
    val loinc = ana?.ana_loinc?.takeIf { it.isNotBlank() } ?: ""
    val displayText = if (loinc.isNotEmpty()) {
        "$code / $loinc - $name"
    } else {
        "$code - $name"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Urgent :",
                    modifier = Modifier.padding(end = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                Row {
                    listOf("Oui", "Non").forEach { label ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            RadioButton(
                                selected = (label == "Oui" && analysisSelection.isUrgent.value) ||
                                        (label == "Non" && !analysisSelection.isUrgent.value),
                                onClick = {
                                    analysisSelection.isUrgent.value = (label == "Oui")
                                }
                            )
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}