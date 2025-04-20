package org.fondationmerieux.labbooklite.database.model

/**
 * Created by AlC on 16/04/2025.
 */
data class RecordPayload(
    val recordNumber: String,
    val recordLiteNumber: String,
    val patientId: Int,
    val prescriberId: Int?,
    val receivedDate: String,
    val receivedTime: String,
    val prescriptionDate: String,
    val comment: String,
    val status: Int = 182
)