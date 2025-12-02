package org.fondationmerieux.labbooklite.cards

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.fondationmerieux.labbooklite.database.entity.DictionaryEntity
import org.fondationmerieux.labbooklite.database.model.PathologicalProduct
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleRequestCard(
    product: PathologicalProduct,
    dictionaries: List<DictionaryEntity>,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val locale = Locale.getDefault()

    val typePrelOptions = dictionaries.filter { it.dico_name == "type_prel" }
    val statusOptions = dictionaries.filter { it.dico_name == "prel_statut" }

    var isTypeMenuExpanded by remember { mutableStateOf(false) }

    val currentTypeId = product.productType
    val currentTypeLabel = typePrelOptions
        .firstOrNull { it.id_data == currentTypeId }?.label.orEmpty()

    val currentStatusId = product.status
    val currentStatusLabel = statusOptions
        .firstOrNull { it.id_data == currentStatusId }?.label.orEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Analysis: ${product.analysisCode}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Date & time of sample
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = product.prelDate,
                    onValueChange = {},
                    label = { Text("Date prélèvement") },
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val newDate = String.format(
                                        locale,
                                        "%02d/%02d/%04d",
                                        day,
                                        month + 1,
                                        year
                                    )
                                    product.prelDate = newDate
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) {
                            Icon(
                                Icons.Filled.CalendarToday,
                                contentDescription = "Date Picker"
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = product.prelTime,
                    onValueChange = {},
                    label = { Text("Heure prélèvement") },
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = {
                            val calendar = Calendar.getInstance()
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    val newTime = String.format(
                                        locale,
                                        "%02d:%02d",
                                        hour,
                                        minute
                                    )
                                    product.prelTime = newTime
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                            ).show()
                        }) {
                            Icon(
                                Icons.Filled.CalendarToday,
                                contentDescription = "Select Time"
                            )
                        }
                    }
                )
            }

            // Product type
            ExposedDropdownMenuBox(
                expanded = isTypeMenuExpanded,
                onExpandedChange = { isTypeMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = currentTypeLabel,
                    onValueChange = {},
                    label = { Text("Type de produit") },
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isTypeMenuExpanded,
                    onDismissRequest = { isTypeMenuExpanded = false }
                ) {
                    typePrelOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label.orEmpty()) },
                            onClick = {
                                product.productType = option.id_data
                                isTypeMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Code sample
            OutlinedTextField(
                value = product.code,
                onValueChange = { product.code = it },
                label = { Text("Code") },
                modifier = Modifier.fillMaxWidth()
            )

            // Date & time of receipt sample
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = product.recvDate,
                    onValueChange = {},
                    label = { Text("Date de réception") },
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val newDate = String.format(
                                        locale,
                                        "%02d/%02d/%04d",
                                        day,
                                        month + 1,
                                        year
                                    )
                                    product.recvDate = newDate
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) {
                            Icon(
                                Icons.Filled.CalendarToday,
                                contentDescription = "Select date"
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = product.recvTime,
                    onValueChange = {},
                    label = { Text("Heure de réception") },
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = {
                            val calendar = Calendar.getInstance()
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    val newTime = String.format(
                                        locale,
                                        "%02d:%02d",
                                        hour,
                                        minute
                                    )
                                    product.recvTime = newTime
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                            ).show()
                        }) {
                            Icon(
                                Icons.Filled.CalendarToday,
                                contentDescription = "Select time"
                            )
                        }
                    }
                )
            }

            // Status
            ExposedDropdownMenuBox(
                expanded = product.isStatusMenuExpanded.value,
                onExpandedChange = { product.isStatusMenuExpanded.value = it }
            ) {
                OutlinedTextField(
                    value = currentStatusLabel,
                    onValueChange = {},
                    label = { Text("Statut") },
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = product.isStatusMenuExpanded.value,
                    onDismissRequest = { product.isStatusMenuExpanded.value = false }
                ) {
                    statusOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label.orEmpty()) },
                            onClick = {
                                product.status = option.id_data
                                product.isStatusMenuExpanded.value = false
                            }
                        )
                    }
                }
            }
        }
    }
}