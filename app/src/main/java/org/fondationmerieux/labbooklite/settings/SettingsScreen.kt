// Fichier : SettingsScreen.kt

package org.fondationmerieux.labbooklite.settings

import android.util.Log
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.NavController
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.fondationmerieux.labbooklite.R
import org.fondationmerieux.labbooklite.adapter.DateJsonAdapter
import org.fondationmerieux.labbooklite.database.model.SetupResponse
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.security.KeystoreHelper
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import android.util.Base64
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import org.fondationmerieux.labbooklite.database.model.PdfReport
import org.fondationmerieux.labbooklite.database.model.UploadPayload
import org.json.JSONObject
import java.util.Date

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val dbPassword = KeystoreHelper.getOrCreatePassword(context)
    val sharedPreferences = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
    var serverUrl by remember { mutableStateOf("") }
    var deviceId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var confirmFetchDialog by remember { mutableStateOf(false) }
    var confirmUploadDialog by remember { mutableStateOf(false) }

    /* 27/05/2025 disabled, useful for debugging
    val jsonImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val json = context.contentResolver.openInputStream(it)?.bufferedReader().use { reader -> reader?.readText() }
                if (!json.isNullOrEmpty()) {
                    importSetupJson(json, context, navController)
                } else {
                    Toast.makeText(context, "Fichier vide", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur lecture fichier : ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    */

    // val for upload to server
    val scope = rememberCoroutineScope()
    val uploadStatus = remember { mutableStateOf<String?>(null) }
    val db = LabBookLiteDatabase.getDatabase(context, dbPassword)

    LaunchedEffect(Unit) {
        serverUrl = sharedPreferences.getString("server_url", "") ?: ""
        deviceId = sharedPreferences.getString("device_id", "") ?: ""
    }

    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Configuration", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = serverUrl,
            onValueChange = { serverUrl = it },
            label = { Text(stringResource(R.string.url_serveur_labbook)) },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                sharedPreferences.edit {
                    putString("server_url", serverUrl)
                    putString("device_id", deviceId)
                }
                Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.enregistrer))
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = deviceId,
            onValueChange = { deviceId = it },
            label = { Text(stringResource(R.string.identifiant)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.mot_de_passe)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            onClick = { confirmFetchDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.recuperer_la_configuration))
            }
        }

        if (confirmFetchDialog) {
            AlertDialog(
                onDismissRequest = { confirmFetchDialog = false },
                title = { Text("Confirmer") },
                text = { Text("Cette opération va écraser toutes les données locales actuelles. Voulez-vous continuer ?") },
                confirmButton = {
                    TextButton(onClick = {
                        confirmFetchDialog = false
                        isLoading = true

                        scope.launch {
                            withContext(Dispatchers.IO) {
                                val database = LabBookLiteDatabase.getDatabase(context, dbPassword)

                                // 1. clean database
                                database.clearAllTables()

                                // 2. clean PDF reports
                                context.filesDir.listFiles()?.forEach { file ->
                                    if (file.name.startsWith("cr_") && file.name.endsWith(".pdf")) {
                                        file.delete()
                                    }
                                }
                            }

                            val fullUrl =
                                "${serverUrl.trim().removeSuffix("/")}/services/lite/setup/load"
                            val cleanLogin = deviceId.trim()
                            val cleanPwd = password.trim()
                            val database = LabBookLiteDatabase.getDatabase(context, dbPassword)

                            fetchConfigurationPost(
                                login = cleanLogin,
                                pwd = cleanPwd,
                                context = context,
                                database = database,
                                navController = navController,
                                url = fullUrl,
                                onError = {
                                    isLoading = false
                                    showErrorDialog = true
                                    errorMessage = it
                                },
                                onSuccess = {
                                    isLoading = false
                                }
                            )
                        }
                    }) {
                        Text("Oui")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmFetchDialog = false }) {
                        Text("Non")
                    }
                }
            )
        }

        /* 27/05/2025 disabled, useful for debugging
        Button(
            onClick = {
                jsonImportLauncher.launch("application/json")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00796B),
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.BugReport, contentDescription = "debug")
            Spacer(Modifier.width(8.dp))
            Text("Importer une configuration JSON (DEBUG)", fontWeight = FontWeight.Bold)
        }
        */

        val currentStatus = uploadStatus.value

        Button(
            onClick = { confirmUploadDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = currentStatus == null || currentStatus.startsWith("Transfert terminé") || currentStatus.startsWith("Erreur")
        ) {
            Text(stringResource(R.string.envoyer_les_donnees_vers_labbook))
        }

        if (confirmUploadDialog) {
            AlertDialog(
                onDismissRequest = { confirmUploadDialog = false },
                title = { Text("Confirmer") },
                text = { Text("Les nouvelles données vont être transférées, puis effacées localement. Voulez-vous continuer ?") },
                confirmButton = {
                    TextButton(onClick = {
                        confirmUploadDialog = false
                        uploadStatus.value = "Envoi des rapports PDF..."
                        scope.launch {
                            val pdfOk = uploadPdfReports(context, serverUrl, deviceId, password) { error ->
                                errorMessage = error
                                showErrorDialog = true
                            }

                            if (pdfOk) {
                                uploadStatus.value = "Rapports transférés. Envoi des données..."
                                val dataOk = uploadAllDataToServer(
                                    context, db, serverUrl, deviceId, password, navController
                                ) { error ->
                                    errorMessage = error
                                    showErrorDialog = true
                                }

                                uploadStatus.value = if (dataOk) {
                                    "Transfert terminé avec succès."
                                } else {
                                    "Erreur lors de l'envoi des données."
                                }
                            } else {
                                uploadStatus.value = "Erreur lors de l'envoi des rapports PDF."
                            }
                        }
                    }) {
                        Text("Oui")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmUploadDialog = false }) {
                        Text("Non")
                    }
                }
            )
        }

        uploadStatus.value?.let { status ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = status, color = Color.DarkGray)
        }
    }
}

