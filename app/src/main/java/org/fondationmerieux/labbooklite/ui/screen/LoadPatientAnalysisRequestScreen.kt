package org.fondationmerieux.labbooklite.ui.screen

/**
 * Created by AlC on 09/04/2025.
 */
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.fondationmerieux.labbooklite.data.entity.PatientEntity
import org.fondationmerieux.labbooklite.data.entity.DictionaryEntity
import org.fondationmerieux.labbooklite.data.model.AnalysisWithFamily
import org.fondationmerieux.labbooklite.PatientAnalysisRequestScreen
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LoadPatientAnalysisRequestScreen(
    navController: NavController,
    patient: PatientEntity,
    dictionaries: List<DictionaryEntity>,
    database: LabBookLiteDatabase
) {
    val db = database
    var analysisList by remember { mutableStateOf<List<AnalysisWithFamily>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val result = db.analysisDao().getAllWithFamily()
            Log.d("LoadScreen", "Analyses loaded: ${result.size}")
            analysisList = result
        }
    }

    if (analysisList.isNotEmpty()) {
        PatientAnalysisRequestScreen(
            navController = navController,
            patient = patient,
            dictionaries = dictionaries,
            analyses = analysisList
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
