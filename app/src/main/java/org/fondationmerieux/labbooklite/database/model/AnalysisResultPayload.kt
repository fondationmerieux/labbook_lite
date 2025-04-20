package org.fondationmerieux.labbooklite.database.model

/**
 * Created by AlC on 16/04/2025.
 */
data class AnalysisResultPayload(
    val analysisId: Int,
    val recordId: Int,
    val value: String = "",
    val unit: String = ""
)