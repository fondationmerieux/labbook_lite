package org.fondationmerieux.labbooklite.database.model

import android.content.Context
import android.util.Log
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.events.PdfDocumentEvent
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.EncryptionConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.database.entity.AnalysisValidationEntity
import org.fondationmerieux.labbooklite.database.entity.UserEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun generateReportHeaderPdf(context: Context, filename: String, database: LabBookLiteDatabase, recordId: Int, reedit: String = "N", previousFilename: String = "", previousDate: String = "") {
    val outputFile = File(context.filesDir, filename)

    // Preferences
    val prefsMap: Map<String, String> = runBlocking {
        withContext(Dispatchers.IO) {
            database.preferencesDao().getAll()
                .associate { Pair(it.key ?: "", it.value ?: "") }
        }
    }

    // Determine if encryption is required based on preferences
    val sharedPrefs = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
    val reportPwdFlag = sharedPrefs.getString("lite_report_pwd", "N")
    val encrypt = reportPwdFlag == "Y"
    Log.i("LabBookLite", "lite_report_pwd (from SharedPreferences) = '$reportPwdFlag' → encryption: $encrypt")

    val patientCode = runBlocking {
        val record = database.recordDao().getById(recordId)
        val pat = record?.patient_id?.let { database.patientDao().getById(it) }
        pat?.pat_code
    }

    val writer = if (encrypt && !patientCode.isNullOrBlank()) {
        Log.i("LabBookLite", "Generating PDF with password for patient code: $patientCode")
        PdfWriter(outputFile.absolutePath, WriterProperties().apply {
            setStandardEncryption(
                patientCode.toByteArray(),  // User password
                null,                       // Owner password
                EncryptionConstants.ALLOW_PRINTING,
                EncryptionConstants.ENCRYPTION_AES_128
            )
        })
    } else {
        Log.i("LabBookLite", "Generating PDF without password")
        PdfWriter(outputFile.absolutePath)
    }

    val pdfDoc = PdfDocument(writer)

    val document = Document(pdfDoc, PageSize.A4)
    document.setMargins(45.36f, 28.35f, 45.36f, 28.35f)

    // HEADER
    val logoFile = File(context.filesDir, "logo.png")
    val table = Table(floatArrayOf(1.0f, 5f)).useAllAvailableWidth()

    if (logoFile.exists()) {
        val imageData = ImageDataFactory.create(logoFile.absolutePath)
        val logo = Image(imageData)
            .setHeight(65f)
            .setAutoScale(false)
            .setHorizontalAlignment(HorizontalAlignment.LEFT)
        val logoCell = Cell()
            .add(logo)
            .setBorder(Border.NO_BORDER)
            .setVerticalAlignment(VerticalAlignment.TOP)
            .setPaddingBottom(4f)
        table.addCell(logoCell)
    } else {
        table.addCell(Cell().setBorder(Border.NO_BORDER))
    }

    val cellContent = Paragraph().setFontSize(10f).setMultipliedLeading(0.7f)
    cellContent.add(Paragraph(prefsMap["entete_1"].orEmpty()).setFontSize(14f).setBold())
    cellContent.add(Paragraph(prefsMap["entete_2"].orEmpty()))
    cellContent.add(Paragraph(prefsMap["entete_3"].orEmpty()))
    cellContent.add(Paragraph(prefsMap["entete_adr"].orEmpty()))

    val telFaxEmail = listOfNotNull(
        prefsMap["entete_tel"]?.let { "Tél : $it" },
        prefsMap["entete_fax"]?.let { "Fax : $it" },
        prefsMap["entete_email"]?.let { "Email : $it" }
    ).joinToString("  ")

    if (telFaxEmail.isNotBlank()) {
        cellContent.add(Paragraph(telFaxEmail))
    }

    table.addCell(Cell().add(cellContent).setBorder(Border.NO_BORDER))
    document.add(table)

    /* --- Determine report type (COMPLET or PARTIEL) --- */
    val analysisRequests = runBlocking { database.analysisRequestDao().getByRecord(recordId) }
    val analysisResults = runBlocking { database.analysisResultDao().getByRecord(recordId) }
    val anaLinks = runBlocking { database.anaLinkDao().getAll() }
    val validations = runBlocking { database.analysisValidationDao().getAll() }
    val analysisMap = runBlocking { database.analysisDao().getAll().associateBy { it.id_data } }

    val analysisStates = mutableMapOf<Int, String>()
    val filteredRequests = analysisRequests.filter { req ->
        val analysis = analysisMap[req.analysisRef]
        analysis?.code?.startsWith("PB") != true
    }

    for (req in filteredRequests) {
        val resultIds = analysisResults.filter { it.analysisId == req.id }.map { it.id }
        val requiredVarIds = anaLinks.filter { it.analysis_id == req.analysisRef && it.required == 4 }.map { it.variable_id }.toSet()
        val resultMap = resultIds.associateWith { id -> analysisResults.find { it.id == id }?.value.orEmpty().trim() }

        val resultValidations = validations.filter { v -> resultIds.contains(v.resultId) }
        Log.i("LabBookLite", "→ req.id=${req.id}, analysisRef=${req.analysisRef}, resultIds=$resultIds")
        resultValidations.forEach {
            Log.i("LabBookLite", "→ validation for resultId=${it.resultId} → type=${it.validationType}, comment=${it.comment}, cancelReason=${it.cancelReason}")
        }

        val hasAnyValidation = resultIds.any { id ->
            validations.find { it.resultId == id && it.validationType == 252 && it.cancelReason == null && it.comment.isNullOrBlank() } != null
        }

        val hasCancel = resultIds.any { id ->
            val v = validations.find { it.resultId == id }
            v?.validationType == 252 && (v.cancelReason != null || !v.comment.isNullOrBlank())
        }
        val requiredFilled = resultIds.all { id ->
            val variableId = analysisResults.find { it.id == id }?.variableRef
            if (requiredVarIds.contains(variableId)) {
                resultMap[id]?.isNotBlank() == true
            } else true
        }
        val anyValueFilled = resultMap.values.any { it.isNotBlank() }
        val allEmpty = resultMap.values.all { it.isBlank() }

        Log.i("LabBookLite", "→ req.id=${req.id} (analysisRef=${req.analysisRef})")
        Log.i("LabBookLite", "→ requiredVarIds=$requiredVarIds")
        requiredVarIds.forEach { varId ->
            val result = analysisResults.find { it.analysisId == req.id && it.variableRef == varId }
            Log.i("LabBookLite", "→ variableId=$varId → result=${result?.value} (id=${result?.id})")
        }
        Log.i("LabBookLite", "→ requiredFilled=$requiredFilled")


        val state = when {
            hasCancel -> "C"
            hasAnyValidation && requiredFilled -> "V"
            requiredFilled || (requiredVarIds.isEmpty() && anyValueFilled) -> "S"
            allEmpty -> "I"
            else -> "I"
        }
        analysisStates[req.id] = state
    }

    val hasValidated = analysisStates.values.any { it == "V" }
    val hasCancelled = analysisStates.values.any { it == "C" }
    val hasPending = analysisStates.values.any { it == "I" || it == "S" }

    val reportLabel = when {
        (hasValidated || hasCancelled) && !hasPending -> "COMPLET"
        (hasValidated || hasCancelled) && hasPending -> "PARTIEL"
        else -> ""
    }

    Log.i("LabBookLite", "PDF reportLabel: $reportLabel — hasValidated=$hasValidated, hasCancelled=$hasCancelled, hasPending=$hasPending")

    if (reportLabel.isNotEmpty()) {
        val reportParagraph = Paragraph(reportLabel)
            .setFontSize(12f)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(2f)
            .setPaddingBottom(2f)
            .setPaddingLeft(4f)
            .setPaddingRight(4f)
            .setBorder(SolidBorder(ColorConstants.BLACK, 1.2f))

        val container = Paragraph().add(reportParagraph)
            .setTextAlignment(TextAlignment.RIGHT)

        document.add(container)
    }

    // RECORD & PATIENT BLOCKS
    val record = runBlocking { database.recordDao().getById(recordId) } ?: return
    if (record.patient_id == null) return
    val patient = runBlocking { database.patientDao().getById(record.patient_id) } ?: return
    val prescriber = if ((record.prescriber ?: 0) > 0) runBlocking { database.prescriberDao().getById(record.prescriber!!) } else null
    val dictionary = runBlocking { database.dictionaryDao().getAll() }

    // Helper to get dictionary label from id
    fun getLabel(id: Int?): String {
        return dictionary.firstOrNull { it.id_data == id }?.label ?: ""
    }

    fun parseDate(value: String?, pattern: String): Date? {
        return try {
            SimpleDateFormat(pattern, Locale.FRANCE).parse(value ?: "")
        } catch (e: Exception) {
            null
        }
    }

    val infoTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 0.05f, 1f))).useAllAvailableWidth()

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    val datetimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
    val todayStr = dateFormatter.format(Date())

    // Left block: record info
    val codeLab = patient.pat_code_lab ?: ""
    val code = listOfNotNull(patient.pat_code?.takeIf { it.isNotBlank() }, codeLab).joinToString(" ")

    val parsedBirthDate = parseDate(patient.pat_birth, "yyyy-MM-dd")
    val birthLine = parsedBirthDate?.let {
        val ageUnitLabel = getLabel(patient.pat_age_unit)
        val sexLabel = getLabel(patient.pat_sex)
        val ageStr = if (patient.pat_age != null) " - ${patient.pat_age} $ageUnitLabel" else ""
        "Né(e) le ${dateFormatter.format(it)}$ageStr - $sexLabel"
    }

    val prescriberLine = if (prescriber != null) {
        val title = getLabel(prescriber.title)
        "par $title ${prescriber.lastname} ${prescriber.firstname}"
    } else ""

    val recordCell = Cell()
        .add(Paragraph("Dossier : ${record.rec_num_lite}").setFontSize(9f))
        .add(Paragraph("Code : $code").setFontSize(9f))
        .apply {
            birthLine?.let { add(Paragraph(it).setFontSize(9f)) }
            add(Paragraph("\u00A0").setFontSize(9f)) // non-breaking space

            val parsedPrescriptionDate = parseDate(record.prescription_date, "yyyy-MM-dd")
            val prescLine = "Examen prescrit le ${parsedPrescriptionDate?.let { dateFormatter.format(it) } ?: ""} $prescriberLine"
            add(Paragraph(prescLine).setFontSize(9f))

            val parsedSaveDate = parseDate(record.rec_date_save, "yyyy-MM-dd HH:mm:ss")
            val saveLine = "Enregistré le ${parsedSaveDate?.let { datetimeFormatter.format(it) } ?: ""}   édité le $todayStr"
            add(Paragraph(saveLine).setFontSize(9f))
        }
        .setPadding(5f)

    // Right block: patient info
    val patientCell = Cell()
        .apply {
            add(Paragraph("${patient.pat_name} ${patient.pat_firstname}").setFontSize(9f))
            val midMaiden = listOfNotNull(patient.pat_midname, patient.pat_maiden).joinToString(" ")
            add(Paragraph(midMaiden.ifBlank { " " }).setFontSize(9f))
            add(Paragraph(patient.pat_address?.ifBlank { " " } ?: " ").setFontSize(9f))
            val cityLine = listOfNotNull(patient.pat_zipcode, patient.pat_city).joinToString(" ")
            add(Paragraph(cityLine.ifBlank { " " }).setFontSize(9f))
            val phones = listOfNotNull(patient.pat_phone1, patient.pat_phone2)
            if (phones.isNotEmpty()) {
                add(Paragraph("Tél : ${phones.joinToString(" ")}").setFontSize(9f))
            } else {
                add(Paragraph(" ").setFontSize(9f))
            }
            if (!patient.pat_email.isNullOrBlank()) {
                add(Paragraph("Email : ${patient.pat_email}").setFontSize(9f))
            } else {
                add(Paragraph(" ").setFontSize(9f))
            }

        }
        .setPadding(5f)

    recordCell.setBorder(SolidBorder(1f)).setPadding(5f)
    patientCell.setBorder(SolidBorder(1f)).setPadding(5f)
    val spacerCell = Cell().setBorder(Border.NO_BORDER)

    infoTable.addCell(recordCell)
    infoTable.addCell(spacerCell)
    infoTable.addCell(patientCell)

    document.add(infoTable)
    /* --- END of block record & patient --- */

    document.add(Paragraph("Compte rendu").setFontSize(14f).setBold().setTextAlignment(TextAlignment.CENTER))

    if (reedit == "Y" && previousFilename.isNotBlank()) {
        val dateSuffix = if (previousDate.isNotBlank()) " ($previousDate)" else ""
        val fullText = "Annule et remplace le précédent : $previousFilename$dateSuffix"
        Log.i("LabBookLite", "fullText : $fullText")
        document.add(
            Paragraph(fullText)
                .setFontSize(9f)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
        )
    }

    if (!record.report.isNullOrBlank()) {
        val reportTable = Table(UnitValue.createPercentArray(floatArrayOf(1f))).useAllAvailableWidth()
        val reportCell = Cell()
            .add(Paragraph("Renseignements cliniques").setFontSize(11f).setBold())
            .add(Paragraph(record.report).setFontSize(9f))
            .setBorder(SolidBorder(1f))
            .setPadding(5f)
        reportTable.addCell(reportCell)
        document.add(reportTable)
    }

    document.add(Paragraph(" ").setFontSize(5f).setMarginBottom(2f))

    /* --- RESULTS TABLE --- */
    val resultTable = Table(UnitValue.createPercentArray(floatArrayOf(4f, 2.5f, 1.5f)))
        .useAllAvailableWidth()
        .setBorder(SolidBorder(1f))

