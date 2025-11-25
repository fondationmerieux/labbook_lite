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
import org.fondationmerieux.labbooklite.database.entity.PrescriberEntity
import org.fondationmerieux.labbooklite.database.entity.RecordEntity

@Composable
fun PrescriptionInfoCard(record: RecordEntity, prescriber: PrescriberEntity?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Prescription", style = MaterialTheme.typography.titleMedium)

            Text("Date de réception : ${record.rec_date_receipt ?: "—"}")
            Text("Date de prescription : ${record.prescription_date ?: "—"}")

            if (prescriber == null) {
                Text("Prescripteur : Non renseigné")
            } else {
                val pr = prescriber
                val fullName = listOfNotNull(pr.lastname, pr.firstname).joinToString(" ")
                Text("Prescripteur : $fullName")

                if (!pr.mobile.isNullOrBlank()) {
                    Text("Téléphone : ${pr.mobile}")
                }
                if (!pr.fax.isNullOrBlank()) {
                    Text("Fax : ${pr.fax}")
                }
                if (!pr.email.isNullOrBlank()) {
                    Text("Email : ${pr.email}")
                }
            }
        }
    }
}
