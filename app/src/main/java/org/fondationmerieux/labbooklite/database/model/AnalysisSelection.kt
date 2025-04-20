package org.fondationmerieux.labbooklite.database.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class AnalysisSelection(
    val id: Int,
    val code: String,
    val name: String,
    val isUrgent: MutableState<Boolean> = mutableStateOf(false)
)