fun fetchConfigurationPost(
    url: String,
    login: String,
    pwd: String,
    context: Context,
    database: LabBookLiteDatabase,
    navController: NavController,
    onError: (String) -> Unit,
    onSuccess: () -> Unit
) {
    Log.d("LabBookLite", "→ fetchConfigurationPost function called")

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val json = """{"login": "$login", "pwd": "$pwd"}"""
            val body = json.toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            val client = OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Log.d("LabBookLite", "HTTP code: ${response.code}")
            Log.d("LabBookLite", "Response body: $responseBody")

            if (response.isSuccessful && responseBody != null) {
                val moshi = Moshi.Builder()
                    .add(DateJsonAdapter())
                    .add(KotlinJsonAdapterFactory())
                    .build()
                val adapter = moshi.adapter(SetupResponse::class.java)
                val setup = adapter.fromJson(responseBody)

                setup?.let {
                    database.userDao().insertAll(it.users)
                    database.patientDao().insertAll(it.patients)
                    database.prescriberDao().insertAll(it.prescribers)
                    database.preferencesDao().insertAll(it.preferences)
                    database.nationalityDao().insertAll(it.nationality)
                    database.dictionaryDao().insertAll(it.dictionary)
                    database.analysisDao().insertAll(it.analysis)
                    database.anaLinkDao().insertAll(it.ana_link)
                    database.anaVarDao().insertAll(it.ana_var)
                    database.sampleDao().insertAll(it.sample)
                    database.recordDao().insertAll(it.record)
                    database.analysisRequestDao().insertAll(it.analysis_request)
                    database.analysisResultDao().insertAll(it.analysis_result)
                    database.analysisValidationDao().insertAll(it.analysis_validation)

                    val prefs = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
                    prefs.edit() {
                        putInt("lite_ser", it.lite_ser)
                        putString("lite_name", it.lite_name)
                        putString("lite_report_pwd", it.lite_report_pwd)
                    }

                    val logoBase64 = it.logo_base64
                    if (!logoBase64.isNullOrEmpty()) {
                        try {
                            val imageBytes = Base64.decode(logoBase64, Base64.DEFAULT)
                            val file = File(context.filesDir, "logo.png")
                            FileOutputStream(file).use { fos ->
                                fos.write(imageBytes)
                                fos.flush()
                            }
                            //Log.d("LabBookLite", "Logo saved at ${file.absolutePath}")
                        } catch (e: Exception) {
                            Log.e("LabBookLite", "Error saving logo: ${e.message}", e)
                        }
                    }

                    val userCount = database.userDao().getAll().size
                    val analysisCount = database.analysisDao().getAll().size
                    val prescriberCount = database.prescriberDao().getAll().size

                    Log.d(
                        "LabBookLite",
                        "AFTER INSERT - user=$userCount, analysis=$analysisCount, prescriber=$prescriberCount"
                    )
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                    Toast.makeText(
                        context,
                        context.getString(R.string.la_configuration_a_ete_recuperee_avec_succes),
                        Toast.LENGTH_LONG
                    ).show()
                    navController.navigate("login") {
                        popUpTo("settings") { inclusive = true }
                    }
                }
            } else {
                val errorMsg = when (response.code) {
                    401 -> "Invalid credentials."
                    403 -> "Access denied."
                    404 -> "Service not found."
                    500 -> "Internal server error."
                    else -> "HTTP error ${response.code}: ${responseBody ?: "Empty response"}"
                }

                Log.e("LabBookLite", "Configuration error: $errorMsg")
                withContext(Dispatchers.Main) {
                    onError(errorMsg)
                }
            }
        } catch (e: Exception) {
            Log.e("LabBookLite", "Request exception: ${e.message}", e)
            withContext(Dispatchers.Main) {
                onError("Error: ${e.message}")
            }
        }
    }
}

