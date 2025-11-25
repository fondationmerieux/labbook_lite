package org.fondationmerieux.labbooklite.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.fondationmerieux.labbooklite.database.entity.DictionaryEntity
import org.fondationmerieux.labbooklite.database.entity.PatientEntity

@Composable
fun PatientInfoCard(
    patient: PatientEntity?,
    dictionaries: List<DictionaryEntity>,
    navController: NavController
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Identité", style = MaterialTheme.typography.titleMedium)

            if (patient == null) {
                Text("Patient introuvable")
            } else {
                val codeLabel = listOfNotNull(patient.pat_code, patient.pat_code_lab)
                    .filter { it.isNotBlank() }
                    .joinToString(" / ")

                Text("Code : $codeLabel")

                Text("Nom : ${patient.pat_name ?: ""}")
                if (!patient.pat_maiden.isNullOrBlank()) {
                    Text("Nom de jeune fille : ${patient.pat_maiden}")
                }
                Text("Prénom : ${patient.pat_firstname ?: ""}")

                val sexLabel = dictionaries
                    .firstOrNull { it.dico_name == "sexe" && it.id_data == patient.pat_sex }
                    ?.label ?: "—"
                Text("Sexe : $sexLabel")

                Text("Date de naissance : ${patient.pat_birth ?: ""}")

                if (!patient.pat_phone1.isNullOrBlank()) Text("Téléphone 1 : ${patient.pat_phone1}")
                if (!patient.pat_phone2.isNullOrBlank()) Text("Téléphone 2 : ${patient.pat_phone2}")
                if (!patient.pat_email.isNullOrBlank()) Text("Email : ${patient.pat_email}")

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        navController.navigate("patient_form/${patient.id_data}")
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Modifier le patient")
                }
            }
        }
    }
}
