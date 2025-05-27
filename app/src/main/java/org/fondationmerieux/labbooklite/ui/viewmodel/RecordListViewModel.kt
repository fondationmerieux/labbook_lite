package org.fondationmerieux.labbooklite.ui.viewmodel

/**
 * Created by AlC on 10/04/2025.
 */
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.database.dao.RecordDao
import org.fondationmerieux.labbooklite.database.model.RecordWithPatient

class RecordListViewModel(private val recordDao: RecordDao) : ViewModel() {

    private val _records = MutableStateFlow<List<RecordWithPatient>>(emptyList())
    val records: StateFlow<List<RecordWithPatient>> = _records.asStateFlow()

    private val _urgentPendingRecordIds = MutableStateFlow(setOf<Int>())
    val urgentPendingRecordIds: StateFlow<Set<Int>> = _urgentPendingRecordIds

    fun loadRecords() {
        viewModelScope.launch {
            _records.value = recordDao.getAllWithPatient()
        }
    }

    fun loadUrgentPendingRecordIds(database: LabBookLiteDatabase) {
        viewModelScope.launch {
            val ids = withContext(Dispatchers.IO) {
                database.analysisRequestDao().getRecordIdsWithUrgentUnvalidated()
            }
            _urgentPendingRecordIds.value = ids.toSet()
        }
    }

    fun deleteRecordWithDetails(
        recordId: Int,
        database: LabBookLiteDatabase,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val record = database.recordDao().getById(recordId)
                    val recordNumber = record?.rec_num_lite

                    if (record != null) {
                        // Delete validations for each result
                        val results = database.analysisResultDao().getByRecord(recordId)
                        results.forEach { result ->
                            database.analysisValidationDao().deleteByResultId(result.id)
                        }

                        // Delete results
                        database.analysisResultDao().deleteByRecord(recordId)

                        // Delete requests
                        database.analysisRequestDao().deleteByRecord(recordId)

                        // Delete samples
                        database.sampleDao().deleteByRecord(recordId)

                        // Delete the record itself
                        database.recordDao().delete(record)

                        // Delete associated PDF files
                        recordNumber?.let { num ->
                            val dir = context.filesDir
                            dir.listFiles()?.forEach { file ->
                                if (file.name.startsWith("cr_$num")) {
                                    file.delete()
                                }
                            }
                        }
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                Log.e("LabBookLite", "Failed to delete record $recordId: ${e.message}", e)
                onError("An error occurred while deleting the record")
            }
        }
    }
}
