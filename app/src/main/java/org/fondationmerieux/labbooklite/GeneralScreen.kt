package org.fondationmerieux.labbooklite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import androidx.compose.material3.OutlinedButton

/**
 * Created by AlC on 19/04/2025.
 */
@Composable
fun GeneralScreen(database: LabBookLiteDatabase, navController: NavController) {
    val context = LocalContext.current

    var patientCount by remember { mutableIntStateOf(0) }
    var analysisCount by remember { mutableIntStateOf(0) }
    var recordCount by remember { mutableIntStateOf(0) }
    var sampleCount by remember { mutableIntStateOf(0) }
    var lastRecordNumber by remember { mutableStateOf<String?>(null) }
    var lastRecordDate by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            patientCount = database.patientDao().count()
            analysisCount = database.analysisDao().countWithoutPB()
            recordCount = database.recordDao().count()
            sampleCount = database.sampleDao().count()

            val lastRecord = database.recordDao().getLastRecord()
            val number = lastRecord?.rec_num_lite?.takeLast(4)?.toIntOrNull()
            lastRecordNumber = number?.toString() ?: lastRecord?.id_data?.toString()
            lastRecordDate = lastRecord?.rec_date_save
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Nombre de patients disponibles : $patientCount")
        Text("Nombre d’analyses disponibles : $analysisCount")
        Text("Nombre de dossiers créés : $recordCount")
        Text("Nombre d’échantillons créés : $sampleCount")

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dernier dossier "
            )
            Box(
                modifier = Modifier
                    .background(color = Color(0xFFC7AD70), shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = lastRecordNumber ?: "N/A",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
        }
        Text("Date de création : ${lastRecordDate ?: "N/A"}")

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            OutlinedButton(onClick = {
                navController.popBackStack()
            }) {
                Text("Retour")
            }
        }
    }
}