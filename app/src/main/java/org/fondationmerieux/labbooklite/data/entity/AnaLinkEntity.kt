package org.fondationmerieux.labbooklite.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by AlC on 31/03/2025.
 */
@Entity(tableName = "ana_link")
data class AnaLinkEntity(
    @PrimaryKey val id_data: Int,
    val analysis_id: Int?,
    val variable_id: Int?,
    val position: Int?,
    val var_number: Int?,
    val required: Int?,
    val var_whonet: Int?,
    val var_qrcode: String?
)