/* 27/05/2025 disabled, useful for debugging
fun importSetupJson(json: String, context: Context, navController: NavController) {
    val dbPassword = KeystoreHelper.getOrCreatePassword(context)
    val database = LabBookLiteDatabase.getDatabase(context, dbPassword)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val moshi = Moshi.Builder()
                .add(DateJsonAdapter())
                .add(KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(SetupResponse::class.java)
            val setup = adapter.fromJson(json)

            setup?.let {
                database.userDao().insertAll(it.users)
                database.patientDao().insertAll(it.patients)
                database.prescriberDao().insertAll(it.prescribers)
                database.preferencesDao().insertAll(it.preferences)
                database.nationalityDao().insertAll(it.nationality)
                database.dictionaryDao().insertAll(it.dictionary)
                database.analysisDao().insertAll(it.analysis)
                database.anaLinkDao().insertAll(it.ana_link)
                database.anaVarDao().insertAll(it.ana_var)
                database.sampleDao().insertAll(it.sample)
                database.recordDao().insertAll(it.record)
                database.analysisRequestDao().insertAll(it.analysis_request)
                database.analysisResultDao().insertAll(it.analysis_result)
                database.analysisValidationDao().insertAll(it.analysis_validation)

                val prefs = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
                prefs.edit {
                    putInt("lite_ser", it.lite_ser)
                    putString("lite_name", it.lite_name)
                    putString("lite_report_pwd", it.lite_report_pwd)

                    // Save fallback configuration values
                    putString("device_id", "imported-device")
                    putString("server_url", "from-json-import")
                }

                it.logo_base64?.let { logoBase64 ->
                    try {
                        val imageBytes = android.util.Base64.decode(logoBase64, android.util.Base64.DEFAULT)
                        val file = File(context.filesDir, "logo.png")
                        FileOutputStream(file).use { fos -> fos.write(imageBytes) }
                        //Log.d("LabBookLite", "Logo imported from JSON at ${file.absolutePath}")
                    } catch (e: Exception) {
                        Log.e("LabBookLite", "Logo import error: ${e.message}", e)
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Configuration importée avec succès", Toast.LENGTH_LONG).show()

                    navController.navigate("login") {
                        popUpTo("settings") { inclusive = true }
                    }
                }
            } ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Erreur de lecture du fichier JSON", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("LabBookLite", "Erreur import JSON: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Erreur lors de l'import : ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
*/

