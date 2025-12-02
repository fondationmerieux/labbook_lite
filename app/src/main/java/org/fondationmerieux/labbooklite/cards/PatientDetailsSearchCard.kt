package org.fondationmerieux.labbooklite.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.fondationmerieux.labbooklite.database.entity.PatientEntity

@Composable
fun PatientDetailsSearchCard(
    patient: PatientEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val name = patient.pat_name.orEmpty()
            val firstName = patient.pat_firstname.orEmpty()
            val code = patient.pat_code?.takeIf { it.isNotBlank() }?.let { "[$it]" } ?: ""
            val labCode = patient.pat_code_lab.orEmpty()

            val line1 = listOf(name, firstName, code)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .plus(if (labCode.isNotBlank()) " / $labCode" else "")

            Text(text = line1, style = MaterialTheme.typography.titleSmall)

            val birth = patient.pat_birth.orEmpty()
            val phone1 = patient.pat_phone1.orEmpty()
            val phone2 = patient.pat_phone2.orEmpty()

            val line2 = listOf(birth, phone1, phone2)
                .filter { it.isNotBlank() }
                .joinToString(" / ")

            if (line2.isNotBlank()) {
                Text(text = line2, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}