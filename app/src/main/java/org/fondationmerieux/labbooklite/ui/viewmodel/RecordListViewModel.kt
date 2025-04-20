package org.fondationmerieux.labbooklite.ui.viewmodel

/**
 * Created by AlC on 10/04/2025.
 */
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.fondationmerieux.labbooklite.database.dao.RecordDao
import org.fondationmerieux.labbooklite.database.model.RecordWithPatient

class RecordListViewModel(private val recordDao: RecordDao) : ViewModel() {

    private val _records = MutableStateFlow<List<RecordWithPatient>>(emptyList())
    val records: StateFlow<List<RecordWithPatient>> = _records.asStateFlow()

    fun loadRecords() {
        viewModelScope.launch {
            _records.value = recordDao.getAllWithPatient()
        }
    }
}
