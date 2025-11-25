package org.fondationmerieux.labbooklite

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
import kotlinx.coroutines.withContext
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.database.entity.AnalysisEntity
import org.fondationmerieux.labbooklite.database.entity.AnalysisRequestEntity
import org.fondationmerieux.labbooklite.database.entity.DictionaryEntity
import org.fondationmerieux.labbooklite.database.entity.PatientEntity
import org.fondationmerieux.labbooklite.database.entity.RecordEntity
import org.fondationmerieux.labbooklite.database.entity.PrescriberEntity
import java.io.File
import androidx.compose.ui.res.stringResource
import org.fondationmerieux.labbooklite.cards.AdditionalInfoCard
import org.fondationmerieux.labbooklite.cards.AnalysesCard
import org.fondationmerieux.labbooklite.cards.PatientInfoCard
import org.fondationmerieux.labbooklite.cards.PrescriptionInfoCard
import org.fondationmerieux.labbooklite.cards.ReportCard
import org.fondationmerieux.labbooklite.cards.SamplingActsCard

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
            val recordData = database.recordDao().getById(recordId)
            if (recordData != null) {
                record = recordData
                patient = recordData.patient_id?.let { database.patientDao().getById(it) }
                prescriber = recordData.prescriber?.let { database.prescriberDao().getById(it) }
                requests = database.analysisRequestDao().getByRecord(recordData.id_data)
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
            .padding(horizontal = 8.dp, vertical = 8.dp)
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

        PatientInfoCard(
            patient = patient,
            dictionaries = dictionaries,
            navController = navController
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrescriptionInfoCard(record = record!!, prescriber = prescriber)

        Spacer(modifier = Modifier.height(16.dp))

        AnalysesCard(analyses = analyses, requests = requests)

        Spacer(modifier = Modifier.height(16.dp))

        SamplingActsCard(analyses = analyses)

        val pdfFiles = remember {
            mutableStateListOf<File>()
        }

        fun reloadPdfFiles() {
            val matchingFiles = context.filesDir.listFiles { file ->
                file.name.startsWith("cr_${record!!.rec_num_lite}") && file.name.endsWith(".pdf")
            } ?: emptyArray()

            pdfFiles.clear()
            pdfFiles.addAll(matchingFiles.sortedBy { it.lastModified() })
        }

        LaunchedEffect(Unit) {
            reloadPdfFiles()
        }

        if (pdfFiles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            ReportCard(
                recordId = recordId,
                database = database,
                context = context,
                pdfFiles = pdfFiles,
                reloadPdfFiles = ::reloadPdfFiles
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        AdditionalInfoCard(record = record!!, database = database)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = { navController.popBackStack() }) {
                Text(stringResource(R.string.retour))
            }

            Button(
                onClick = {
                    navController.navigate("record_results/$recordId")
                }
            ) {
                Text("Résultats")
            }
        }
    }
}
