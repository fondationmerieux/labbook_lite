package org.fondationmerieux.labbooklite

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    val urgentPendingRecordIds by viewModel.urgentPendingRecordIds.collectAsState()

    LaunchedEffect(recordList) {
        Log.i("LabBookLite", "===== RECORDS FROM VIEWMODEL =====")
        recordList.forEach { rw ->
            val r = rw.record
            val p = rw.patient
            Log.i("LabBookLite", "Record ${r.id_data} (${r.rec_num_lite}) → patient_id=${r.patient_id} → ${p?.id_data} ${p?.pat_code} ${p?.pat_name}")
        }
    }

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
        viewModel.loadUrgentPendingRecordIds(database)

        Log.i("LabBookLite", "===== TRACE DES DONNEES ENREGISTREES =====")
        withContext(Dispatchers.IO) {
            /*
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
            */
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

        RecordTable(
            records = filteredRecords,
            database = database,
            navController = navController,
            urgentPendingRecordIds = urgentPendingRecordIds
        )
    }
}

@Composable
fun RecordRow(record: RecordWithPatient, database: LabBookLiteDatabase, navController: NavController, urgentPendingRecordIds: Set<Int>) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = if (urgentPendingRecordIds.contains(record.record.id_data)) Color.Red else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
    )
    {
        Column(modifier = Modifier.padding(12.dp)) {
            val r = record.record
            val p = record.patient
            val context = LocalContext.current
            val viewModel: RecordListViewModel = viewModel()

            val recordNumber = r.rec_num_lite?.takeLast(4)?.toIntOrNull()?.toString() ?: r.id_data.toString()
            val statusLabel = when (r.status) {
                182 -> Triple("A", Color(0xFFF08739), Color(0xFF000000))
                253 -> Triple("I", Color(0xFFF08739), Color(0xFF000000))
                254 -> Triple("T", Color(0xFF00ADEE), Color(0xFF000000))
                255 -> Triple("I", Color(0xFF00ADEE), Color(0xFF000000))
                256 -> Triple("B", Color(0xFF8330E2), Color.White)
                else -> Triple("?", Color.LightGray, Color.Black)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(statusLabel.second, shape = MaterialTheme.shapes.small)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusLabel.first,
                            color = statusLabel.third,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFFC7AD70), shape = MaterialTheme.shapes.small)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = recordNumber,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (!r.rec_num_int.isNullOrBlank()) {
                        Text(
                            text = "(${r.rec_num_int})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        text = r.rec_date_receipt ?: "—",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = stringResource(R.string.patient) + " : ${p?.pat_code ?: ""} ${p?.pat_name ?: ""} ${p?.pat_firstname ?: ""}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = {
                    navController.navigate("record_admin/${record.record.id_data}")
                }) {
                    Text("Dossier administratif")
                }

                TextButton(onClick = {
                    navController.navigate("record_results/${record.record.id_data}")
                }) {
                    Text("Résultats")
                }

                var showDialog by remember { mutableStateOf(false) }

                IconButton(onClick = { showDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = Color.Red
                    )
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Delete confirmation") },
                        text = { Text("Are you sure you want to delete this record? This action cannot be undone.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showDialog = false
                                viewModel.deleteRecordWithDetails(
                                    recordId = record.record.id_data,
                                    database = database, // make sure `database` is passed down to RecordRow
                                    context = context,
                                    onSuccess = { viewModel.loadRecords() },
                                    onError = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }) {
                                Text("Yes", color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RecordTable(records: List<RecordWithPatient>, database: LabBookLiteDatabase, navController: NavController, urgentPendingRecordIds: Set<Int>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(records) { record ->
            RecordRow(record = record, database = database, navController = navController, urgentPendingRecordIds = urgentPendingRecordIds)
        }
    }
}
