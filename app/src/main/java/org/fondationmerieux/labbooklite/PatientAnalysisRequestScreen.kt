package org.fondationmerieux.labbooklite

import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.database.dao.RecordDao
import org.fondationmerieux.labbooklite.database.entity.DictionaryEntity
import org.fondationmerieux.labbooklite.database.entity.PatientEntity
import org.fondationmerieux.labbooklite.database.entity.PrescriberEntity
import org.fondationmerieux.labbooklite.database.model.AnalysisRequestPayload
import org.fondationmerieux.labbooklite.database.model.AnalysisResultPayload
import org.fondationmerieux.labbooklite.database.model.AnalysisSelection
import org.fondationmerieux.labbooklite.database.model.AnalysisWithFamily
import org.fondationmerieux.labbooklite.database.model.PathologicalProduct
import org.fondationmerieux.labbooklite.database.model.RecordPayload
import org.fondationmerieux.labbooklite.database.model.SamplePayload
import org.fondationmerieux.labbooklite.ui.viewmodel.PatientRequestViewModel
import org.fondationmerieux.labbooklite.ui.viewmodel.PatientRequestViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientAnalysisRequestScreen(
    navController: NavController,
    patient: PatientEntity,
    dictionaries: List<DictionaryEntity>,
    analyses: List<AnalysisWithFamily>,
    prescribers: List<PrescriberEntity>,
    database: LabBookLiteDatabase
) {
    val locale = Locale.getDefault()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    /*
    analyses.forEach {
        Log.i("LabBookLite", "Analysis loaded: id=${it.id}, code=${it.code}, name=${it.name}, bio_product=${it.bio_product}")
    }
    */

    var recordNumber by remember { mutableStateOf("") }
    var receivedDate by remember { mutableStateOf("") }
    var receivedTime by remember { mutableStateOf("") }
    var prescriptionDate by remember { mutableStateOf("") }

    val ageUnitLabel = dictionaries
        .firstOrNull { it.id_data == patient.pat_age_unit }?.label.orEmpty()
    val sexLabel = dictionaries
        .firstOrNull { it.dico_name == "sexe" && it.id_data == patient.pat_sex }?.label.orEmpty()

    val isAnonymous = patient.pat_ano == dictionaries
        .firstOrNull {
            it.dico_name == "yorn" && it.label.equals(
                "Oui",
                ignoreCase = true
            )
        }?.id_data

    val l_ana = remember { mutableStateListOf<AnalysisSelection>() }
    val l_samp = remember { mutableStateListOf<MutableMap<String, Any>>() }
    val l_prod = remember { mutableStateListOf<PathologicalProduct>() }

    var showConfirmationDialog by remember { mutableStateOf(false) }

    val application = context.applicationContext as Application
    val viewModel: PatientRequestViewModel = viewModel(
        factory = PatientRequestViewModelFactory(application, database)
    )
    val scope = rememberCoroutineScope()

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
            label = { Text("Numéro de dossier interne au laboratoire") },
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
                        val date =
                            String.format(locale, "%02d/%02d/%04d", dayOfMonth, month + 1, year)
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
                        val time = String.format(locale, "%02d:%02d", hourOfDay, minute)
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
                        val date =
                            String.format(locale, "%02d/%02d/%04d", dayOfMonth, month + 1, year)
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

        // Prescriber search section
        Text(stringResource(R.string.prescripteur), style = MaterialTheme.typography.titleMedium)

        var prescriberSearchText by remember { mutableStateOf("") }
        var prescriberExpanded by remember { mutableStateOf(false) }
        var selectedPrescriber by remember { mutableStateOf<PrescriberEntity?>(null) }

        val filteredPrescribers = if (prescriberSearchText.length >= 3) {
            prescribers.filter {
                val query = prescriberSearchText.lowercase()
                it.code.orEmpty().lowercase().contains(query) ||
                        it.lastname.orEmpty().lowercase().contains(query) ||
                        it.firstname.orEmpty().lowercase().contains(query)
            }
        } else emptyList()

        ExposedDropdownMenuBox(
            expanded = prescriberExpanded,
            onExpandedChange = {
                if (prescriberSearchText.length >= 3 && filteredPrescribers.isNotEmpty()) {
                    prescriberExpanded = it
                } else {
                    prescriberExpanded = false
                }
            }
        ) {
            OutlinedTextField(
                value = prescriberSearchText,
                onValueChange = {
                    prescriberSearchText = it
                    prescriberExpanded = it.length >= 3 && filteredPrescribers.isNotEmpty()
                },
                label = { Text(stringResource(R.string.rechercher_un_prescripteur)) },
                trailingIcon = {
                    if (prescriberSearchText.isNotBlank()) {
                        IconButton(onClick = {
                            prescriberSearchText = ""
                            prescriberExpanded = false
                            selectedPrescriber = null
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Effacer")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = prescriberExpanded,
                onDismissRequest = { prescriberExpanded = false }
            ) {
                filteredPrescribers.take(5).forEach { prescriber ->
                    val specialityLabel =
                        dictionaries.firstOrNull { it.id_data == prescriber.speciality }?.label.orEmpty()
                    val displayText = buildString {
                        if (!prescriber.code.isNullOrBlank()) append("${prescriber.code} - ")
                        append("${prescriber.lastname.orEmpty()} ${prescriber.firstname.orEmpty()}")
                        if (specialityLabel.isNotBlank()) append(" - $specialityLabel")
                    }

                    DropdownMenuItem(
                        text = { Text(displayText) },
                        onClick = {
                            selectedPrescriber = prescriber
                            prescriberSearchText = displayText
                            prescriberExpanded = false
                        }
                    )
                }
            }
        }

        // Analysis search section
        Text(
            stringResource(R.string.analyse_et_acte_de_prelevement),
            style = MaterialTheme.typography.titleMedium
        )

        var searchText by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }

        // Filter analysis list based on search text
        val filteredAnalysis = analyses.filter {
            val query = searchText.lowercase()
            !it.code.startsWith("PB") && (
                    it.name.lowercase().contains(query) ||
                            it.code.orEmpty().lowercase().contains(query) ||
                            it.ana_loinc.orEmpty().lowercase().contains(query)
                    )
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
                    .heightIn(max = 300.dp) // ou 400.dp selon ton écran
                    .padding(horizontal = 8.dp)
            ) {
                filteredAnalysis.forEach { analysis ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text("[${analysis.code} / ${analysis.ana_loinc.orEmpty()}]")
                                Text(
                                    analysis.familyName ?: "",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(analysis.name, style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        onClick = {
                            val alreadyExists = l_ana.any { it.id == analysis.id }
                            if (!alreadyExists) {
                                l_ana.add(
                                    AnalysisSelection(
                                        id = analysis.id,
                                        code = analysis.code.orEmpty(),
                                        name = analysis.name,
                                        isUrgent = mutableStateOf(false)
                                    )
                                )
                            }

                            /*
                            Log.i(
                                "LabBookLite",
                                "Selected analysis: ${analysis.code}, bio_product = ${analysis.bio_product}"
                            )
                            */
                            val actId = analysis.bio_product
                            if (actId > 0) {
                                val linkedAct = analyses.firstOrNull { it.id == actId }
                                if (linkedAct == null) {
                                    Log.w(
                                        "LabBookLite",
                                        "No linked act found for bio_product id = $actId"
                                    )
                                } else if (l_samp.none { it["id"] == linkedAct.id }) {
                                    Log.i("LabBookLite", "Linked act found: ${linkedAct.code}")
                                    l_samp.add(
                                        mutableMapOf(
                                            "id" to linkedAct.id,
                                            "code" to linkedAct.code.orEmpty(),
                                            "name" to linkedAct.name
                                        )
                                    )
                                }
                            }

                            // Add pathological product for all analyses with a defined or undefined sample_type
                            val calendar = Calendar.getInstance()
                            val initialDate = String.format(
                                locale,
                                "%02d/%02d/%04d",
                                calendar.get(Calendar.DAY_OF_MONTH),
                                calendar.get(Calendar.MONTH) + 1,
                                calendar.get(Calendar.YEAR)
                            )
                            val initialTime = String.format(
                                locale,
                                "%02d:%02d",
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE)
                            )

                            val sampleType = analysis.sample_type
                            val productType = if (sampleType > 0) sampleType else -1

                            l_prod.add(
                                PathologicalProduct(
                                    analysisId = analysis.id,
                                    analysisCode = analysis.code.orEmpty(),
                                    sampleType = sampleType,
                                    productType = productType,
                                    prelDate = initialDate,
                                    prelTime = initialTime,
                                    code = "",
                                    recvDate = "",
                                    recvTime = "",
                                    status = -1
                                )
                            )

                            searchText = ""
                            expanded = false
                        }
                    )
                }
            }
        }

        Text("Analyses", style = MaterialTheme.typography.titleMedium)

        l_ana.forEachIndexed { index, analysis ->
            val code = analysis.code
            val name = analysis.name

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val ana = analyses.firstOrNull { it.id == analysis.id }
                        val loinc = ana?.ana_loinc?.takeIf { it.isNotBlank() } ?: ""
                        val displayText =
                            if (loinc.isNotEmpty()) "$code / $loinc - $name" else "$code - $name"

                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { l_ana.removeAt(index) },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Urgent :",
                            modifier = Modifier.padding(end = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row {
                            listOf("Oui", "Non").forEach { label ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    RadioButton(
                                        selected = (label == "Oui" && analysis.isUrgent.value) || (label == "Non" && !analysis.isUrgent.value),
                                        onClick = {
                                            analysis.isUrgent.value = (label == "Oui")
                                        }
                                    )

                                    Text(label, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        Text("Actes de prélèvement", style = MaterialTheme.typography.titleMedium)

        l_samp.forEachIndexed { index, act ->
            val code = act["code"] as String
            val name = act["name"] as String

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Code : $code", style = MaterialTheme.typography.bodyMedium)
                        Text("Nom : $name", style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(
                        onClick = { l_samp.removeAt(index) },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Text("Produits pathologiques", style = MaterialTheme.typography.titleMedium)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                l_prod.forEachIndexed { index, product ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                                IconButton(onClick = { l_prod.removeAt(index) }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Supprimer",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            // Date & Time of sample
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val prelDate = product.prelDate
                                OutlinedTextField(
                                    value = prelDate,
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

                                val prelTime = product.prelTime
                                OutlinedTextField(
                                    value = prelTime,
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
                            val typePrelOptions =
                                dictionaries.filter { it.dico_name == "type_prel" }
                            var expanded by remember { mutableStateOf(false) }
                            val currentTypeId = product.productType
                            val currentTypeLabel =
                                typePrelOptions.firstOrNull { it.id_data == currentTypeId }?.label
                                    ?: ""

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it }
                            ) {
                                OutlinedTextField(
                                    value = currentTypeLabel,
                                    onValueChange = {},
                                    label = { Text("Type de produit") },
                                    readOnly = true,
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    typePrelOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.label.orEmpty()) },
                                            onClick = {
                                                product.productType = option.id_data
                                                expanded = false
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
                            val statusOptions =
                                dictionaries.filter { it.dico_name == "prel_statut" }
                            val currentStatusId = product.status
                            val currentStatusLabel = statusOptions
                                .firstOrNull { it.id_data == currentStatusId }?.label.orEmpty()

                            ExposedDropdownMenuBox(
                                expanded = product.isStatusMenuExpanded.value,
                                onExpandedChange = { product.isStatusMenuExpanded.value = it }
                            ) {
                                OutlinedTextField(
                                    value = currentStatusLabel,
                                    onValueChange = {},
                                    label = { Text("Statut") },
                                    readOnly = true,
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
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
            }
        }

        var additionalInfo by remember { mutableStateOf("") }

        OutlinedTextField(
            value = additionalInfo,
            onValueChange = { additionalInfo = it },
            label = { Text("Renseignements / Informations complémentaires") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { navController.navigate("home") },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Quitter")
            }

            Button(
                onClick = {
                    focusManager.clearFocus(force = true)

                    // Check that at least one analysis is selected
                    if (l_ana.isEmpty()) {
                        Toast.makeText(context, "Sélectionnez au moins une analyse.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    // Check that reception date and time are valid
                    if (receivedDate.isBlank() || receivedTime.isBlank()) {
                        Toast.makeText(context, "Renseignez la date et l'heure de réception du dossier.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    // Check prescription date
                    if (prescriptionDate.isBlank()) {
                        Toast.makeText(context, "Renseignez la date de prescription.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Check pathological product: missing date or time
                    val missingDateTime = l_prod.any {
                        it.prelDate.isBlank() || it.prelTime.isBlank()
                    }
                    if (missingDateTime) {
                        Toast.makeText(context, "Renseignez la date et l'heure de prélèvement.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    // Check pathological product: invalid type or status
                    val invalidTypeOrStatus = l_prod.any {
                        it.productType <= 0 || it.status <= 0
                    }
                    if (invalidTypeOrStatus) {
                        Toast.makeText(context, "Sélectionnez un type et un statut pour les produits pathologiques.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    // All checks passed, show confirmation dialog
                    showConfirmationDialog = true
                }
            ) {
                Text("Valider")
            }

            if (showConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmationDialog = false },
                    title = { Text("Confirmation") },
                    text = { Text("Voulez-vous vraiment valider cette demande ?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showConfirmationDialog = false

                                /*
                                Log.i(
                                    "LabBookLite PatientRequest",
                                    "Patient sélectionné: id=${patient.id_data}, nom=${patient.pat_name}, prénom=${patient.pat_firstname}"
                                )
                                */

                                scope.launch {
                                    val liteNumber = generateRecordLiteNumber(database.recordDao())

                                    val recordPayload = RecordPayload(
                                        recordNumber = recordNumber,
                                        recordLiteNumber = "", // will be generated in repository
                                        patientId = patient.id_data,
                                        prescriberId = selectedPrescriber?.id_data,
                                        receivedDate = receivedDate,
                                        receivedTime = receivedTime,
                                        prescriptionDate = prescriptionDate,
                                        comment = additionalInfo
                                    )

                                    val analysisRequests = l_ana.map {
                                        AnalysisRequestPayload(
                                            analysisId = it.id,
                                            urgent = it.isUrgent.value
                                        )
                                    }

                                    val samplingActs = l_samp.map {
                                        AnalysisRequestPayload(
                                            analysisId = it["id"] as Int,
                                            urgent = false
                                        )
                                    }

                                    val pathologicalSamples = l_prod.map {
                                        SamplePayload(
                                            analysisId = it.analysisId,
                                            sampleType = it.sampleType,
                                            productType = it.productType,
                                            prelDate = it.prelDate,
                                            prelTime = it.prelTime,
                                            code = it.code,
                                            recvDate = it.recvDate,
                                            recvTime = it.recvTime,
                                            status = it.status
                                        )
                                    }

                                    val resultList = l_ana.map {
                                        AnalysisResultPayload(
                                            analysisId = it.id,
                                            recordId = -1
                                        )
                                    }

                                    viewModel.logPatientRequest(
                                        record = recordPayload,
                                        analyses = analysisRequests,
                                        acts = samplingActs,
                                        samples = pathologicalSamples,
                                        results = resultList
                                    )

                                    /*
                                    Log.d("LabBookLite", ">>> Preparing to submit patient request")
                                    Log.d("LabBookLite", ">>> record = $recordPayload")
                                    Log.d(
                                        "LabBookLite",
                                        ">>> analysisRequests = ${analysisRequests.map { it.analysisId }}"
                                    )
                                    Log.d(
                                        "LabBookLite",
                                        ">>> samplingActs = ${samplingActs.map { it.analysisId }}"
                                    )
                                    Log.d(
                                        "LabBookLite",
                                        ">>> samples = ${pathologicalSamples.map { it.analysisId }}"
                                    )
                                    Log.d(
                                        "LabBookLite",
                                        ">>> results = ${resultList.map { it.analysisId }}"
                                    )
                                    */

                                    viewModel.submitPatientRequest(
                                        record = recordPayload,
                                        analyses = analysisRequests,
                                        acts = samplingActs,
                                        samples = pathologicalSamples,
                                        results = resultList,
                                        onSuccess = {
                                            Log.i("LabBookLite", "SUCCESS: patient request submitted")
                                            navController.navigate("home")
                                        },
                                        onError = { e ->
                                            Log.e(
                                                "LabBookLite",
                                                "Error saving request",
                                                e
                                            )
                                        }
                                    )
                                }
                            }
                        ) {
                            Text("Confirmer")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmationDialog = false }) {
                            Text("Annuler")
                        }
                    }
                )
            }
        }
    }
}

suspend fun generateRecordLiteNumber(recordDao: RecordDao): String {
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
