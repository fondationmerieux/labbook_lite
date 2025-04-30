package org.fondationmerieux.labbooklite

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.database.entity.AnalysisEntity
import org.fondationmerieux.labbooklite.database.entity.AnalysisRequestEntity
import org.fondationmerieux.labbooklite.database.entity.DictionaryEntity
import org.fondationmerieux.labbooklite.database.entity.PatientEntity
import org.fondationmerieux.labbooklite.database.entity.RecordEntity
import org.fondationmerieux.labbooklite.database.entity.PrescriberEntity

/**
 * Created by AlC on 19/04/2025.
 */
@Composable
fun AdministrativeRecordScreen(recordId: Int, database: LabBookLiteDatabase, navController: NavController) {
    val context = LocalContext.current
    var dictionaries by remember { mutableStateOf<List<DictionaryEntity>>(emptyList()) }
    var record by remember { mutableStateOf<RecordEntity?>(null) }
    var patient by remember { mutableStateOf<PatientEntity?>(null) }
    var prescriber by remember { mutableStateOf<PrescriberEntity?>(null) }
    var requests by remember { mutableStateOf(emptyList<AnalysisRequestEntity>()) }
    var analyses by remember { mutableStateOf(emptyList<AnalysisEntity>()) }

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

        withContext(Dispatchers.IO) {
            val record = database.recordDao().getById(recordId)

            if (record != null) {
                patient = record.patient_id?.let { database.patientDao().getById(it) }
                prescriber = record.prescriber?.let { database.prescriberDao().getById(it) }
                requests = database.analysisRequestDao().getByRecord(record.id_data)
                val allAnalysis = database.analysisDao().getAll()
                analyses = allAnalysis.filter { a -> requests.any { it.analysisRef == a.id_data } }
            }
        }
    }

    if (record == null) {
        Text("Chargement du dossier...")
        return
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
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

            if (!record!!.rec_num_int.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${record!!.rec_num_int}",
                    style = MaterialTheme.typography.bodyMedium
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            navController.navigate("patient_form/${patient!!.id_data}")
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Modifier le patient")
                    }
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

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        var reportText by remember { mutableStateOf(record?.report.orEmpty()) }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Renseignements / Informations complémentaires", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = reportText,
                    onValueChange = { reportText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 6
                )

                Spacer(modifier = Modifier.height(12.dp))

                val coroutineScope = rememberCoroutineScope()

                Button(
                    onClick = {
                        val updated = record!!.copy(report = reportText)
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                database.recordDao().insert(updated)
                            }
                            Toast.makeText(context, "Commentaire enregistré", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Enregistrer le commentaire")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("record_results/$recordId")
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Résultats")
        }
    }
}
