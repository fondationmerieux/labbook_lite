package org.fondationmerieux.labbooklite.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by AlC on 31/03/2025.
 */
@Entity(tableName = "preferences")
data class PreferencesEntity(
    @PrimaryKey val id_data: Int,
    val key: String?,   // identifiant in LabBook DB
    val label: String?,
    val value: String?
)
