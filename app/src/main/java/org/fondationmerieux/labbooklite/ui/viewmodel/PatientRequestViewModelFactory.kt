package org.fondationmerieux.labbooklite.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.repository.RecordRepository

class PatientRequestViewModelFactory(
    private val application: Application,
    private val database: LabBookLiteDatabase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PatientRequestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PatientRequestViewModel(
                application = application,
                repository = RecordRepository(application, database)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}