// Header row
    val headerGray = DeviceRgb(245, 245, 245)
    val headers = listOf("ANALYSE", "RESULTAT", "Références")
    for (title in headers) {
        val paragraph = Paragraph(title)
            .setFontSize(11f)
            .setMultipliedLeading(0.9f)

        val cell = Cell()
            .add(paragraph)
            .setBackgroundColor(headerGray)
            .setBorder(Border.NO_BORDER)
            .setPadding(3f)

        if (title == "ANALYSE") {
            cell.setTextAlignment(TextAlignment.CENTER)
        }

        resultTable.addHeaderCell(cell)
    }

    val validatedResults = analysisResults.filter { result ->
        validations.any { it.resultId == result.id && it.validationType == 252 }
    }

    val displayedRequests = filteredRequests.filter { req ->
        val hasValid = validatedResults.any { it.analysisId == req.id }
        val analysis = analysisMap[req.analysisRef]
        analysis?.code?.startsWith("PB") != true && hasValid
    }

    var lastFamily: String? = null
    var latestValidation: AnalysisValidationEntity? = null

    for (req in displayedRequests) {
        val analysis = analysisMap[req.analysisRef] ?: continue
        val familyLabel = getLabel(analysis.family).takeIf { it.isNotBlank() }

        if (familyLabel != null && familyLabel != lastFamily) {
            val familyCell = Cell(1, 3)
                .add(Paragraph(familyLabel).setFontSize(15f).setBold())
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(Border.NO_BORDER)
            resultTable.addCell(familyCell)
            lastFamily = familyLabel
        }

        val nameCell = Cell(1, 3)
            .add(Paragraph(analysis.name).setFontSize(11f).setBold())
            .setTextAlignment(TextAlignment.LEFT)
            .setBorder(Border.NO_BORDER)
        resultTable.addCell(nameCell)

        val linkedResults = validatedResults.filter { it.analysisId == req.id }
        val linkedLinks = anaLinks.filter { it.analysis_id == analysis.id_data }
        val dictionaryMap = dictionary.associateBy { it.id_data }
        val sortedVars = linkedLinks.sortedBy { it.position ?: Int.MAX_VALUE }

        for (link in sortedVars) {
            val variable = link.variable_id?.let {
                runBlocking { database.anaVarDao().getById(it) }
            } ?: continue
            val result = linkedResults.find { it.variableRef == variable.id_data } ?: continue

            val resultValue = if (
                dictionaryMap[variable.result_type]?.short_label?.startsWith("dico_") == true
            ) {
                dictionaryMap[result.value?.toIntOrNull()]?.label.orEmpty()
            } else {
                result.value.orEmpty()
            }

            val unitLabel = getLabel(variable.unit)
            val highlight = variable.var_highlight == "Y"
            val refRange = if (!variable.normal_min.isNullOrBlank() && !variable.normal_max.isNullOrBlank()) {
                "${variable.normal_min} - ${variable.normal_max}"
            } else ""

            val labelCell = Cell().add(Paragraph(variable.label ?: "").setFontSize(10.5f))
                .setBorder(Border.NO_BORDER)

            val resultText = if (unitLabel.isNotBlank()) "$resultValue $unitLabel" else resultValue
            val resultParagraph = Paragraph(resultText).setFontSize(10.5f)
            if (highlight) resultParagraph.setBold()

            val resultCell = Cell().add(resultParagraph).setBorder(Border.NO_BORDER)

            val refCell = Cell().setBorder(Border.NO_BORDER)
            if (refRange.isNotEmpty()) {
                refCell.add(Paragraph(refRange).setFontSize(10.5f))
            }

            resultTable.addCell(labelCell)
            resultTable.addCell(resultCell)
            resultTable.addCell(refCell)

            // Check if the validation is more recent than the current latest
            val validation = validations.find { it.resultId == result.id && it.validationType == 252 }
            if (validation != null) {
                val thisDate = parseDate(validation.validationDate, "yyyy-MM-dd HH:mm:ss")
                val latestDate = parseDate(latestValidation?.validationDate, "yyyy-MM-dd HH:mm:ss")

                if (thisDate != null && (latestDate == null || thisDate.after(latestDate))) {
                    latestValidation = validation
                }
            }
        }
    }

    // Final "validé par" row
    latestValidation?.let { validation ->
        val user: UserEntity? = runBlocking {
            database.userDao().getUserById(validation.userId)
        }

        val name = listOfNotNull(
            getLabel(user?.title),
            user?.lastname,
            user?.firstname
        ).joinToString(" ").trim()

        val date = validation.validationDate?.let {
            try {
                val parsed = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE).parse(it)
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(parsed)
            } catch (_: Exception) { it }
        } ?: ""

        val text = "validé par $name le $date"

        val finalCell = Cell(1, 3)
            .add(Paragraph(text).setFontSize(11f).setItalic())
            .setBorder(Border.NO_BORDER)
            .setTextAlignment(TextAlignment.LEFT)
            .setPaddingTop(6f)

        resultTable.addCell(finalCell)
    }

    document.add(resultTable)

    /* --- FOOTER --- */
    pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE) { event ->
        val docEvent = event as com.itextpdf.kernel.events.PdfDocumentEvent
        val page = docEvent.page
        val pageSize = page.pageSize

        val pdfCanvas = com.itextpdf.kernel.pdf.canvas.PdfCanvas(
            page.newContentStreamAfter(), page.resources, pdfDoc
        )

        val canvas = com.itextpdf.layout.Canvas(pdfCanvas, pageSize)
            .setFontSize(9f)

        val leftText = "Dossier : ${record.rec_num_lite} édité le ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date())}"
        val rightText = "Page ${pdfDoc.getPageNumber(page)} / ${pdfDoc.numberOfPages}"

        val y = pageSize.bottom + 20f

        canvas.showTextAligned(leftText, pageSize.left + 30f, y, TextAlignment.LEFT)
        canvas.showTextAligned(rightText, pageSize.right - 30f, y, TextAlignment.RIGHT)

        canvas.close()
    }

    document.close()
}