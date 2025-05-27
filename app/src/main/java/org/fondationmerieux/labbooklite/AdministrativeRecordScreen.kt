package org.fondationmerieux.labbooklite

import android.content.ActivityNotFoundException
import android.content.Context
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
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.core.content.FileProvider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import org.fondationmerieux.labbooklite.database.model.generateReportHeaderPdf
import java.util.Date
import java.util.Locale
import androidx.compose.ui.res.stringResource

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

        val pdfFiles = remember {
            mutableStateListOf<File>().apply {
                val matchingFiles = context.filesDir.listFiles { file ->
                    file.name.startsWith("cr_${record!!.rec_num_lite}") && file.name.endsWith(".pdf")
                }?.sortedBy { it.lastModified() } ?: emptyList()

                clear()
                addAll(matchingFiles)
            }
        }

        fun reloadPdfFiles() {
            val matchingFiles = context.filesDir.listFiles { file ->
                file.name.startsWith("cr_${record!!.rec_num_lite}") && file.name.endsWith(".pdf")
            } ?: emptyArray()

            pdfFiles.clear()
            pdfFiles.addAll(matchingFiles.sortedBy { it.lastModified() })
        }

        // Initial load
        LaunchedEffect(Unit) {
            reloadPdfFiles()
        }

        if (pdfFiles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Compte rendu", style = MaterialTheme.typography.titleMedium)

                    pdfFiles.forEachIndexed { index, file ->
                        val isLast = index == pdfFiles.lastIndex

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(file.name, modifier = Modifier.weight(1f))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    val success = copyToDownloads(context, file.name)
                                    val msg = if (success) "Fichier copié vers Téléchargements" else "Erreur de copie"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.FileDownload, contentDescription = "Télécharger", tint = Color(0xFF006B8F))
                                }

                                IconButton(onClick = {
                                    //Log.i("LabBookLite", "Opening PDF file: ${file.name}")
                                    openPdfFile(context, file)
                                }) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = "Ouvrir", tint = Color(0xFF006B8F))
                                }

                                var showConfirmDialog by remember { mutableStateOf<File?>(null) }

                                if (showConfirmDialog != null) {
                                    AlertDialog(
                                        onDismissRequest = { showConfirmDialog = null },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                showConfirmDialog?.delete()
                                                reloadPdfFiles()
                                                Toast.makeText(context, "Fichier supprimé", Toast.LENGTH_SHORT).show()
                                                showConfirmDialog = null
                                            }) {
                                                Text("Supprimer")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showConfirmDialog = null }) {
                                                Text("Annuler")
                                            }
                                        },
                                        title = { Text("Confirmer la suppression") },
                                        text = { Text("Voulez-vous vraiment supprimer le fichier ${showConfirmDialog?.name} ?") }
                                    )
                                }

                                IconButton(onClick = {
                                    showConfirmDialog = file
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Red)
                                }

                                if (isLast) {
                                    IconButton(onClick = {
                                        val lastModified = file.lastModified() // Long timestamp
                                        val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
                                        val formattedDate = dateFormatter.format(Date(lastModified))

                                        val base = file.nameWithoutExtension
                                            .removeSuffix("_reedit")
                                            .substringBefore("_reedit-")

                                        val baseFileName = base.ifBlank { file.nameWithoutExtension }

                                        //Log.i("LabBookLite", "Base filename: $baseFileName")

                                        var counter = 0
                                        var newFile: File
                                        do {
                                            val suffix = "_reedit-${counter + 1}"
                                            val name = "$baseFileName$suffix.pdf"
                                            newFile = File(context.filesDir, name)
                                            counter++
                                        } while (newFile.exists())

                                        val newName = newFile.name
                                        //Log.i("LabBookLite", "Generating reedited PDF: $newName")
                                        //Log.i("LabBookLite", "previous date : $formattedDate")

                                        generateReportHeaderPdf(
                                            context = context,
                                            filename = newName,
                                            database = database,
                                            recordId = recordId,
                                            reedit = "Y",
                                            previousFilename = file.name,
                                            previousDate = formattedDate
                                        )

                                        reloadPdfFiles()

                                        Toast.makeText(context, "Reedited PDF generated: $newName", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Rééditer", tint = Color(0xFF006B8F))
                                    }
                                }
                            }
                        }
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

fun openPdfFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Aucun lecteur PDF trouvé", Toast.LENGTH_SHORT).show()
    }
}

fun copyToDownloads(context: Context, filename: String): Boolean {
    val sourceFile = File(context.filesDir, filename)
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val targetFile = File(downloadsDir, filename)

    return try {
        FileInputStream(sourceFile).use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
