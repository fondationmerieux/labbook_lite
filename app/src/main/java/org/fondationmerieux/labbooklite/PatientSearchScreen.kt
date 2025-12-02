package org.fondationmerieux.labbooklite

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.database.entity.PatientEntity
import org.fondationmerieux.labbooklite.cards.PatientDetailsSearchCard

@Composable
fun PatientSearchScreen(
    database: LabBookLiteDatabase,
    navController: NavController
) {
    val context = LocalContext.current
    val patientDao = database.patientDao()

    var queryText by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<PatientEntity>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                OutlinedButton(onClick = { navController.popBackStack() }) {
                    Text(stringResource(id = R.string.retour))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.rechercher_un_patient),
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = queryText,
                onValueChange = {
                    queryText = it
                    if (queryText.length >= 3) {
                        coroutineScope.launch {
                            results = withContext(Dispatchers.IO) {
                                patientDao.searchPatients(queryText)
                            }
                        }
                    } else {
                        results = emptyList()
                    }
                },
                label = { Text(stringResource(R.string.nom_prenom_code_telephone_ou_email)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { navController.navigate("patient_form") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.creer_un_nouveau_patient))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (results.isNotEmpty()) {
                Text(
                    stringResource(R.string.resultats),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(results) { patient ->
                        PatientDetailsSearchCard(
                            patient = patient,
                            onClick = {
                                navController.navigate("patient_analysis_request/${patient.id_data}")
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.patient_selectionne, patient.pat_name),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            } else if (queryText.length >= 3) {
                Text(
                    text = stringResource(R.string.aucun_patient_trouve),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}