package org.fondationmerieux.labbooklite

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.fondationmerieux.labbooklite.database.entity.DictionaryEntity
import org.fondationmerieux.labbooklite.database.entity.NationalityEntity
import org.fondationmerieux.labbooklite.database.entity.PatientEntity
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import org.fondationmerieux.labbooklite.session.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientFormScreen(database: LabBookLiteDatabase, navController: NavController, patientId: Int? = null) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var yornOptions by remember { mutableStateOf(emptyList<DictionaryEntity>()) }
    var anonymousValue by remember { mutableStateOf<Int?>(null) }
    var anonYesId by remember { mutableStateOf<Int?>(null) }
    var nom by remember { mutableStateOf(TextFieldValue()) }
    var secondName by remember { mutableStateOf(TextFieldValue()) }
    var maidenName by remember { mutableStateOf(TextFieldValue()) }
    var firstname by remember { mutableStateOf(TextFieldValue()) }
    var codeLab by remember { mutableStateOf(TextFieldValue()) }
    val generatedCode = remember { mutableStateOf("") }

    var sexOptions by remember { mutableStateOf(emptyList<DictionaryEntity>()) }
    var selectedSexId by remember { mutableStateOf<Int?>(null) }
    var sexExpanded by remember { mutableStateOf(false) }

    var birthField by remember { mutableStateOf(TextFieldValue("")) }
    var isBirthValid by remember { mutableStateOf(true) }
    var birthApproxValue by remember { mutableStateOf<Int?>(null) }
    var age by remember { mutableStateOf("") }
    var ageUnit by remember { mutableStateOf<Int?>(null) }
    var ageUnits by remember { mutableStateOf(emptyList<DictionaryEntity>()) }
    var ageUnitExpanded by remember { mutableStateOf(false) }

    var nationalities by remember { mutableStateOf(emptyList<NationalityEntity>()) }
    var selectedNationalityId by remember { mutableStateOf<Int?>(null) }
    var nationalityExpanded by remember { mutableStateOf(false) }

    var isResident by remember { mutableStateOf(true) }

    var bloodGroups by remember { mutableStateOf(emptyList<DictionaryEntity>()) }
    var selectedGroupId by remember { mutableStateOf<Int?>(null) }
    var groupExpanded by remember { mutableStateOf(false) }

    var rhesusList by remember { mutableStateOf(emptyList<DictionaryEntity>()) }
    var selectedRhesusId by remember { mutableStateOf<Int?>(null) }
    var rhesusExpanded by remember { mutableStateOf(false) }

    var phone1 by remember { mutableStateOf(TextFieldValue()) }
    var phone2 by remember { mutableStateOf(TextFieldValue()) }
    var email by remember { mutableStateOf(TextFieldValue()) }
    var address by remember { mutableStateOf(TextFieldValue()) }
    var profession by remember { mutableStateOf(TextFieldValue()) }
    var pbox by remember { mutableStateOf(TextFieldValue()) }
    var district by remember { mutableStateOf(TextFieldValue()) }
    var zipcode by remember { mutableStateOf(TextFieldValue()) }
    var city by remember { mutableStateOf(TextFieldValue()) }

    val isAnonymous = remember(anonymousValue, anonYesId) {
        anonymousValue == anonYesId
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val allDictionaries = database.dictionaryDao().getAll()

            yornOptions = allDictionaries.filter { it.dico_name == "yorn" }.sortedBy { it.position ?: Int.MAX_VALUE }
            anonymousValue = yornOptions.firstOrNull { it.label.equals("Non", ignoreCase = true) }?.id_data
            anonYesId = yornOptions.firstOrNull { it.label.equals("Oui", ignoreCase = true) }?.id_data
            birthApproxValue = yornOptions.firstOrNull { it.label.equals("Non", ignoreCase = true) }?.id_data

            ageUnits = allDictionaries.filter { it.dico_name == "periode_unite" }
                .sortedBy { it.position ?: Int.MAX_VALUE }
            ageUnit = ageUnits.firstOrNull { it.label?.contains("an", ignoreCase = true) == true }?.id_data

            val sexList = allDictionaries.filter { it.dico_name == "sexe" }
            sexOptions = sexList.sortedBy { it.position ?: Int.MAX_VALUE }

            bloodGroups = allDictionaries.filter { it.dico_name == "groupesang" }
                .sortedBy { it.position ?: Int.MAX_VALUE }
            rhesusList = allDictionaries.filter { it.dico_name == "posneg" }
                .sortedBy { it.position ?: Int.MAX_VALUE }

            nationalities = database.nationalityDao().getAll().sortedBy { it.nat_name }

            if (patientId != null) {
                val existing = database.patientDao().getById(patientId)
                if (existing != null) {
                    nom = TextFieldValue(existing.pat_name.orEmpty())
                    secondName = TextFieldValue(existing.pat_midname.orEmpty())
                    maidenName = TextFieldValue(existing.pat_maiden.orEmpty())
                    firstname = TextFieldValue(existing.pat_firstname.orEmpty())
                    codeLab = TextFieldValue(existing.pat_code_lab.orEmpty())
                    birthField = TextFieldValue(existing.pat_birth.orEmpty())
                    age = existing.pat_age?.toString() ?: ""
                    ageUnit = existing.pat_age_unit
                    anonymousValue = existing.pat_ano
                    selectedSexId = existing.pat_sex
                    birthApproxValue = existing.pat_birth_approx
                    selectedNationalityId = existing.pat_nationality
                    isResident = existing.pat_resident == "Y"
                    selectedGroupId = existing.pat_blood_group
                    selectedRhesusId = existing.pat_blood_rhesus
                    address = TextFieldValue(existing.pat_address.orEmpty())
                    phone1 = TextFieldValue(existing.pat_phone1.orEmpty())
                    phone2 = TextFieldValue(existing.pat_phone2.orEmpty())
                    email = TextFieldValue(existing.pat_email.orEmpty())
                    pbox = TextFieldValue(existing.pat_pbox.orEmpty())
                    zipcode = TextFieldValue(existing.pat_zipcode.orEmpty())
                    city = TextFieldValue(existing.pat_city.orEmpty())
                    district = TextFieldValue(existing.pat_district.orEmpty())
                    profession = TextFieldValue(existing.pat_profession.orEmpty())
                    generatedCode.value = existing.pat_code.orEmpty()
                }
            } else {
                val existingCodes = database.patientDao().getAll().mapNotNull { it.pat_code }
                var code: String
                do {
                    code = "LT" + UUID.randomUUID().toString().filter { it.isLetterOrDigit() }.take(6).uppercase()
                } while (existingCodes.contains(code))
                generatedCode.value = code
            }
        }
    }

    fun showDatePicker(context: Context, onDateSelected: (LocalDate) -> Unit) {
        val today = LocalDate.now()
        val dialog = android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
            },
            today.year,
            today.monthValue - 1,
            today.dayOfMonth
        )
        dialog.show()
    }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = {
                    navController.popBackStack()
                }) {
                    Text(stringResource(R.string.retour))
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val trimmedCodeLab = codeLab.text.trim()

                            val existing = withContext(Dispatchers.IO) {
                                if (trimmedCodeLab.isNotBlank()) {
                                    database.patientDao().getByCodeLab(trimmedCodeLab)
                                } else {
                                    null
                                }
                            }

                            // Conflict only if another patient already has this code
                            if (existing != null && existing.id_data != patientId) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.code_patient_deja_existant),
                                    Toast.LENGTH_LONG
                                ).show()
                                return@launch
                            }

                            val missingFields = mutableListOf<String>()
                            if (selectedSexId == null) missingFields.add(context.getString(R.string.sexe))
                            if (age.isBlank() || ageUnit == null) missingFields.add(
                                context.getString(
                                    R.string.age
                                )
                            )
                            if (!isAnonymous && nom.text.isBlank()) missingFields.add(
                                context.getString(
                                    R.string.nom
                                )
                            )

                            if (missingFields.isNotEmpty()) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.champs_obligatoires_manquants) + " : " + missingFields.joinToString(),
                                    Toast.LENGTH_LONG
                                ).show()
                                return@launch
                            }

                            val liteSer =
                                context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
                                    .getInt("lite_ser", -1)

                            val currentUserId = SessionManager.getCurrentUserId(context)

                            val newPatient = PatientEntity(
                                id_data = 0,
                                pat_ano = anonymousValue,
                                pat_code_lab = trimmedCodeLab.ifBlank { null },
                                pat_code = generatedCode.value,
                                pat_name = if (anonymousValue == anonYesId) null else nom.text,
                                pat_midname = if (anonymousValue == anonYesId) null else secondName.text,
                                pat_maiden = if (anonymousValue == anonYesId) null else maidenName.text,
                                pat_firstname = if (anonymousValue == anonYesId) null else firstname.text,
                                pat_sex = selectedSexId,
                                pat_birth = birthField.text.ifBlank { null },
                                pat_birth_approx = birthApproxValue,
                                pat_age = age.toIntOrNull(),
                                pat_age_unit = ageUnit,
                                pat_nationality = selectedNationalityId,
                                pat_resident = if (isResident) "Y" else "N",
                                pat_blood_group = selectedGroupId,
                                pat_blood_rhesus = selectedRhesusId,
                                pat_address = address.text.ifBlank { null },
                                pat_phone1 = phone1.text.ifBlank { null },
                                pat_phone2 = phone2.text.ifBlank { null },
                                pat_profession = profession.text.ifBlank { null },
                                pat_zipcode = zipcode.text.ifBlank { null },
                                pat_city = city.text.ifBlank { null },
                                pat_pbox = pbox.text.ifBlank { null },
                                pat_district = district.text.ifBlank { null },
                                pat_email = email.text.ifBlank { null },
                                pat_lite = liteSer,
                                pat_user = currentUserId // user id from login
                            )

                            val insertedId = withContext(Dispatchers.IO) {
                                if (patientId != null) {
                                    val existing = database.patientDao().getById(patientId)
                                    val updatedPatient = newPatient.copy(
                                        id_data = patientId,
                                        pat_lite = existing?.pat_lite ?: newPatient.pat_lite
                                    )
                                    database.patientDao().update(updatedPatient)
                                    patientId
                                } else {
                                    database.patientDao().insert(newPatient).toInt()
                                }
                            }

                            // Reload the inserted patient to confirm correct id_data
                            val patient = withContext(Dispatchers.IO) {
                                database.patientDao().getById(insertedId)
                            }

                            if (patient != null) {
                                //Log.i("LabBookLite", "Navigating with patient: id=${patient.id_data}, code=${patient.pat_code}")
                                navController.navigate("patient_analysis_request/${patient.id_data}")
                            } else {
                                Toast.makeText(context, "Failed to reload patient", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.enregistrer))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(top = 8.dp, bottom = padding.calculateBottomPadding())
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.identite), style = MaterialTheme.typography.titleLarge)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.anonyme), modifier = Modifier.padding(end = 8.dp))
                yornOptions.forEach {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = anonymousValue == it.id_data,
                            onClick = {
                                anonymousValue = it.id_data
                                if (anonymousValue == anonYesId) {
                                    nom = TextFieldValue()
                                    secondName = TextFieldValue()
                                    maidenName = TextFieldValue()
                                    firstname = TextFieldValue()
                                }
                            }
                        )
                        Text(it.label ?: "")
                    }
                }
            }

            OutlinedTextField(
                nom,
                { nom = it },
                label = { Text("${stringResource(R.string.nom)} *")},
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAnonymous
            )
            OutlinedTextField(
                secondName,
                { secondName = it },
                label = { Text(stringResource(R.string.deuxieme_nom)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAnonymous
            )
            OutlinedTextField(
                maidenName,
                { maidenName = it },
                label = { Text(stringResource(R.string.nom_de_jeune_fille)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAnonymous
            )
            OutlinedTextField(
                firstname,
                { firstname = it },
                label = { Text(stringResource(R.string.prenom_s)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAnonymous
            )

            OutlinedTextField(
                value = TextFieldValue(generatedCode.value),
                onValueChange = {},
                label = { Text(stringResource(R.string.code_interne)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
            OutlinedTextField(
                codeLab,
                { codeLab = it },
                label = { Text(stringResource(R.string.code_patient_interne_au_laboratoire)) },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = sexExpanded,
                onExpandedChange = { sexExpanded = !sexExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = sexOptions.find { it.id_data == selectedSexId }?.label ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("${stringResource(R.string.sexe)} *") },
                    placeholder = { Text(stringResource(R.string.sexe)) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                )
                ExposedDropdownMenu(
                    expanded = sexExpanded,
                    onDismissRequest = { sexExpanded = false }
                ) {
                    sexOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(it.label ?: "") },
                            onClick = {
                                selectedSexId = it.id_data
                                sexExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = birthField,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.date_de_naissance_yyyy_mm_dd)) },
                trailingIcon = {
                    IconButton(onClick = {
                        showDatePicker(context) { date ->
                            val formatted = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            birthField = TextFieldValue(formatted)
                            isBirthValid = true

                            val today = LocalDate.now()
                            val period = Period.between(date, today)

                            when {
                                period.years >= 1 -> {
                                    age = period.years.toString()
                                    ageUnit = ageUnits.firstOrNull { it.label?.contains("an", ignoreCase = true) == true }?.id_data
                                }
                                period.months >= 1 -> {
                                    age = period.months.toString()
                                    ageUnit = ageUnits.firstOrNull { it.label?.contains("mois", ignoreCase = true) == true }?.id_data
                                }
                                else -> {
                                    val days = ChronoUnit.DAYS.between(date, today).toInt()
                                    age = days.toString()
                                    ageUnit = ageUnits.firstOrNull { it.label?.contains("jour", ignoreCase = true) == true }?.id_data
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Select date")
                    }
                },
                isError = !isBirthValid,
                supportingText = {
                    if (!isBirthValid) {
                        Text(
                            stringResource(R.string.date_invalide),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.date_approximative), modifier = Modifier.padding(end = 8.dp))
                yornOptions.forEach {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = birthApproxValue == it.id_data,
                            onClick = { birthApproxValue = it.id_data }
                        )
                        Text(it.label ?: "")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("${stringResource(R.string.age)} *")},
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                ExposedDropdownMenuBox(
                    expanded = ageUnitExpanded,
                    onExpandedChange = { ageUnitExpanded = !ageUnitExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = ageUnits.find { it.id_data == ageUnit }?.label ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.unite)) },
                        modifier = Modifier.menuAnchor(
                            MenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = ageUnitExpanded,
                        onDismissRequest = { ageUnitExpanded = false }) {
                        ageUnits.forEach {
                            DropdownMenuItem(text = { Text(it.label ?: "") }, onClick = {
                                ageUnit = it.id_data
                                ageUnitExpanded = false
                            })
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = nationalityExpanded,
                    onExpandedChange = { nationalityExpanded = !nationalityExpanded },
                    modifier = Modifier.weight(2f)
                ) {
                    OutlinedTextField(
                        value = nationalities.find { it.nat_ser == selectedNationalityId }?.let { "${it.nat_name} (${it.nat_code})" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text(stringResource(R.string.nationalite)) },
                        label = { Text(stringResource(R.string.nationalite)) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded = nationalityExpanded,
                        onDismissRequest = { nationalityExpanded = false }
                    ) {
                        nationalities.forEach {
                            DropdownMenuItem(
                                text = { Text("${it.nat_name} (${it.nat_code})") },
                                onClick = {
                                    selectedNationalityId = it.nat_ser
                                    nationalityExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isResident, onCheckedChange = { isResident = it })
                    Text(stringResource(R.string.patient_resident))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = groupExpanded,
                    onExpandedChange = { groupExpanded = !groupExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = bloodGroups.find { it.id_data == selectedGroupId }?.label ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.groupe_sanguin)) },
                        placeholder = { Text(stringResource(R.string.groupe_sanguin)) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded = groupExpanded,
                        onDismissRequest = { groupExpanded = false }
                    ) {
                        bloodGroups.forEach {
                            DropdownMenuItem(
                                text = { Text(it.label ?: "") },
                                onClick = {
                                    selectedGroupId = it.id_data
                                    groupExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = rhesusExpanded,
                    onExpandedChange = { rhesusExpanded = !rhesusExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = rhesusList.find { it.id_data == selectedRhesusId }?.label ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.rhesus)) },
                        placeholder = { Text(stringResource(R.string.rhesus)) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded = rhesusExpanded,
                        onDismissRequest = { rhesusExpanded = false }
                    ) {
                        rhesusList.forEach {
                            DropdownMenuItem(
                                text = { Text(it.label ?: "") },
                                onClick = {
                                    selectedRhesusId = it.id_data
                                    rhesusExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Text(stringResource(R.string.coordonnees), style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                address,
                { address = it },
                label = { Text(stringResource(R.string.adresse)) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 4
            )
            OutlinedTextField(
                phone1,
                { phone1 = it },
                label = { Text(stringResource(R.string.telephone_1)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                phone2,
                { phone2 = it },
                label = { Text(stringResource(R.string.telephone_2)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                email,
                { email = it },
                label = { Text(stringResource(R.string.email)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                pbox,
                { pbox = it },
                label = { Text(stringResource(R.string.boite_postale)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                district,
                { district = it },
                label = { Text(stringResource(R.string.quartier_secteur)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                zipcode,
                { zipcode = it },
                label = { Text(stringResource(R.string.code_postal)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                city,
                { city = it },
                label = { Text(stringResource(R.string.ville_village)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                profession,
                { profession = it },
                label = { Text(stringResource(R.string.profession)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}