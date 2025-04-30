package org.fondationmerieux.labbooklite.database.model

/**
 * Created by AlC on 30/04/2025.
 */
import android.content.Context
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.*
//import com.itextpdf.layout.property.UnitValue
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.layout.element.Image
import java.io.File

fun generateReportHeaderPdf(context: Context, filename: String) {
    val outputFile = File(context.filesDir, filename)
    val writer = PdfWriter(outputFile)
    val pdfDoc = PdfDocument(writer)
    val document = Document(pdfDoc)

    // Logo
    val logoFile = File(context.filesDir, "logo.png")
    if (logoFile.exists()) {
        val imageData = ImageDataFactory.create(logoFile.absolutePath)
        val logo = Image(imageData).scaleToFit(100f, 100f)
        document.add(logo)
    }

    // Preferences
    val prefs = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
    val labName = prefs.getString("lite_name", "Laboratoire")
    val labCode = prefs.getInt("lite_ser", 0)
    val reportPwd = prefs.getString("lite_report_pwd", "")

    val header = Paragraph()
        .add("Laboratoire : $labName\n")
        .add("Code : $labCode\n")
        .add("Mot de passe : $reportPwd\n")
        .setFontSize(12f)

    document.add(header)

    document.close()
}