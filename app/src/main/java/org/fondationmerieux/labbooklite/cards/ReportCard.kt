package org.fondationmerieux.labbooklite.cards

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import java.io.File

@Composable
fun ReportCard(
    recordId: Int,
    database: LabBookLiteDatabase,
    context: Context,
    pdfFiles: List<File>,
    reloadPdfFiles: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Compte rendu", style = MaterialTheme.typography.titleMedium)

            pdfFiles.forEachIndexed { index, file ->
                val isLast = index == pdfFiles.lastIndex
                PdfFileRow(
                    file = file,
                    isLast = isLast,
                    recordId = recordId,
                    database = database,
                    context = context,
                    reloadPdfFiles = reloadPdfFiles
                )
            }
        }
    }
}
