package org.fondationmerieux.labbooklite.database.model

import org.fondationmerieux.labbooklite.database.entity.*
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadPayload(
    val login: String,
    val pwd: String,
    val patients: List<PatientEntity>,
    val records: List<RecordEntity>,
    val samples: List<SampleEntity>,
    val analysis_request: List<AnalysisRequestEntity>,
    val analysis_result: List<AnalysisResultEntity>,
    val analysis_validation: List<AnalysisValidationEntity>,
    val pdf_reports: List<PdfReport>
)

@JsonClass(generateAdapter = true)
data class PdfReport(
    val filename: String,
    val recordId: Int,
    val creationDate: String
)
