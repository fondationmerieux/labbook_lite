package org.fondationmerieux.labbooklite.data.model

/**
 * Created by AlC on 09/04/2025.
 */
data class AnalysisWithFamily(
    val id: Int,
    val code: String,
    val ana_loinc: String?,
    val name: String,
    val familyName: String
)
