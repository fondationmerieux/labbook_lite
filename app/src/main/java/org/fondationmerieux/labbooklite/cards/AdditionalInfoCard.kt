package org.fondationmerieux.labbooklite.cards

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.database.entity.RecordEntity
import org.fondationmerieux.labbooklite.session.SessionManager

@Composable
fun AdditionalInfoCard(record: RecordEntity, database: LabBookLiteDatabase) {
    val context = LocalContext.current
    var reportText by remember { mutableStateOf(record.report.orEmpty()) }
    val coroutineScope = rememberCoroutineScope()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Renseignements / Informations complémentaires", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = reportText,
                onValueChange = { reportText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val currentUserId = SessionManager.getCurrentUserId(context)
                    val updated = record.copy(
                        report = reportText,
                        rec_user = currentUserId
                    )
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            database.recordDao().insert(updated)
                        }
                        Toast.makeText(context, "Commentaire enregistré", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Enregistrer le commentaire")
            }
        }
    }
}
