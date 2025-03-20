package org.fondationmerieux.labbooklite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.database.Patient
import org.fondationmerieux.labbooklite.security.KeystoreHelper
import org.fondationmerieux.labbooklite.ui.theme.LabBookLiteTheme
import androidx.navigation.compose.rememberNavController
import org.fondationmerieux.labbooklite.settings.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dbPassword = KeystoreHelper.getOrCreatePassword(this)
        val database = LabBookLiteDatabase.getDatabase(this, dbPassword)

        setContent {
            LabBookLiteTheme {
                MainScreen(database)
            }
        }
    }
}

@Composable
fun MainScreen(database: LabBookLiteDatabase) {
    val navController = rememberNavController()

    Scaffold(
        topBar = { AppTopBar(navController) },
        content = { innerPadding ->
            NavHost(navController, startDestination = "home", Modifier.padding(innerPadding)) {
                composable("home") { PatientScreen(database, navController) }
                composable("settings") { SettingsScreen() }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(navController: NavController) {
    TopAppBar(
        title = { Text("LabBookLite") },
        actions = {
            IconButton(onClick = { navController.navigate("settings") }) {
                Icon(Icons.Default.Settings, contentDescription = "Paramètres")
            }
        }
    )
}

/**
 * Composable function to display the list of patients.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientScreen(database: LabBookLiteDatabase, navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    var patients by remember { mutableStateOf(emptyList<Patient>()) }

    // Load patients when the screen is displayed
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            patients = database.patientDao().getAllPatients()
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Liste des patients", style = MaterialTheme.typography.headlineMedium)
        PatientList(patients)
        FloatingActionButton(onClick = {
            coroutineScope.launch {
                val newPatient = Patient(name = "John Doe", birthdate = "1985-07-12", diagnosis = "Flu")
                database.patientDao().insert(newPatient)
                patients = database.patientDao().getAllPatients()
            }
        }) {
            Text("+")
        }
    }
}

/**
 * Composable function to display a list of patients.
 */
@Composable
fun PatientList(patients: List<Patient>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(patients) { patient ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Name: ${patient.name}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Birthdate: ${patient.birthdate}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Diagnosis: ${patient.diagnosis}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}