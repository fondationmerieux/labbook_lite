package org.fondationmerieux.labbooklite

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import android.graphics.Color as AndroidColor
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.border
import kotlinx.coroutines.launch
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.database.entity.AnaVarEntity
import org.fondationmerieux.labbooklite.database.entity.AnaLinkEntity
import org.fondationmerieux.labbooklite.database.entity.AnalysisEntity
import org.fondationmerieux.labbooklite.database.entity.AnalysisRequestEntity
import org.fondationmerieux.labbooklite.database.entity.AnalysisResultEntity
import org.fondationmerieux.labbooklite.database.entity.AnalysisValidationEntity
import org.fondationmerieux.labbooklite.database.entity.DictionaryEntity
import org.fondationmerieux.labbooklite.database.entity.PatientEntity
import org.fondationmerieux.labbooklite.database.entity.RecordEntity
import kotlin.collections.set
import kotlin.text.Regex
import kotlin.text.isNotBlank
import kotlin.text.startsWith
import org.fondationmerieux.labbooklite.database.model.generateReportHeaderPdf
import java.io.File

/**
 * Created by AlC on 21/04/2025.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordResultsScreen(recordId: Int, database: LabBookLiteDatabase, navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val buttonTexts = remember { mutableStateMapOf<Int, String>() }

    var record by remember { mutableStateOf<RecordEntity?>(null) }
    var patient by remember { mutableStateOf<PatientEntity?>(null) }
    var dictionaries by remember { mutableStateOf<List<DictionaryEntity>>(emptyList()) }
    var requests by remember { mutableStateOf<List<AnalysisRequestEntity>>(emptyList()) }
    var results by remember { mutableStateOf<List<AnalysisResultEntity>>(emptyList()) }
    var analyses by remember { mutableStateOf<List<AnalysisEntity>>(emptyList()) }
    var anaLinks by remember { mutableStateOf<List<AnaLinkEntity>>(emptyList()) }
    var variables by remember { mutableStateOf<List<AnaVarEntity>>(emptyList()) }

    val currentResultIdsMap = remember { mutableStateMapOf<Int, List<Int>>() }
    val resultInputs = remember { mutableStateMapOf<Int, String>() }
    val savedResults = remember { mutableStateMapOf<Int, String>() }
    val validationStatus = remember { mutableStateMapOf<Int, Boolean>() }
    val scope = rememberCoroutineScope()

    val showResetConfirmMap = remember { mutableStateMapOf<Int, Boolean>() }
    val showCancelDialogMap = remember { mutableStateMapOf<Int, Boolean>() }
    var selectedCancelReason by remember { mutableStateOf<Int?>(null) }
    var cancelComment by remember { mutableStateOf("") }

    var validationsLoaded by remember { mutableStateOf(false) }
    val validationMap = remember { mutableStateMapOf<Int, AnalysisValidationEntity?>() }
    val analysisStates = remember { mutableStateMapOf<Int, String>() }

    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(recordId) {
        withContext(Dispatchers.IO) {
            record = database.recordDao().getById(recordId)
            dictionaries = database.dictionaryDao().getAll()
            requests = database.analysisRequestDao().getByRecord(recordId)
            results = database.analysisResultDao().getByRecord(recordId)
            analyses = database.analysisDao().getAll()
            anaLinks = database.anaLinkDao().getAll()
            variables = database.anaVarDao().getAll()
        }

        withContext(Dispatchers.Main) {
            savedResults.clear()
            resultInputs.clear()
            results.forEach { res ->
                val value = res.value.orEmpty().trim()
                savedResults[res.id] = value
                resultInputs[res.id] = value
            }
        }

        record?.patient_id?.let {
            withContext(Dispatchers.IO) {
                patient = database.patientDao().getById(it)
            }
        }

        val validationDao = database.analysisValidationDao()
        val existingValidations = validationDao.getAll()

        results.forEach { result ->
            validationStatus[result.id] = false
        }

        existingValidations.groupBy { it.resultId }.forEach { (resultId, validations) ->
            val latestValidation = validations.maxByOrNull { it.validationDate ?: "" }
            if (resultId != null && latestValidation != null) {
                validationMap[resultId] = latestValidation
                if (latestValidation.validationType == 252) {
                    if (latestValidation.cancelReason != null || !latestValidation.comment.isNullOrBlank()) {
                        // canceled validation
                        validationStatus[resultId] = true
                        resultInputs[resultId] = "ANNULE"
                    } else {
                        // normal validation
                        validationStatus[resultId] = true
                    }
                }
            }
        }

        validationsLoaded = true

        val filteredRequests = requests.filter { req ->
            val analysis = analyses.find { it.id_data == req.analysisRef }
            analysis?.code?.startsWith("PB") != true
        }

        analysisStates.clear()

        /* load analyzes states */
        filteredRequests.forEach { req ->
            val analysisId = req.analysisRef
            val resultIds = results.filter { it.analysisId == req.id }.map { it.id }

            val requiredVarIds = anaLinks
                .filter { it.analysis_id == analysisId && it.required == 4 }
                .map { it.variable_id }
                .toSet()

            val resultMap = resultIds.associateWith { id ->
                results.find { it.id == id }?.value.orEmpty().trim()
            }

            val hasAnyValidation = resultIds.any { id ->
                validationMap[id]?.validationType == 252
            }

            val hasCancel = resultIds.any { id ->
                val v = validationMap[id]
                v?.validationType == 252 && (v.cancelReason != null || !v.comment.isNullOrBlank())
            }

            val requiredFilled = resultIds.all { id ->
                val variableId = results.find { it.id == id }?.variableRef
                if (requiredVarIds.contains(variableId)) {
                    resultMap[id]?.isNotBlank() == true
                } else true
            }

            val anyValueFilled = resultMap.values.any { it.isNotBlank() }
            val allEmpty = resultMap.values.all { it.isBlank() }

            val state = when {
                hasCancel -> "C"
                hasAnyValidation && requiredFilled -> "V"
                requiredFilled || (requiredVarIds.isEmpty() && anyValueFilled) -> "S"
                allEmpty -> "I"
                else -> "I"
            }

            analysisStates[req.id] = state
            //Log.i("LabBookLite", "Analysis reqId=${req.id} → state=$state")
        }
    }

    if (record == null) {
        Text("Chargement du dossier...")
        return
    }

    if (!validationsLoaded) {
        Text("Chargement des résultats en cours...")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        val recordNumber = record!!.rec_num_lite?.takeLast(4)?.toIntOrNull() ?: record!!.id_data

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dossier ",
                style = MaterialTheme.typography.titleLarge
            )
            Box(
                modifier = Modifier
                    .background(Color(0xFFC7AD70), shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$recordNumber",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (!record!!.rec_num_int.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${record!!.rec_num_int})",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (!record!!.rec_date_receipt.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${record!!.rec_date_receipt}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Identité", style = MaterialTheme.typography.titleMedium)

                if (patient == null) {
                    Text("Patient introuvable")
                } else {
                    val codeLabel = listOfNotNull(patient!!.pat_code, patient!!.pat_code_lab)
                        .filter { it.isNotBlank() }
                        .joinToString(" / ")

                    Text("Code : $codeLabel")
                    Text("Nom : ${patient!!.pat_name.orEmpty()}")
                    if (!patient!!.pat_maiden.isNullOrBlank()) {
                        Text("Nom de jeune fille : ${patient!!.pat_maiden}")
                    }
                    Text("Prénom : ${patient!!.pat_firstname.orEmpty()}")

                    val sexLabel = dictionaries
                        .firstOrNull { it.dico_name == "sexe" && it.id_data == patient!!.pat_sex }
                        ?.label ?: "—"
                    Text("Sexe : $sexLabel")

                    Text("Date de naissance : ${patient!!.pat_birth ?: ""}")

                    if (!patient!!.pat_phone1.isNullOrBlank()) Text("Téléphone 1 : ${patient!!.pat_phone1}")
                    if (!patient!!.pat_phone2.isNullOrBlank()) Text("Téléphone 2 : ${patient!!.pat_phone2}")
                    if (!patient!!.pat_email.isNullOrBlank()) Text("Email : ${patient!!.pat_email}")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val filteredRequests = requests.filter { req ->
            val analysis = analyses.find { it.id_data == req.analysisRef }
            analysis?.code?.startsWith("PB") != true
        }

        key(refreshKey) {
            filteredRequests.forEach { req ->
                Log.i("LabBookLite", "Recomposition bloc analyse avec refreshKey=$refreshKey")
                val analysis = analyses.find { it.id_data == req.analysisRef } ?: return@forEach
                val familyLabel =
                    dictionaries.firstOrNull { it.id_data == analysis.family }?.label ?: ""

                // Get the variables from ana_link
                val links = anaLinks.filter { it.analysis_id == analysis.id_data }
                    .sortedBy { it.position ?: Int.MAX_VALUE }

                val variableList = links.mapNotNull { link ->
                    val variable =
                        variables.find { it.id_data == link.variable_id } ?: return@mapNotNull null
                    val unitLabel =
                        dictionaries.firstOrNull { it.id_data == variable.unit }?.label ?: ""
                    val result =
                        results.find { it.analysisId == req.id && it.variableRef == variable.id_data }
                    Triple(result, variable, unitLabel)
                }

                val resultIds = variableList.mapNotNull { it.first?.id }

                resultIds.forEach { resultId ->
                    val initialText =
                        if (!results.find { it.id == resultId }?.value.isNullOrBlank()) "Valider" else "Enregistrer"
                    buttonTexts[resultId] = initialText
                }

                // Define card for an analysis
                Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "${analysis.name} [${familyLabel}]",
                            style = MaterialTheme.typography.titleMedium
                        )

                        variableList.forEach { (res, variable, unitLabel) ->
                            val resultId = res?.id ?: return@forEach
                            val resultTypeId = variable.result_type
                            val dictShortLabel =
                                dictionaries.firstOrNull { it.id_data == resultTypeId }?.short_label.orEmpty()
                            val link = links.find { it.variable_id == variable.id_data }
                            val isRequired = link?.required == 4 && dictShortLabel != "label"
                            val borderModifier = Modifier
                                .weight(2f)
                                .then(
                                    if (isRequired) Modifier.border(
                                        width = 1.dp,
                                        color = Color.Blue,
                                        shape = MaterialTheme.shapes.small
                                    ) else Modifier
                                )


                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (dictShortLabel != "label") {
                                    Text(variable.label ?: "—", modifier = Modifier.weight(1.5f))
                                }

                                when {
                                    /* Type of result : select response */
                                    dictShortLabel.startsWith("dico_") -> {
                                        val dicoName = dictShortLabel.removePrefix("dico_")
                                        val options =
                                            dictionaries.filter { it.dico_name == dicoName }

                                        var selectedId by remember(resultId) {
                                            mutableStateOf(
                                                (resultInputs[resultId]
                                                    ?: res.value.orEmpty()).toIntOrNull()
                                            )
                                        }

                                        if (!resultInputs.contains(resultId)) {
                                            resultInputs[resultId] =
                                                selectedId?.toString().orEmpty()
                                        }

                                        var expanded by remember { mutableStateOf(false) }

                                        fun getLabel(id: Int?): String =
                                            options.firstOrNull { it.id_data == id }?.label.orEmpty()

                                        Box(modifier = Modifier.weight(2f)) {
                                            ExposedDropdownMenuBox(
                                                expanded = expanded,
                                                onExpandedChange = { expanded = !expanded }
                                            ) {
                                                OutlinedTextField(
                                                    value = if (validationMap[resultId]?.let {
                                                            isResultCancelled(
                                                                it
                                                            )
                                                        } == true) {
                                                        "ANNULE"
                                                    } else {
                                                        getLabel(selectedId)
                                                    },
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Valeur") },
                                                    trailingIcon = {
                                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                                            expanded
                                                        )
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .menuAnchor()
                                                )
                                                ExposedDropdownMenu(
                                                    expanded = expanded,
                                                    onDismissRequest = { expanded = false }
                                                ) {
                                                    options.forEach { opt ->
                                                        DropdownMenuItem(
                                                            text = { Text(opt.label.orEmpty()) },
                                                            onClick = {
                                                                selectedId = opt.id_data
                                                                resultInputs[resultId] =
                                                                    opt.id_data.toString()

                                                                val saved =
                                                                    savedResults[resultId].orEmpty()
                                                                val current =
                                                                    resultInputs[resultId].orEmpty()
                                                                        .trim()
                                                                val isValidated =
                                                                    validationMap[resultId]?.validationType == 252 &&
                                                                            validationMap[resultId]?.cancelReason == null &&
                                                                            validationMap[resultId]?.comment.isNullOrBlank()

                                                                buttonTexts[resultId] = when {
                                                                    isValidated -> ""
                                                                    current == saved && saved.isNotBlank() -> "Valider"
                                                                    else -> "Enregistrer"
                                                                }
                                                                /*
                                                                Log.i(
                                                                    "LabBookLite",
                                                                    "Dropdown click - resultId=$resultId, saved='$saved', current='$current', action=${buttonTexts[resultId]}"
                                                                )
                                                                */
                                                                expanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    /* Type of result : inactive only label display */
                                    dictShortLabel == "label" -> {
                                        Text(
                                            text = variable.label.orEmpty(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier
                                                .background(Color(0xFFF0F0F0))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }

                                    /* Type of result : write an integer number */
                                    dictShortLabel == "integer" -> {
                                        val saved = savedResults[resultId].orEmpty()

                                        OutlinedTextField(
                                            value = resultInputs[resultId] ?: "",
                                            onValueChange = { newInput ->
                                                val trimmedInput = newInput.trim()
                                                resultInputs[resultId] = trimmedInput

                                                val isValidated =
                                                    validationMap[resultId]?.validationType == 252 &&
                                                            validationMap[resultId]?.cancelReason == null &&
                                                            validationMap[resultId]?.comment.isNullOrBlank()

                                                val action = when {
                                                    isValidated -> ""
                                                    trimmedInput == saved && saved.isNotBlank() -> "Valider"
                                                    else -> "Enregistrer"
                                                }

                                                buttonTexts[resultId] = action

                                                /*
                                                Log.i(
                                                    "LabBookLite",
                                                    "Bloc integer - ResultId: $resultId - Saved: '$saved' - NewInput: '$trimmedInput' => $action"
                                                )
                                                */
                                            },
                                            modifier = borderModifier,
                                            keyboardOptions = KeyboardOptions.Default.copy(
                                                keyboardType = KeyboardType.Number
                                            )
                                        )
                                    }

                                    /* Type of result : write a float number */
                                    dictShortLabel == "float" -> {
                                        val savedValue =
                                            results.find { it.id == resultId }?.value.orEmpty()
                                        val accuracy = variable.accuracy ?: 0
                                        OutlinedTextField(
                                            value = if (validationMap[resultId]?.let {
                                                    isResultCancelled(
                                                        it
                                                    )
                                                } == true) {
                                                "ANNULE"
                                            } else {
                                                resultInputs[resultId] ?: savedValue
                                            },
                                            onValueChange = { input ->
                                                val cleaned = if (accuracy > 0) {
                                                    val parts = input.replace(',', '.').split(".")
                                                    if (parts.size == 2) {
                                                        val decimals = parts[1].take(accuracy)
                                                        "${parts[0]}.$decimals"
                                                    } else input
                                                } else input

                                                resultInputs[resultId] = cleaned
                                                val saved = savedResults[resultId].orEmpty()
                                                buttonTexts[resultId] =
                                                    if (cleaned == saved && saved.isNotBlank()) "Valider" else "Enregistrer"
                                            },
                                            modifier = borderModifier,
                                            keyboardOptions = KeyboardOptions.Default.copy(
                                                keyboardType = KeyboardType.Decimal
                                            )
                                        )
                                    }

                                    /* Type of result : calculate result, readonly cell */
                                    dictShortLabel == "calculee" -> {
                                        val formula = variable.formula.orEmpty()
                                        val varNumbers = Regex("\\$\\_(\\d+)").findAll(formula)
                                            .map { it.groupValues[1].toInt() }.toSet()

                                        val canCalculate = varNumbers.all { varNum ->
                                            val linkedVar =
                                                anaLinks.find { it.analysis_id == analysis.id_data && it.var_number == varNum }?.variable_id
                                            val linkedResultId =
                                                results.find { it.analysisId == req.id && it.variableRef == linkedVar }?.id
                                            val linkedValue = linkedResultId?.let {
                                                resultInputs[it]
                                                    ?: results.find { r -> r.id == it }?.value
                                            }
                                            !linkedValue.isNullOrBlank()
                                        }

                                        val displayValue = if (canCalculate) {
                                            var expr = formula
                                            var valid = true
                                            varNumbers.forEach { varNum ->
                                                val linkedVar =
                                                    anaLinks.find { it.analysis_id == analysis.id_data && it.var_number == varNum }?.variable_id
                                                val linkedResultId =
                                                    results.find { it.analysisId == req.id && it.variableRef == linkedVar }?.id
                                                val linkedValue = linkedResultId?.let {
                                                    resultInputs[it]
                                                        ?: results.find { r -> r.id == it }?.value
                                                }

                                                if (linkedValue.isNullOrBlank() || linkedValue == "0" && formula.contains(
                                                        "/_${varNum}"
                                                    )
                                                ) {
                                                    valid = false
                                                }
                                                expr = expr.replace(
                                                    "\$_$varNum",
                                                    linkedValue?.replace(",", ".") ?: "0"
                                                )
                                            }

                                            if (valid) {
                                                try {
                                                    val calculated = evalSimpleExpression(expr)
                                                    val accuracy = variable.accuracy ?: 0
                                                    if (accuracy > 0) "%.${accuracy}f".format(
                                                        calculated
                                                    ) else calculated.toString()
                                                } catch (_: Exception) {
                                                    ""
                                                }
                                            } else {
                                                ""
                                            }
                                        } else {
                                            ""
                                        }

                                        resultInputs[resultId] = displayValue

                                        OutlinedTextField(
                                            value = displayValue,
                                            onValueChange = {},
                                            readOnly = true,
                                            modifier = Modifier
                                                .weight(2f)
                                                .background(Color(0xFFE0E0E0))
                                        )
                                    }

                                    /* Type of result : others type often string result */
                                    else -> {
                                        OutlinedTextField(
                                            value = getDisplayedValue(
                                                resultId,
                                                resultInputs,
                                                results,
                                                validationMap
                                            ),
                                            onValueChange = { newInput ->
                                                resultInputs[resultId] = newInput
                                                val savedValue = res.value.orEmpty()
                                                buttonTexts[resultId] = when {
                                                    savedValue.isBlank() -> "Enregistrer"
                                                    newInput != savedValue -> "Enregistrer"
                                                    else -> "Valider"
                                                }
                                            },
                                            modifier = borderModifier
                                        )
                                    }
                                }

                                val showMinMax = variable.var_show_minmax == "Y"
                                val interval =
                                    if (showMinMax && !variable.normal_min.isNullOrBlank() && !variable.normal_max.isNullOrBlank()) {
                                        " [${variable.normal_min} - ${variable.normal_max}]"
                                    } else ""

                                Text(unitLabel + interval, modifier = Modifier.weight(1.5f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    when (analysisStates[req.id]) {
                        /* INTIAL state for this analysis => button save */
                        "I" -> {
                            val hasAnyInput = resultIds.any { id -> resultInputs[id]?.isNotBlank() == true }

                            if (hasAnyInput) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(onClick = {
                                        scope.launch {
                                            val updates = resultIds.map { id ->
                                                val value = resultInputs[id].orEmpty().trim()
                                                id to value
                                            }
                                            withContext(Dispatchers.IO) {
                                                updates.forEach { (id, value) ->
                                                    database.analysisResultDao().updateValue(id, value)
                                                }
                                                results = database.analysisResultDao().getByRecord(recordId)
                                            }
                                            withContext(Dispatchers.Main) {
                                                updates.forEach { (id, value) ->
                                                    savedResults[id] = value
                                                    buttonTexts[id] = "Valider"
                                                }
                                                recalculateAnalysisStates(
                                                    requests, results, analyses, anaLinks, validationMap, analysisStates
                                                )
                                                refreshKey++
                                                Toast.makeText(context, "Résultats enregistrés.", Toast.LENGTH_SHORT).show()
                                                updateRecordStatusIfNeeded(database, record!!, analysisStates)
                                            }
                                        }
                                    }) {
                                        Text("Enregistrer")
                                    }
                                }
                            }
                        }

                        /* SAVED state for this analysis => button valid */
                        "S" -> {
                            val anyModified = resultIds.any { resultId ->
                                val saved = savedResults[resultId].orEmpty().trim()
                                val current = resultInputs[resultId].orEmpty().trim()
                                saved != current
                            }

                            val showLabel = if (anyModified) "Enregistrer" else "Valider"

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(onClick = {
                                    currentResultIdsMap[req.id] = resultIds

                                    scope.launch {
                                        if (anyModified) {
                                            // → enregistrer à nouveau si modifié
                                            val updates = resultIds.map { id ->
                                                val value = resultInputs[id].orEmpty().trim()
                                                id to value
                                            }

                                            withContext(Dispatchers.IO) {
                                                updates.forEach { (id, value) ->
                                                    database.analysisResultDao().updateValue(id, value)
                                                }
                                                results = database.analysisResultDao().getByRecord(recordId)
                                            }

                                            withContext(Dispatchers.Main) {
                                                updates.forEach { (id, value) ->
                                                    savedResults[id] = value
                                                    buttonTexts[id] = "Valider"
                                                }
                                                recalculateAnalysisStates(
                                                    requests, results, analyses, anaLinks, validationMap, analysisStates
                                                )
                                                refreshKey++
                                                Toast.makeText(context, "Résultats enregistrés.", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            // → valider si aucune modif
                                            val validationDao = database.analysisValidationDao()
                                            val maxId = withContext(Dispatchers.IO) {
                                                validationDao.getMaxId() ?: 0
                                            }
                                            val newIdStart = maxId + 1
                                            val validationDate = getCurrentDateTime()
                                            val userId = getUserIdFromPreferences(context)

                                            val validationsToInsert = resultIds.mapIndexed { index, resultId ->
                                                val inputValue = resultInputs[resultId]
                                                    ?: results.find { it.id == resultId }?.value.orEmpty()

                                                val variableId = results.find { it.id == resultId }?.variableRef
                                                val variable = variables.find { it.id_data == variableId }
                                                val dictShortLabel = dictionaries.firstOrNull { it.id_data == variable?.result_type }?.short_label.orEmpty()

                                                val finalValue = if (dictShortLabel.startsWith("dico_")) {
                                                    inputValue.toIntOrNull()?.toString() ?: dictionaries
                                                        .firstOrNull { it.dico_name == dictShortLabel.removePrefix("dico_") && it.label == inputValue }
                                                        ?.id_data?.toString().orEmpty()
                                                } else {
                                                    inputValue
                                                }

                                                AnalysisValidationEntity(
                                                    id = newIdStart + index,
                                                    resultId = resultId,
                                                    validationDate = validationDate,
                                                    userId = userId,
                                                    value = finalValue,
                                                    validationType = 252,
                                                    comment = null,
                                                    cancelReason = null
                                                )
                                            }

                                            withContext(Dispatchers.IO) {
                                                validationDao.insertAll(validationsToInsert)
                                            }

                                            withContext(Dispatchers.Main) {
                                                validationsToInsert.forEach { validation ->
                                                    validation.resultId?.let { resultId ->
                                                        validationMap[resultId] = validation
                                                        validationStatus[resultId] = true
                                                        savedResults[resultId] = resultInputs[resultId].orEmpty().trim()
                                                        buttonTexts[resultId] = ""
                                                    }

                                                }
                                                Toast.makeText(context, "Validation enregistrée.", Toast.LENGTH_SHORT).show()
                                                updateRecordStatusIfNeeded(database, record!!, analysisStates)

                                                record = withContext(Dispatchers.IO) {
                                                    database.recordDao().getById(recordId)
                                                }

                                                recalculateAnalysisStates(
                                                    requests,
                                                    results,
                                                    analyses,
                                                    anaLinks,
                                                    validationMap,
                                                    analysisStates
                                                )
                                                refreshKey++
                                            }
                                        }
                                    }
                                }) {
                                    Text(showLabel)
                                }
                            }
                        }

                        /* VALID state for this analysis => buttons cancel + reset */
                        "V" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(onClick = {
                                    currentResultIdsMap[req.id] = resultIds
                                    showCancelDialogMap[req.id] = true
                                }) {
                                    Text("Annuler")
                                }

                                Button(onClick = {
                                    currentResultIdsMap[req.id] = resultIds
                                    showResetConfirmMap[req.id] = true
                                }) {
                                    Text("Réinitialiser")
                                }
                            }

                            if (showResetConfirmMap.getOrElse(req.id) { false }) {
                                AlertDialog(
                                    onDismissRequest = { showResetConfirmMap[req.id] = false },
                                    title = { Text("Confirmation") },
                                    text = { Text("Voulez-vous réinitialiser les résultats de cette analyse ?") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            showResetConfirmMap[req.id] = false
                                            scope.launch {
                                                withContext(Dispatchers.IO) {
                                                    currentResultIdsMap[req.id]?.forEach { resultId ->
                                                        database.analysisResultDao()
                                                            .updateValue(resultId, "")
                                                        database.analysisValidationDao()
                                                            .deleteByResultId(resultId)
                                                    }
                                                }
                                                val updatedResults = withContext(Dispatchers.IO) {
                                                    database.analysisResultDao()
                                                        .getByRecord(recordId)
                                                }

                                                withContext(Dispatchers.Main) {
                                                    val resetResultIds = currentResultIdsMap[req.id] ?: emptyList()

                                                    results = results.map { res ->
                                                        if (resetResultIds.contains(res.id)) res.copy(value = "") else res
                                                    }

                                                    resetResultIds.forEach { resultId ->
                                                        resultInputs[resultId] = ""
                                                        validationStatus[resultId] = false
                                                        validationMap[resultId] = null
                                                        buttonTexts[resultId] = "Enregistrer"
                                                        savedResults[resultId] = ""
                                                    }

                                                    Toast.makeText(
                                                        context,
                                                        "Résultats réinitialisés.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    updateRecordStatusIfNeeded(database, record!!, analysisStates)

                                                    record = withContext(Dispatchers.IO) {
                                                        database.recordDao().getById(recordId)
                                                    }

                                                    recalculateAnalysisStates(
                                                        requests,
                                                        results,
                                                        analyses,
                                                        anaLinks,
                                                        validationMap,
                                                        analysisStates
                                                    )
                                                    refreshKey++
                                                }
                                            }
                                        }) {
                                            Text("Oui")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = {
                                            showResetConfirmMap[req.id] = false
                                        }) {
                                            Text("Non")
                                        }
                                    }
                                )
                            }

                            if (showCancelDialogMap[req.id] == true) {
                                val cancelMotifs = dictionaries.filter { it.dico_name == "motif_annulation" }
                                var expanded by remember { mutableStateOf(false) }

                                AlertDialog(
                                    onDismissRequest = { showCancelDialogMap[req.id] = false },
                                    title = { Text("Annulation de validation") },
                                    text = {
                                        Column {
                                            Text("Motif d'annulation", style = MaterialTheme.typography.bodyMedium)
                                            Spacer(modifier = Modifier.height(8.dp))

                                            ExposedDropdownMenuBox(
                                                expanded = expanded,
                                                onExpandedChange = { expanded = !expanded }
                                            ) {
                                                OutlinedTextField(
                                                    value = cancelMotifs.firstOrNull { it.id_data == selectedCancelReason }?.label ?: "",
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Choisir un motif") },
                                                    modifier = Modifier
                                                        .menuAnchor()
                                                        .fillMaxWidth()
                                                )
                                                ExposedDropdownMenu(
                                                    expanded = expanded,
                                                    onDismissRequest = { expanded = false }
                                                ) {
                                                    cancelMotifs.forEach { motif ->
                                                        DropdownMenuItem(
                                                            text = { Text(motif.label ?: "") },
                                                            onClick = {
                                                                selectedCancelReason = motif.id_data
                                                                expanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Text("Commentaire", style = MaterialTheme.typography.bodyMedium)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedTextField(
                                                value = cancelComment,
                                                onValueChange = { cancelComment = it },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(100.dp)
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            showCancelDialogMap[req.id] = false
                                            scope.launch {
                                                withContext(Dispatchers.IO) {
                                                    val validationDao = database.analysisValidationDao()
                                                    val maxId = validationDao.getMaxId() ?: 0
                                                    val validationDate = getCurrentDateTime()
                                                    val userId = getUserIdFromPreferences(context)

                                                    resultIds.forEachIndexed { index, resultId ->
                                                        val newId = maxId + index + 1
                                                        val validation = AnalysisValidationEntity(
                                                            id = newId,
                                                            resultId = resultId,
                                                            validationDate = validationDate,
                                                            userId = userId,
                                                            value = "",
                                                            validationType = 252,
                                                            comment = cancelComment,
                                                            cancelReason = selectedCancelReason
                                                        )
                                                        validationDao.insertAll(listOf(validation))
                                                        database.analysisResultDao().updateValue(resultId, "")
                                                    }
                                                }

                                                withContext(Dispatchers.Main) {
                                                    resultIds.forEach { resultId ->
                                                        validationStatus[resultId] = true
                                                        validationMap[resultId] = AnalysisValidationEntity(
                                                            id = 0,
                                                            resultId = resultId,
                                                            validationDate = null,
                                                            userId = 0,
                                                            value = "",
                                                            validationType = 252,
                                                            comment = cancelComment,
                                                            cancelReason = selectedCancelReason
                                                        )
                                                        resultInputs[resultId] = "ANNULE"
                                                        buttonTexts[resultId] = ""
                                                    }
                                                    recalculateAnalysisStates(
                                                        requests,
                                                        results,
                                                        analyses,
                                                        anaLinks,
                                                        validationMap,
                                                        analysisStates
                                                    )
                                                    refreshKey++
                                                    Toast.makeText(context, "Résultats annulés.", Toast.LENGTH_SHORT).show()
                                                    updateRecordStatusIfNeeded(database, record!!, analysisStates)
                                                }
                                            }
                                        }) {
                                            Text("Confirmer")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = {
                                            showCancelDialogMap[req.id] = false
                                        }) {
                                            Text("Annuler")
                                        }
                                    }
                                )
                            }
                        }

                        /* CANCEL state for this analysis => button reset */
                        "C" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(onClick = {
                                    currentResultIdsMap[req.id] = resultIds
                                    showResetConfirmMap[req.id] = true
                                }) {
                                    Text("Réinitialiser")
                                }
                            }

                            if (showResetConfirmMap.getOrElse(req.id) { false }) {
                                AlertDialog(
                                    onDismissRequest = { showResetConfirmMap[req.id] = false },
                                    title = { Text("Confirmation") },
                                    text = { Text("Voulez-vous réinitialiser les résultats de cette analyse ?") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            showResetConfirmMap[req.id] = false
                                            scope.launch {
                                                withContext(Dispatchers.IO) {
                                                    currentResultIdsMap[req.id]?.forEach { resultId ->
                                                        database.analysisResultDao()
                                                            .updateValue(resultId, "")
                                                        database.analysisValidationDao()
                                                            .deleteByResultId(resultId)
                                                    }
                                                }
                                                val updatedResults = withContext(Dispatchers.IO) {
                                                    database.analysisResultDao()
                                                        .getByRecord(recordId)
                                                }

                                                withContext(Dispatchers.Main) {
                                                    val resetResultIds = currentResultIdsMap[req.id] ?: emptyList()

                                                    results = results.map { res ->
                                                        if (resetResultIds.contains(res.id)) res.copy(value = "") else res
                                                    }

                                                    resetResultIds.forEach { resultId ->
                                                        resultInputs[resultId] = ""
                                                        validationStatus[resultId] = false
                                                        validationMap[resultId] = null
                                                        buttonTexts[resultId] = "Enregistrer"
                                                        savedResults[resultId] = ""
                                                    }

                                                    Toast.makeText(
                                                        context,
                                                        "Résultats réinitialisés.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    recalculateAnalysisStates(
                                                        requests,
                                                        results,
                                                        analyses,
                                                        anaLinks,
                                                        validationMap,
                                                        analysisStates
                                                    )
                                                    refreshKey++
                                                }
                                            }
                                        }) {
                                            Text("Oui")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = {
                                            showResetConfirmMap[req.id] = false
                                        }) {
                                            Text("Non")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            key(refreshKey) {
                val analysisStatesList = analysisStates.values.toList()
                val hasValidated = analysisStatesList.any { it == "V" }
                val hasCancelled = analysisStatesList.any { it == "C" }
                val hasPending = analysisStatesList.any { it == "I" || it == "S" }

                val reportEnabled = (hasValidated || hasCancelled)
                val reportLabel = when {
                    reportEnabled && !hasPending -> "Rapport complet"
                    reportEnabled && hasPending -> "Rapport partiel"
                    else -> "Rapport"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = { navController.popBackStack() }) {
                        Text("Retour")
                    }

                    Button(
                        onClick = {
                            val baseFilename = "cr_${record!!.rec_num_lite ?: record!!.id_data}"
                            val existingFiles = context.filesDir.listFiles { _, name ->
                                name.startsWith(baseFilename)
                            } ?: emptyArray()

                            val nextIndex = if (existingFiles.isEmpty()) "" else "-${existingFiles.size}"
                            val finalFilename = "$baseFilename$nextIndex.pdf"

                            val file = File(context.filesDir, finalFilename)


                            try {
                                generateReportHeaderPdf(context, file.name, database, recordId)

                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        database.recordDao().update(record!!.copy(status = 256))
                                    }
                                }

                                Toast.makeText(context, "PDF généré", Toast.LENGTH_SHORT).show()
                                navController.navigate("record_admin/${recordId}")
                            } catch (e: Exception) {
                                Log.e("LabBookLite", "Erreur lors de la génération du PDF", e)

                                val drawable = GradientDrawable().apply {
                                    setColor(AndroidColor.RED)
                                    cornerRadius = 24f
                                }

                                val textView = TextView(context).apply {
                                    text = "Erreur lors de la génération du PDF"
                                    setBackground(drawable)
                                    setTextColor(AndroidColor.WHITE)
                                    textSize = 16f
                                    setPadding(32, 16, 32, 16)
                                    gravity = Gravity.CENTER
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                }

                                val toast = Toast(context)
                                toast.duration = Toast.LENGTH_LONG
                                toast.view = textView
                                toast.setGravity(
                                    Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
                                    0,
                                    100
                                )
                                toast.show()
                            }
                        },
                        enabled = reportEnabled && record!!.status != 256
                    ) {
                        Text(reportLabel)
                    }
                }
            }
        }
    }
}

fun recalculateAnalysisStates(
    requests: List<AnalysisRequestEntity>,
    results: List<AnalysisResultEntity>,
    analyses: List<AnalysisEntity>,
    anaLinks: List<AnaLinkEntity>,
    validationMap: Map<Int, AnalysisValidationEntity?>,
    analysisStates: MutableMap<Int, String>
) {
    analysisStates.clear()

    val filteredRequests = requests.filter { req ->
        val analysis = analyses.find { it.id_data == req.analysisRef }
        analysis?.code?.startsWith("PB") != true
    }

    filteredRequests.forEach { req ->
        val analysisId = req.analysisRef
        val resultIds = results.filter { it.analysisId == req.id }.map { it.id }

        val requiredVarIds = anaLinks
            .filter { it.analysis_id == analysisId && it.required == 4 }
            .map { it.variable_id }
            .toSet()

        val resultMap = resultIds.associateWith { id ->
            results.find { it.id == id }?.value.orEmpty().trim()
        }

        val hasAnyValidation = resultIds.any { id ->
            validationMap[id]?.validationType == 252
        }

        val hasCancel = resultIds.any { id ->
            val v = validationMap[id]
            v?.validationType == 252 && (v.cancelReason != null || !v.comment.isNullOrBlank())
        }

        val requiredFilled = resultIds.all { id ->
            val variableId = results.find { it.id == id }?.variableRef
            if (requiredVarIds.contains(variableId)) {
                resultMap[id]?.isNotBlank() == true
            } else true
        }

        val anyValueFilled = resultMap.values.any { it.isNotBlank() }
        val allEmpty = resultMap.values.all { it.isBlank() }

        val state = when {
            hasCancel -> "C"
            hasAnyValidation && requiredFilled -> "V"
            requiredFilled || (requiredVarIds.isEmpty() && anyValueFilled) -> "S"
            allEmpty -> "I"
            else -> "I"
        }

        analysisStates[req.id] = state
        //Log.i("LabBookLite", "Recalcul après action → reqId=${req.id} → state=$state")
    }
}

fun getCurrentDateTime(): String {
    val now = java.time.LocalDateTime.now()
    val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return now.format(formatter)
}

fun getUserIdFromPreferences(context: Context): Int {
    val sharedPreferences = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getInt("user_id", 0)
}

fun getDisplayedValue(
    resultId: Int,
    resultInputs: Map<Int, String>,
    results: List<AnalysisResultEntity>,
    validationMap: Map<Int, AnalysisValidationEntity?>
): String {
    val latestValidation = validationMap[resultId]
    if (latestValidation?.validationType == 252 &&
        (latestValidation.cancelReason != null || !latestValidation.comment.isNullOrBlank())
    ) {
        return "ANNULE"
    }
    return resultInputs[resultId] ?: results.find { it.id == resultId }?.value.orEmpty()
}

fun evalSimpleExpression(expression: String): Double {
    val cleanExpr = expression.replace(" ", "")
    return calculate(cleanExpr)
}

private fun calculate(expr: String): Double {
    var expression = expr
    if (expression.startsWith("+")) {
        expression = expression.substring(1)
    }

    val stack = mutableListOf<Double>()
    var num = ""
    var sign = '+'

    var i = 0
    while (i < expression.length) {
        val c = expression[i]
        if (c.isDigit() || c == '.' || c == ',') {
            num += if (c == ',') '.' else c
        }
        if (!c.isDigit() && c != '.' && c != ',' || i == expression.lastIndex) {
            val number = num.toDoubleOrNull() ?: 0.0
            when (sign) {
                '+' -> stack.add(number)
                '-' -> stack.add(-number)
                '*' -> stack[stack.lastIndex] = stack.last() * number
                '/' -> stack[stack.lastIndex] = if (number != 0.0) stack.last() / number else 0.0
            }
            sign = c
            num = ""
        }
        i++
    }

    return stack.sum()
}

fun isResultCancelled(validation: AnalysisValidationEntity?): Boolean {
    return validation?.validationType == 252 && (validation.cancelReason != null || !validation.comment.isNullOrBlank())
}

suspend fun updateRecordStatusIfNeeded(
    database: LabBookLiteDatabase,
    record: RecordEntity,
    analysisStates: Map<Int, String>
) {
    val currentStatus = record.status
    val states = analysisStates.values

    val newStatus = when {
        currentStatus == 182 && states.any { it == "S" || it == "V" } -> 253
        currentStatus == 253 && states.any { it == "V" } -> 255
        currentStatus != 256 && states.all { it == "V" || it == "C" } -> 254
        currentStatus in listOf(255, 256) && states.any { it != "I" } -> 255
        states.all { it == "I" } -> 182
        else -> null
    }

    if (newStatus != null && newStatus != currentStatus) {
        val updated = record.copy(status = newStatus)
        database.recordDao().update(updated)
    }
}
