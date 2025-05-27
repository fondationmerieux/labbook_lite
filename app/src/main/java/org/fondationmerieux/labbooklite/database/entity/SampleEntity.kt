package org.fondationmerieux.labbooklite.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Created by AlC on 31/03/2025.
 */
@Entity(tableName = "sample")
data class SampleEntity(
    @PrimaryKey(autoGenerate = true) val id_data: Int,
    val samp_date: Date?,
    val sample_type: Int?,
    val status: Int?,
    val record_id: Int?,
    val sampler: String?,
    val samp_receipt_date: Date?,
    val comment: String?,
    val location_id: Int?,      // lieu_prel
    val location_plus: String?, // lieu_prel_plus
    val localization: String?,
    val code: String?,
    val samp_id_ana: Int?
)
