package org.fondationmerieux.labbooklite.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by AlC on 31/03/2025.
 */
@Entity(tableName = "ana_var")
data class AnaVarEntity(
    @PrimaryKey val id_data: Int,
    val label: String?,
    val description: String?,
    val unit: Int?,
    val normal_min: String?,
    val normal_max: String?,
    val comment: String?,
    val result_type: Int?,
    val unit2: Int?,
    val formula_unit2: String?,
    val formula: String?,
    val accuracy: Int?,
    val accuracy2: Int?,    // precision2
    val var_code: String?,
    val var_highlight: String?,
    val var_show_minmax: String?
)
