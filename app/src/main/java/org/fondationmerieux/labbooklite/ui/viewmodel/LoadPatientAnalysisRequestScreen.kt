package org.fondationmerieux.labbooklite.ui.viewmodel

/**
 * Created by AlC on 09/04/2025.
 */
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.fondationmerieux.labbooklite.database.entity.PatientEntity
import org.fondationmerieux.labbooklite.database.entity.DictionaryEntity
import org.fondationmerieux.labbooklite.database.model.AnalysisWithFamily
import org.fondationmerieux.labbooklite.PatientAnalysisRequestScreen
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.fondationmerieux.labbooklite.database.entity.PrescriberEntity

@Composable
fun LoadPatientAnalysisRequestScreen(
    navController: NavController,
    patient: PatientEntity,
    dictionaries: List<DictionaryEntity>,
    database: LabBookLiteDatabase
) {
    val db = database
    var analysisList by remember { mutableStateOf<List<AnalysisWithFamily>>(emptyList()) }
    var prescriberList by remember { mutableStateOf<List<PrescriberEntity>>(emptyList()) }


    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val result = db.analysisDao().getAllWithFamily()
            // Debug to verify which analyses are loaded from Room
            result.forEach {
                Log.d("AnalysisFromRoom", "id=${it.id}, code=${it.code}")
            }

            Log.d("LoadScreen", "Analyses loaded: ${result.size}")
            analysisList = result

            // Load prescribers
            val prescribers = db.prescriberDao().getAll()
            Log.d("LoadScreen", "Prescribers loaded: ${prescribers.size}")
            prescriberList = prescribers
        }
    }

    if (analysisList.isNotEmpty()) {
        PatientAnalysisRequestScreen(
            navController = navController,
            patient = patient,
            dictionaries = dictionaries,
            analyses = analysisList,
            prescribers = prescriberList,
            database = database
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                // Text("Loading analyses: ${analysisList.size}")
            }
        }
    }
}
