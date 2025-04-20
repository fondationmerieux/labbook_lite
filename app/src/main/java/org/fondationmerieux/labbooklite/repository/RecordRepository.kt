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
        val recordId = (recordDao.getMaxRecordId() ?: 0) + 1

        val recordEntity = RecordEntity(
            id_data = recordId,
            patient_id = record.patientId,
            type = null,
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

        recordDao.insert(recordEntity)

        val requestIdMap = mutableMapOf<Int, Int>()  // analysisRef -> inserted requestId
        var reqIdCounter = (analysisRequestDao.getMaxId() ?: 0)
        val requests = (analyses + acts).map {
            reqIdCounter += 1
            requestIdMap[it.analysisId] = reqIdCounter
            AnalysisRequestEntity(
                id = reqIdCounter,
                recordId = recordId,
                analysisRef = it.analysisId,
                isUrgent = if (it.urgent) 4 else 5
            )
        }
        analysisRequestDao.insertAll(requests)

        var sampleIdCounter = (sampleDao.getMaxId() ?: 0)
        val sampleEntities = samples.map {
            sampleIdCounter += 1
            SampleEntity(
                id_data = sampleIdCounter,
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

        var resultIdCounter = (analysisResultDao.getMaxId() ?: 0)
        val resultEntities = mutableListOf<AnalysisResultEntity>()
        results.forEach { result ->
            val analysisRequestId = requestIdMap[result.analysisId] ?: return@forEach
            val variableRefs = anaLinkDao.getVariableIdsForAnalysis(result.analysisId)
            variableRefs.forEach { variableId ->
                resultIdCounter += 1
                resultEntities.add(
                    AnalysisResultEntity(
                        id = resultIdCounter,
                        analysisId = analysisRequestId,
                        variableRef = variableId,
                        value = result.value,
                        isRequired = null
                    )
                )
            }
        }
        analysisResultDao.insertAll(resultEntities)

        // Get user ID from preferences
        val prefs = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", 0)

        var validationIdCounter = (validationDao.getMaxId() ?: 0)
        val nowStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val validationEntities = resultEntities.map {
            validationIdCounter += 1
            AnalysisValidationEntity(
                id = validationIdCounter,
                resultId = it.id,
                validationDate = nowStr,
                userId = userId,
                value = null,
                validationType = 250,  // administrative validation
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