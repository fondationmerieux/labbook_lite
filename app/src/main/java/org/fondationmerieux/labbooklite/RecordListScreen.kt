package org.fondationmerieux.labbooklite

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.fondationmerieux.labbooklite.database.entity.PatientEntity
import org.fondationmerieux.labbooklite.database.entity.RecordEntity
import org.fondationmerieux.labbooklite.database.model.RecordWithPatient
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.ui.viewmodel.RecordListViewModel
import org.fondationmerieux.labbooklite.ui.viewmodel.RecordListViewModelFactory
import java.util.Calendar

@Composable
fun RecordListScreen(database: LabBookLiteDatabase, navController: NavController) {
    val factory = remember { RecordListViewModelFactory(database.recordDao()) }
    val viewModel: RecordListViewModel = viewModel(factory = factory)
    val recordList by viewModel.records.collectAsState()

    var recordNumber by remember { mutableStateOf("") }
    var dateStart by remember { mutableStateOf("") }
    var dateEnd by remember { mutableStateOf("") }
    var patientQuery by remember { mutableStateOf("") }

    val context = LocalContext.current

    LaunchedEffect(true) {
        withContext(Dispatchers.IO) {
            val patients = database.patientDao().getAll()
            val records = database.recordDao().getAll()
            Log.i("LabBookLite", "==== PATIENTS ====")
            patients.forEach { Log.i("LabBookLite", it.toString()) }
            Log.i("LabBookLite", "==== RECORDS ====")
            records.forEach { Log.i("LabBookLite", it.toString()) }
        }

        viewModel.loadRecords()

        Log.i("LabBookLite", "===== TRACE DES DONNEES ENREGISTREES =====")
        withContext(Dispatchers.IO) {
            val recordDao = database.recordDao()
            val sampleDao = database.sampleDao()
            val requestDao = database.analysisRequestDao()
            val resultDao = database.analysisResultDao()
            val validationDao = database.analysisValidationDao()

            val records = recordDao.getAll()
            val samples = sampleDao.getAll()
            val requests = requestDao.getAll()
            val results = resultDao.getAll()
            val validations = validationDao.getAll()

            Log.i("LabBookLite", "=== RECORDS ===")
            records.forEach { Log.i("LabBookLite", it.toString()) }

            Log.i("LabBookLite", "=== SAMPLES ===")
            samples.forEach { Log.i("LabBookLite", it.toString()) }

            Log.i("LabBookLite", "=== ANALYSIS_REQUEST ===")
            requests.forEach { Log.i("LabBookLite", it.toString()) }

            Log.i("LabBookLite", "=== ANALYSIS_RESULT ===")
            results.forEach { Log.i("LabBookLite", it.toString()) }

            Log.i("LabBookLite", "=== ANALYSIS_VALIDATION ===")
            validations.forEach { Log.i("LabBookLite", it.toString()) }
        }

    }

    val calendar = Calendar.getInstance()

    fun showDatePicker(onDateSelected: (String) -> Unit) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val date = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                onDateSelected(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val filteredRecords = recordList.filter { item ->
        val r = item.record
        val p = item.patient

        val query = patientQuery.trim().lowercase()
        val matchPatient = query.isEmpty() || listOf(
            p?.pat_name,
            p?.pat_firstname,
            p?.pat_code,
            p?.pat_code_lab
        ).any { field ->
            field?.lowercase()?.contains(query) == true
        }

        val matchNumber = recordNumber.isEmpty() || r.rec_num_lite?.contains(recordNumber, ignoreCase = true) == true
        val matchDateStart = dateStart.isEmpty() || (r.rec_date_receipt ?: "") >= dateStart
        val matchDateEnd = dateEnd.isEmpty() || (r.rec_date_receipt ?: "") <= dateEnd

        matchPatient && matchNumber && matchDateStart && matchDateEnd
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)) {

        Text(
            text = "Liste des dossiers",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 0.dp, bottom = 16.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = recordNumber,
                onValueChange = { recordNumber = it },
                label = { Text("Numéro de dossier") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dateStart,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date début") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showDatePicker { dateStart = it } }) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = "Choisir la date début")
                }
                OutlinedTextField(
                    value = dateEnd,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date fin") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showDatePicker { dateEnd = it } }) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = "Choisir la date fin")
                }
            }
            OutlinedTextField(
                value = patientQuery,
                onValueChange = { patientQuery = it },
                label = { Text("Recherche patient (nom, prénom, code)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        RecordTable(records = filteredRecords, navController = navController)
    }
}

@Composable
fun RecordRow(record: RecordWithPatient, navController: NavController) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val r = record.record
            val p = record.patient

            Text("Numéro : ${r.rec_num_lite ?: "?"} — ${r.rec_date_receipt ?: "?"}")
            Text("Patient : ${p?.pat_code ?: ""} ${p?.pat_name ?: ""} ${p?.pat_firstname ?: ""}")
            Text("Statut : ${r.status ?: "?"}")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = {
                    navController.navigate("record_admin/${record.record.id_data}")
                }) {
                    Text("Dossier administratif")
                }

                TextButton(onClick = { /* TODO: Résultats */ }) {
                    Text("Résultats")
                }

                TextButton(onClick = { /* TODO: Supprimer */ }) {
                    Text("Supprimer")
                }
            }
        }
    }
}

@Composable
fun RecordTable(records: List<RecordWithPatient>, navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        records.forEach { record ->
            RecordRow(record = record, navController = navController)
        }
    }
}
