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

@Composable
fun SamplingActsCard(analyses: List<AnalysisEntity>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Actes de prélèvements", style = MaterialTheme.typography.titleMedium)

            val pbs = analyses.filter { it.code?.startsWith("PB") == true }

            if (pbs.isEmpty()) {
                Text("Aucun acte")
            } else {
                pbs.forEach { act ->
                    Text("${act.code} - ${act.name}")
                }
            }
        }
    }
}
