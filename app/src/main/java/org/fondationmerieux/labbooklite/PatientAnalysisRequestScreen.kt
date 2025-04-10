package org.fondationmerieux.labbooklite

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import org.fondationmerieux.labbooklite.data.entity.DictionaryEntity
import org.fondationmerieux.labbooklite.data.entity.PatientEntity
import org.fondationmerieux.labbooklite.data.model.AnalysisWithFamily
import java.util.*

@Composable
fun PatientAnalysisRequestScreen(
    navController: NavController,
    patient: PatientEntity,
    dictionaries: List<DictionaryEntity>,
    analyses: List<AnalysisWithFamily>
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var recordNumber by remember { mutableStateOf("") }
    var receivedDate by remember { mutableStateOf("") }
    var receivedTime by remember { mutableStateOf("") }
    var prescriptionDate by remember { mutableStateOf("") }

    val ageUnitLabel = dictionaries
        .firstOrNull { it.id_data == patient.pat_age_unit }?.label.orEmpty()
    val sexLabel = dictionaries
        .firstOrNull { it.dico_name == "sexe" && it.id_data == patient.pat_sex }?.label.orEmpty()

    val isAnonymous = patient.pat_ano == dictionaries
        .firstOrNull { it.dico_name == "yorn" && it.label.equals("Oui", ignoreCase = true) }?.id_data

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Internal record number input
        OutlinedTextField(
            value = recordNumber,
            onValueChange = { recordNumber = it },
            label = { Text(stringResource(R.string.code_patient_interne_au_laboratoire)) },
            modifier = Modifier.fillMaxWidth()
        )

        // Patient identity section
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.identite),
                style = MaterialTheme.typography.titleMedium
            )

            if (!patient.pat_code.isNullOrBlank() || !patient.pat_code_lab.isNullOrBlank()) {
                Text("${patient.pat_code ?: ""} ${patient.pat_code_lab ?: ""}".trim())
            }

            if (isAnonymous == true) {
                Text(stringResource(R.string.patient_anonyme))
            } else {
                val fullName = listOfNotNull(
                    patient.pat_firstname,
                    patient.pat_name,
                    patient.pat_maiden?.takeIf { it.isNotBlank() }
                ).joinToString(" ")
                if (fullName.isNotBlank()) Text(fullName)
            }

            if (!patient.pat_birth.isNullOrBlank() || patient.pat_age != null || sexLabel.isNotBlank()) {
                val birthStr = patient.pat_birth?.let { stringResource(R.string.ne_le, it) } ?: ""
                val ageStr = patient.pat_age?.let { "$it $ageUnitLabel" } ?: ""
                val sep = if (birthStr.isNotBlank() && ageStr.isNotBlank()) " - " else ""
                Text("$birthStr$sep$ageStr${if (sexLabel.isNotBlank()) " - $sexLabel" else ""}")
            }

            val address = listOfNotNull(
                patient.pat_address,
                patient.pat_district,
                patient.pat_pbox,
                patient.pat_zipcode,
                patient.pat_city
            ).filter { it.isNotBlank() }.joinToString(", ")
            if (address.isNotBlank()) Text(address)

            val phones = listOfNotNull(
                patient.pat_phone1,
                patient.pat_phone2
            ).filter { it.isNotBlank() }.joinToString(" / ")
            if (phones.isNotBlank()) Text(phones)

            Button(
                onClick = { navController.navigate("patient_form/${patient.id_data}") },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.modifier_dossier_patient))
            }
        }

        // Prescription section
        Text(stringResource(R.string.prescription), style = MaterialTheme.typography.titleMedium)

        // Reception date
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = receivedDate,
                onValueChange = { receivedDate = it },
                label = { Text(stringResource(R.string.date_de_reception_du_dossier)) },
                readOnly = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                val calendar = Calendar.getInstance()
                val datePicker = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                        receivedDate = date
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePicker.show()
            }) {
                Icon(Icons.Filled.CalendarToday, contentDescription = "Date Picker")
            }
        }

        // Reception time
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = receivedTime,
                onValueChange = { receivedTime = it },
                label = { Text(stringResource(R.string.heure_de_reception_du_dossier)) },
                readOnly = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                val calendar = Calendar.getInstance()
                val timePicker = TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val time = String.format("%02d:%02d", hourOfDay, minute)
                        receivedTime = time
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePicker.show()
            }) {
                Icon(Icons.Filled.CalendarToday, contentDescription = "Time Picker")
            }
        }

        // Prescription date
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = prescriptionDate,
                onValueChange = { prescriptionDate = it },
                label = { Text(stringResource(R.string.date_de_prescription)) },
                readOnly = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                val calendar = Calendar.getInstance()
                val datePicker = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                        prescriptionDate = date
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePicker.show()
            }) {
                Icon(Icons.Filled.CalendarToday, contentDescription = "Date Picker")
            }
        }

        // Analysis search section
        Text(stringResource(R.string.analyse_et_acte_de_prelevement), style = MaterialTheme.typography.titleMedium)

        var searchText by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }

        // Filter analysis list based on search text
        val filteredAnalysis = analyses.filter {
            val query = searchText.lowercase()
            it.name.lowercase().contains(query) ||
                    it.code.orEmpty().lowercase().contains(query) ||
                    it.ana_loinc.orEmpty().lowercase().contains(query)
        }

        Column {
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    expanded = it.length >= 3
                },
                label = { Text(stringResource(R.string.rechercher_une_analyse)) },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = expanded && filteredAnalysis.isNotEmpty(),
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                filteredAnalysis.take(5).forEach { analysis ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text("[${analysis.code} / ${analysis.ana_loinc.orEmpty()}]")
                                Text(analysis.familyName, style = MaterialTheme.typography.labelSmall)
                                Text(analysis.name, style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        onClick = {
                            searchText = analysis.name
                            expanded = false
                            // TODO: add selected analysis to the record
                        }
                    )
                }
            }
        }
    }
}