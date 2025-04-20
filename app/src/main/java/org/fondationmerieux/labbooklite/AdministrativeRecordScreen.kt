package org.fondationmerieux.labbooklite

import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.database.entity.DictionaryEntity
import org.fondationmerieux.labbooklite.database.entity.PatientEntity
import org.fondationmerieux.labbooklite.database.entity.RecordEntity
import org.fondationmerieux.labbooklite.database.entity.PrescriberEntity

/**
 * Created by AlC on 19/04/2025.
 */
@Composable
fun AdministrativeRecordScreen(recordId: Int, database: LabBookLiteDatabase) {
    val context = LocalContext.current
    var dictionaries by remember { mutableStateOf<List<DictionaryEntity>>(emptyList()) }
    var record by remember { mutableStateOf<RecordEntity?>(null) }
    var patient by remember { mutableStateOf<PatientEntity?>(null) }
    var prescriber by remember { mutableStateOf<PrescriberEntity?>(null) }

    LaunchedEffect(recordId) {
        withContext(Dispatchers.IO) {
            dictionaries = database.dictionaryDao().getAll()
        }

        withContext(Dispatchers.IO) {
            record = database.recordDao().getById(recordId)
        }

        if (record != null) {
            record!!.patient_id?.let {
                patient = database.patientDao().getById(it)
            }
            record!!.prescriber?.let {
                prescriber = database.prescriberDao().getById(it)
            }
        }
    }

    if (record == null) {
        Text("Chargement du dossier...")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val recordNumber = record!!.rec_num_lite?.takeLast(4)?.toIntOrNull() ?: record!!.id_data

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dossier ",
                style = MaterialTheme.typography.titleLarge
            )
            Box(
                modifier = Modifier
                    .background(color = Color(0xFFC7AD70), shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$recordNumber",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Identité", style = MaterialTheme.typography.titleMedium)

                if (patient == null) {
                    Text("Patient introuvable")
                } else {
                    val codeLabel = listOfNotNull(patient!!.pat_code, patient!!.pat_code_lab)
                        .filter { it.isNotBlank() }
                        .joinToString(" / ")

                    Text("Code : $codeLabel")

                    Text("Nom : ${patient!!.pat_name ?: ""}")
                    if (!patient!!.pat_maiden.isNullOrBlank()) {
                        Text("Nom de jeune fille : ${patient!!.pat_maiden}")
                    }
                    Text("Prénom : ${patient!!.pat_firstname ?: ""}")

                    val sexLabel = dictionaries
                        .firstOrNull { it.dico_name == "sexe" && it.id_data == patient!!.pat_sex }
                        ?.label ?: "—"
                    Text("Sexe : $sexLabel")

                    Text("Date de naissance : ${patient!!.pat_birth ?: ""}")

                    if (!patient!!.pat_phone1.isNullOrBlank()) Text("Téléphone 1 : ${patient!!.pat_phone1}")
                    if (!patient!!.pat_phone2.isNullOrBlank()) Text("Téléphone 2 : ${patient!!.pat_phone2}")
                    if (!patient!!.pat_email.isNullOrBlank()) Text("Email : ${patient!!.pat_email}")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Prescription", style = MaterialTheme.typography.titleMedium)

                Text("Date de réception : ${record!!.rec_date_receipt ?: "—"}")
                Text("Date de prescription : ${record!!.prescription_date ?: "—"}")

                if (prescriber == null) {
                    Text("Prescripteur : Non renseigné")
                } else {
                    val pr = prescriber!!
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
}
