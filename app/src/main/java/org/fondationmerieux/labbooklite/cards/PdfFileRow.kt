package org.fondationmerieux.labbooklite.cards

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.database.model.generateReportHeaderPdf
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Date
import java.util.Locale

@Composable
fun PdfFileRow(
    file: File,
    isLast: Boolean,
    recordId: Int,
    database: LabBookLiteDatabase,
    context: Context,
    reloadPdfFiles: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(file.name, modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                val success = copyToDownloads(context, file.name)
                val msg = if (success) "Fichier copié vers Téléchargements" else "Erreur de copie"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.FileDownload, contentDescription = "Télécharger", tint = Color(0xFF006B8F))
            }

            IconButton(onClick = {
                openPdfFile(context, file)
            }) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Ouvrir", tint = Color(0xFF006B8F))
            }

            var showConfirmDialog by remember { mutableStateOf<File?>(null) }

            if (showConfirmDialog != null) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = null },
                    confirmButton = {
                        TextButton(onClick = {
                            showConfirmDialog?.delete()
                            reloadPdfFiles()
                            Toast.makeText(context, "Fichier supprimé", Toast.LENGTH_SHORT).show()
                            showConfirmDialog = null
                        }) {
                            Text("Supprimer")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDialog = null }) {
                            Text("Annuler")
                        }
                    },
                    title = { Text("Confirmer la suppression") },
                    text = { Text("Voulez-vous vraiment supprimer le fichier ${showConfirmDialog?.name} ?") }
                )
            }

            IconButton(onClick = {
                showConfirmDialog = file
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Red)
            }

            if (isLast) {
                IconButton(onClick = {
                    val lastModified = file.lastModified()
                    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
                    val formattedDate = dateFormatter.format(Date(lastModified))

                    val base = file.nameWithoutExtension
                        .removeSuffix("_reedit")
                        .substringBefore("_reedit-")

                    val baseFileName = base.ifBlank { file.nameWithoutExtension }

                    var counter = 0
                    var newFile: File
                    do {
                        val suffix = "_reedit-${counter + 1}"
                        val name = "$baseFileName$suffix.pdf"
                        newFile = File(context.filesDir, name)
                        counter++
                    } while (newFile.exists())

                    val newName = newFile.name

                    generateReportHeaderPdf(
                        context = context,
                        filename = newName,
                        database = database,
                        recordId = recordId,
                        reedit = "Y",
                        previousFilename = file.name,
                        previousDate = formattedDate
                    )

                    reloadPdfFiles()

                    Toast.makeText(context, "Reedited PDF generated: $newName", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Rééditer", tint = Color(0xFF006B8F))
                }
            }
        }
    }
}

private fun openPdfFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "Aucun lecteur PDF trouvé", Toast.LENGTH_SHORT).show()
    }
}

private fun copyToDownloads(context: Context, filename: String): Boolean {
    val sourceFile = File(context.filesDir, filename)
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val targetFile = File(downloadsDir, filename)

    return try {
        FileInputStream(sourceFile).use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
