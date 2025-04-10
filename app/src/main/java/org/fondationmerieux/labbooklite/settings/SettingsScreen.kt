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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import androidx.core.content.edit
import androidx.navigation.NavController
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import android.util.Base64
import androidx.compose.ui.res.stringResource
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.fondationmerieux.labbooklite.R
import org.fondationmerieux.labbooklite.data.adapter.DateJsonAdapter
import org.fondationmerieux.labbooklite.data.model.SetupResponse
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase

@Composable
fun SettingsScreen(database: LabBookLiteDatabase, navController: NavController) {
    var serverUrl by remember { mutableStateOf("") }
    var deviceId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

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
            onClick = {
                Log.d("LabBookLite", "Button clicked - checking inputs")
                if (serverUrl.isBlank() || deviceId.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
                } else {
                    isLoading = true
                    val fullUrl = "${serverUrl.trim().removeSuffix("/")}/services/lite/setup/load"
                    val cleanLogin = deviceId.trim()
                    val cleanPwd = password.trim()
                    fetchConfigurationPost(
                        login = cleanLogin,
                        pwd = cleanPwd,
                        context = context,
                        database = database,
                        navController = navController,
                        url = fullUrl,
                        onError = { error ->
                            isLoading = false
                            showErrorDialog = true
                            errorMessage = error
                        },
                        onSuccess = {
                            isLoading = false
                        }
                    )
                }
            },
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
                Text(stringResource(R.string.r_cup_rer_la_configuration))
            }
        }

        Button(
            onClick = {
                context.deleteDatabase("labbooklite_encrypted.db")
                Toast.makeText(context, "Database deleted", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(R.string.r_initialiser_la_base_de_donn_es), color = MaterialTheme.colorScheme.onError)
        }

        Button(
            onClick = {
                // TODO: Implement actual export logic
                Toast.makeText(context, "Fonction d’envoi à LabBook à implémenter", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.envoyer_les_donn_es_vers_labbook))
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

                    val logoBase64 = it.logo_base64
                    if (!logoBase64.isNullOrEmpty()) {
                        try {
                            val imageBytes = Base64.decode(logoBase64, Base64.DEFAULT)
                            val file = File(context.filesDir, "logo.png")
                            FileOutputStream(file).use { fos ->
                                fos.write(imageBytes)
                                fos.flush()
                            }
                            Log.d("LabBookLite", "Logo saved at ${file.absolutePath}")
                        } catch (e: Exception) {
                            Log.e("LabBookLite", "Error saving logo: ${e.message}", e)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                    Toast.makeText(context,
                        context.getString(R.string.la_configuration_a_t_r_cup_r_e_avec_succ_s), Toast.LENGTH_LONG).show()
                    navController.navigate("login") {
                        popUpTo("settings") { inclusive = true }
                    }
                }
            } else {
                val errorMsg = when (response.code) {
                    401 -> "Identifiant ou mot de passe invalide."
                    403 -> "Accès refusé. Vérifiez vos autorisations."
                    404 -> "Service non trouvé. Vérifier l'URL du serveur."
                    500 -> "Erreur de serveur interne. Réessayez plus tard."
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