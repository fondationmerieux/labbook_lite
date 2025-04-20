package org.fondationmerieux.labbooklite.ui.viewmodel

/**
 * Created by AlC on 17/04/2025.
 */
import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.fondationmerieux.labbooklite.database.model.AnalysisRequestPayload
import org.fondationmerieux.labbooklite.database.model.AnalysisResultPayload
import org.fondationmerieux.labbooklite.database.model.RecordPayload
import org.fondationmerieux.labbooklite.database.model.SamplePayload
import org.fondationmerieux.labbooklite.repository.RecordRepository
import android.util.Log
import androidx.lifecycle.AndroidViewModel

class PatientRequestViewModel(
    application: Application,
    private val repository: RecordRepository
) : AndroidViewModel(application) {

    fun submitPatientRequest(
        record: RecordPayload,
        analyses: List<AnalysisRequestPayload>,
        acts: List<AnalysisRequestPayload>,
        samples: List<SamplePayload>,
        results: List<AnalysisResultPayload>,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val prefs = getApplication<Application>().getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
                val recLite = prefs.getInt("rec_lite", 1) // Default to 1 if not set

                Log.i("LabBookLite PatientRequest", "== RECORD ==")
                Log.i("LabBookLite PatientRequest", "Record: $record")
                Log.i("LabBookLite PatientRequest", "Analyses: ${analyses.size} → ${analyses.joinToString { it.analysisId.toString() }}")
                Log.i("LabBookLite PatientRequest", "Actes: ${acts.size} → ${acts.joinToString { it.analysisId.toString() }}")
                Log.i("LabBookLite PatientRequest", "Samples: ${samples.size} → ${samples.joinToString { it.analysisId.toString() }}")
                Log.i("LabBookLite PatientRequest", "Results: ${results.size} → ${results.joinToString { it.analysisId.toString() }}")

                repository.savePatientRequest(record, analyses, acts, samples, results, recLite)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun logPatientRequest(
        record: RecordPayload,
        analyses: List<AnalysisRequestPayload>,
        acts: List<AnalysisRequestPayload>,
        samples: List<SamplePayload>,
        results: List<AnalysisResultPayload>
    ) {
        Log.i("LabBookLite", "== RECORD ==")
        Log.i("LabBookLite", record.toString())

        Log.i("LabBookLite", "== ANALYSES ==")
        analyses.forEach { Log.i("LabBookLite", it.toString()) }

        Log.i("LabBookLite", "== ACTS ==")
        acts.forEach { Log.i("LabBookLite", it.toString()) }

        Log.i("LabBookLite", "== SAMPLES ==")
        samples.forEach { Log.i("LabBookLite", it.toString()) }

        Log.i("LabBookLite", "== RESULTS ==")
        results.forEach { Log.i("LabBookLite", it.toString()) }
    }
}
