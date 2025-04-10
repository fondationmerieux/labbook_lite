package org.fondationmerieux.labbooklite.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by AlC on 31/03/2025.
 */
@Entity(tableName = "dictionary")
data class DictionaryEntity(
    @PrimaryKey val id_data: Int,
    val dico_name: String?,
    val label: String?,
    val short_label: String?,
    val position: Int?,
    val code: String?,
    val dico_descr: String?,
    val dict_formatting: String?
)
