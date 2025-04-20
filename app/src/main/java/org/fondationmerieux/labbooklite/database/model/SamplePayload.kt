package org.fondationmerieux.labbooklite.database.model

/**
 * Created by AlC on 16/04/2025.
 */
data class SamplePayload(
    val analysisId: Int,
    val sampleType: Int,
    val productType: Int,
    val prelDate: String,
    val prelTime: String,
    val code: String,
    val recvDate: String,
    val recvTime: String,
    val status: Int
)