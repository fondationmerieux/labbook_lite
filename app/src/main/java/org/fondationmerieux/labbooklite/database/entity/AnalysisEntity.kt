package org.fondationmerieux.labbooklite.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by AlC on 31/03/2025.
 */
@Entity(tableName = "analysis")
data class AnalysisEntity(
    @PrimaryKey val id_data: Int,
    val code: String?,
    val name: String?,
    val abbr: String?,
    val family: Int?,
    val comment: String?,
    val bio_product: Int?,           // produit_biologique
    val sample_type: Int?,           // type_prel
    val analysis_type: Int?,         // type_analyse
    val active: Int?,
    val ana_loinc: String?
)

