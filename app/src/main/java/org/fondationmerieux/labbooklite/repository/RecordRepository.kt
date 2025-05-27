package org.fondationmerieux.labbooklite.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.database.dao.RecordDao
import org.fondationmerieux.labbooklite.database.entity.*
import org.fondationmerieux.labbooklite.database.model.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class RecordRepository(private val context: Context, private val db: LabBookLiteDatabase) {

    suspend fun savePatientRequest(
        record: RecordPayload,
        analyses: List<AnalysisRequestPayload>,
        acts: List<AnalysisRequestPayload>,
        samples: List<SamplePayload>,
        results: List<AnalysisResultPayload>,
        recLite: Int
    ) = withContext(Dispatchers.IO) {
        val recordDao = db.recordDao()
        val analysisRequestDao = db.analysisRequestDao()
        val sampleDao = db.sampleDao()
        val analysisResultDao = db.analysisResultDao()
        val validationDao = db.analysisValidationDao()
        val anaLinkDao = db.anaLinkDao()

        val now = LocalDateTime.now()
        val dateSave = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val recNumLite = generateRecordLiteNumber(recordDao)

        // Insert record with auto-generated ID
        val recordEntity = RecordEntity(
            id_data = 0,
            patient_id = record.patientId,
            type = 183, // External type
            rec_date_receipt = "${record.receivedDate} ${record.receivedTime}",
            prescriber = record.prescriberId,
            prescription_date = record.prescriptionDate,
            report = record.comment,
            status = record.status,
            rec_num_int = record.recordNumber,
            rec_date_vld = null,
            rec_modified = null,
            rec_hosp_num = null,
            rec_date_save = dateSave,
            rec_num_lite = recNumLite,
            rec_lite = recLite
        )
        val recordId = recordDao.insert(recordEntity).toInt()

        // Insert requests with auto-generated IDs
        val requests = (analyses + acts).map {
            AnalysisRequestEntity(
                id = 0,
                recordId = recordId,
                analysisRef = it.analysisId,
                isUrgent = if (it.urgent) 4 else 5
            )
        }
        val requestInsertedIds = analysisRequestDao.insertAll(requests)
        val requestIdMap = (analyses + acts).mapIndexed { index, req ->
            req.analysisId to requestInsertedIds[index].toInt()
        }.toMap()

        // Insert samples with auto-generated IDs
        val sampleEntities = samples.map {
            SampleEntity(
                id_data = 0,
                samp_date = parseDate("${it.prelDate} ${it.prelTime}"),
                sample_type = it.productType,
                status = it.status,
                record_id = recordId,
                sampler = null,
                samp_receipt_date = parseDate("${it.recvDate} ${it.recvTime}"),
                comment = null,
                location_id = null,
                location_plus = null,
                localization = null,
                code = it.code,
                samp_id_ana = it.analysisId
            )
        }
        sampleDao.insertAll(sampleEntities)

        // Insert results with auto-generated IDs
        val resultEntities = results.flatMap { result ->
            val analysisRequestId = requestIdMap[result.analysisId] ?: return@flatMap emptyList()
            val variableRefs = anaLinkDao.getVariableIdsForAnalysis(result.analysisId)
            variableRefs.map { variableId ->
                AnalysisResultEntity(
                    id = 0,
                    analysisId = analysisRequestId,
                    variableRef = variableId,
                    value = result.value,
                    isRequired = null
                )
            }
        }
        val resultInsertedIds = analysisResultDao.insertAll(resultEntities)

        // Insert validations with auto-generated IDs
        val prefs = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", 0)
        val nowStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        val validationEntities = resultInsertedIds.map { resultId ->
            AnalysisValidationEntity(
                id = 0,
                resultId = resultId.toInt(),
                validationDate = nowStr,
                userId = userId,
                value = null,
                validationType = 250,
                comment = null,
                cancelReason = null
            )
        }
        validationDao.insertAll(validationEntities)
    }

    private fun parseDate(dateTime: String): Date? {
        return try {
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            formatter.parse(dateTime)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun generateRecordLiteNumber(recordDao: RecordDao): String {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val prefix = "LT-$today"

        val last = recordDao.getLastLiteRecordNumber()
        val nextNumber = if (last != null && last.length >= 13) {
            val current = last.takeLast(4).toIntOrNull() ?: 0
            current + 1
        } else 1

        val padded = nextNumber.toString().padStart(4, '0')
        return "$prefix$padded"
    }
}