package org.fondationmerieux.labbooklite.ui.viewmodel

/**
 * Created by AlC on 10/04/2025.
 */
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.fondationmerieux.labbooklite.database.dao.RecordDao

class RecordListViewModelFactory(
    private val recordDao: RecordDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecordListViewModel(recordDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}