package org.fondationmerieux.labbooklite.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import androidx.core.content.edit

/**
 * Created by AlC on 19/03/2025.
 */
@Composable
fun SettingsScreen() {
    var serverUrl by remember { mutableStateOf("") }
    var deviceId by remember { mutableStateOf("") }
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)

    // Charger l'URL sauvegardée
    LaunchedEffect(Unit) {
        serverUrl = sharedPreferences.getString("server_url", "") ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Paramétrages", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = serverUrl,
            onValueChange = { serverUrl = it },
            label = { Text("URL du serveur LabBook") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                sharedPreferences.edit() { putString("server_url", serverUrl) }
                Toast.makeText(context, "URL enregistrée", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enregistrer l'URL")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = deviceId,
            onValueChange = { deviceId = it },
            label = { Text("Identifiant") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val fullUrl = "$serverUrl/config_suffix" // Remplace "config_suffix" par ton suffixe fixe
                fetchConfiguration(fullUrl, deviceId, context)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Récupérer la configuration")
        }
    }
}

fun fetchConfiguration(url: String, deviceId: String, context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val fullUrl = "$url?user_id=$deviceId" // Construit l'URL avec l'ID device
            val request = Request.Builder().url(fullUrl).build()
            val client = OkHttpClient()
            val response = client.newCall(request).execute()

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Configuration récupérée avec succès", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Erreur : ${response.code}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Échec de la requête", Toast.LENGTH_LONG).show()
            }
        }
    }
}