suspend fun uploadPdfReports(context: Context, serverUrl: String, deviceId: String, password: String, onError: (String) -> Unit): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val pdfFiles = context.filesDir.listFiles()?.filter {
                it.name.startsWith("cr_") && it.name.endsWith(".pdf")
            } ?: emptyList()

            val pdfPayloads = pdfFiles.map { file ->
                val content = file.readBytes()
                val encoded = Base64.encodeToString(content, Base64.NO_WRAP)

                mapOf(
                    "record_number" to file.name.removePrefix("cr_").removeSuffix(".pdf"),
                    "filename" to file.name,
                    "content_base64" to encoded
                )
            }

            val payload = mapOf(
                "login" to deviceId,
                "pwd" to password,
                "pdf_files" to pdfPayloads
            )


            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter(Map::class.java)
            val json = adapter.toJson(payload)

            val client = OkHttpClient()
            val request = Request.Builder()
                .url("$serverUrl/services/lite/recovery/report")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                if (json.optBoolean("success", false)) {
                    return@withContext true
                } else {
                    val errorMsg = json.optString("error", "Erreur lors de l'envoi des fichiers PDF.")
                    Log.e("LabBookLite", "PDF upload error: $errorMsg")
                    withContext(Dispatchers.Main) {
                        onError(errorMsg)
                    }
                    return@withContext false
                }
            } else {
                Log.e("LabBookLite", "Réponse HTTP invalide: ${response.code}")
                withContext(Dispatchers.Main) {
                    onError("Erreur serveur : ${response.code}")
                }
                return@withContext false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

suspend fun uploadAllDataToServer(
    context: Context,
    db: LabBookLiteDatabase,
    serverUrl: String,
    deviceId: String,
    password: String,
    navController: NavController,
    onError: (String) -> Unit
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val patients = db.patientDao().getAll()
            val records = db.recordDao().getAll()
            val samples = db.sampleDao().getAll()
            val requests = db.analysisRequestDao().getAll()
            val results = db.analysisResultDao().getAll()
            val validations = db.analysisValidationDao().getAll()

            val filteredPatients = patients.filter {
                !it.pat_code.isNullOrBlank() &&
                        it.pat_code.startsWith("LT") &&
                        it.pat_lite > 0
            }

            val pdfReports = context.filesDir.listFiles()?.filter {
                it.name.startsWith("cr_") && it.name.endsWith(".pdf")
            }?.mapNotNull { file ->
                val matchingRecord = records.find { r ->
                    file.name.startsWith("cr_${r.rec_num_lite}")
                }

                matchingRecord?.let { rec ->
                    PdfReport(
                        filename = file.name,
                        recordId = rec.id_data,
                        creationDate = DateJsonAdapter().toJson(Date(file.lastModified()))!!
                    )
                }
            } ?: emptyList()

            val payload = UploadPayload(
                login = deviceId,
                pwd = password,
                patients = filteredPatients,
                records = records,
                samples = samples,
                analysis_request = requests,
                analysis_result = results,
                analysis_validation = validations,
                pdf_reports = pdfReports
            )

            val moshi = Moshi.Builder()
                .add(DateJsonAdapter())
                .add(KotlinJsonAdapterFactory())
                .build()

            val json = moshi.adapter(UploadPayload::class.java).toJson(payload)

            val client = OkHttpClient()
            val request = Request.Builder()
                .url("$serverUrl/services/lite/recovery/data")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val success = response.isSuccessful

            if (success) {
                // 1. Clear some tables
                db.recordDao().deleteAll()
                db.analysisRequestDao().deleteAll()
                db.analysisResultDao().deleteAll()
                db.analysisValidationDao().deleteAll()
                db.sampleDao().deleteAll()

                // 2. Delete all report PDFs
                context.filesDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("cr_") && file.name.endsWith(".pdf")) {
                        file.delete()
                    }
                }
            }

            success
        } catch (e: Exception) {
            Log.e("LabBookLite", "Erreur envoi données : ${e.message}", e)
            withContext(Dispatchers.Main) {
                onError("Erreur lors de l'envoi des données : ${e.message}")
            }
            false
        }
    }
}
