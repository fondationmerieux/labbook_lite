package org.fondationmerieux.labbooklite.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.fondationmerieux.labbooklite.database.entity.AnalysisEntity
import org.fondationmerieux.labbooklite.database.entity.AnalysisRequestEntity

@Composable
fun AnalysesCard(analyses: List<AnalysisEntity>, requests: List<AnalysisRequestEntity>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Analyses", style = MaterialTheme.typography.titleMedium)

            val filtered = analyses.filterNot { it.code?.startsWith("PB") == true }

            if (filtered.isEmpty()) {
                Text("Aucune analyse")
            } else {
                filtered.forEach { ana ->
                    val urgent = requests.find { it.analysisRef == ana.id_data }?.isUrgent == 4
                    Text("${ana.code} - ${ana.name}" + if (urgent) " (Urgent)" else "")
                }
            }
        }
    }
}
