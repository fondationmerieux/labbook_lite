package org.fondationmerieux.labbooklite

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.fondationmerieux.labbooklite.database.entity.DictionaryEntity
import org.fondationmerieux.labbooklite.database.entity.PatientEntity
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.security.KeystoreHelper
import org.fondationmerieux.labbooklite.settings.SettingsScreen
import org.fondationmerieux.labbooklite.ui.viewmodel.*
import org.fondationmerieux.labbooklite.ui.theme.LabBookLiteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dbPassword = KeystoreHelper.getOrCreatePassword(this)
        val database = LabBookLiteDatabase.getDatabase(this, dbPassword)

        //database.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")
        //database.close()

        //Log.d("LabBookLite", "password database = $dbPassword")

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
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
    val loggedIn = prefs.getBoolean("logged_in", false)

    var initialized by remember { mutableStateOf(false) }

    val startDestination by produceState<String?>(initialValue = null, context, database) {
        val configMissing = isConfigurationMissing(context)
        initialized = isAppInitialized(database)
        value = when {
            configMissing || !initialized -> "settings"
            loggedIn -> "home"
            else -> "login"
        }
        Log.d("LabBookLite", "→ configMissing=$configMissing, initialized=$initialized, loggedIn=$loggedIn")
    }

    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else {
        Scaffold(
            topBar = {
                if (currentDestination != "login") {
                    val showMenu = !(currentDestination == "settings" && initialized)
                    AppTopBar(navController, showMenu = showMenu)
                }
            },
            content = { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination!!,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("login") {
                        LoginScreen(database = database, navController = navController)
                    }

                    composable("home") {
                        HomeScreen(navController = navController)
                    }

                    composable("settings") {
                        SettingsScreen(navController = navController)
                    }

                    composable("preferences") {
                        PreferencesScreen(database = database)
                    }

                    composable("new_record") {
                        PatientSearchScreen(database = database, navController = navController)
                    }

                    composable("record_list") {
                        RecordListScreen(database = database, navController = navController)
                    }

                    composable("record_admin/{recordId}") { backStackEntry ->
                        val recordId = backStackEntry.arguments?.getString("recordId")?.toIntOrNull()
                        if (recordId != null) {
                            AdministrativeRecordScreen(recordId = recordId, database = database, navController = navController)
                        }
                    }

                    composable(
                        "record_results/{recordId}",
                        arguments = listOf(navArgument("recordId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val recordId = backStackEntry.arguments?.getInt("recordId") ?: return@composable
                        RecordResultsScreen(recordId = recordId, database = database, navController = navController)
                    }

                    composable("patient_form") {
                        PatientFormScreen(database = database, navController = navController)
                    }

                    composable(
                        "patient_form/{patientId}",
                        arguments = listOf(navArgument("patientId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val patientId = backStackEntry.arguments?.getInt("patientId")
                        if (patientId != null) {
                            PatientFormScreen(database = database, navController = navController, patientId = patientId)
                        }
                    }

                    composable(
                        "patient_analysis_request/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val patientId = backStackEntry.arguments?.getInt("id")
                        var patient by remember { mutableStateOf<PatientEntity?>(null) }
                        var dictionaries by remember { mutableStateOf(emptyList<DictionaryEntity>()) }

                        LaunchedEffect(patientId) {
                            if (patientId != null) {
                                withContext(Dispatchers.IO) {
                                    patient = database.patientDao().getById(patientId)
                                    dictionaries = database.dictionaryDao().getAll()
                                }
                            }
                        }

                        if (patient != null) {
                            LoadPatientAnalysisRequestScreen(
                                navController = navController,
                                patient = patient!!,
                                dictionaries = dictionaries,
                                database = database
                            )
                        } else {
                            Text("Patient data is loading...")
                        }
                    }

                    composable("general") {
                        GeneralScreen(database = database)
                    }

                    composable("about") {
                        AboutScreen()
                    }
                }
            }
        )
    }
}

fun isConfigurationMissing(context: Context): Boolean {
    val prefs = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
    val url = prefs.getString("server_url", null)
    val deviceId = prefs.getString("device_id", null)
    val missing = url.isNullOrEmpty() || deviceId.isNullOrEmpty()
    Log.d("LabBookLite", "isConfigurationMissing = $missing")
    return missing
}

suspend fun isAppInitialized(database: LabBookLiteDatabase): Boolean {
    return withContext(Dispatchers.IO) {
        val userOk = database.userDao().getAll().isNotEmpty()
        val allAnalysis = database.analysisDao().getAll()
        val analysisOk = allAnalysis.isNotEmpty()
        /*
        allAnalysis.forEach {
            Log.i("LabBookLite", "analysis: id=${it.id_data}, code=${it.code}, name=${it.name}, bio_product=${it.bio_product}")
        }
        */

        val linkOk = database.anaLinkDao().getAll().isNotEmpty()
        val varOk = database.anaVarDao().getAll().isNotEmpty()
        val dictOk = database.dictionaryDao().getAll().isNotEmpty()
        val prefsOk = database.preferencesDao().getAll().isNotEmpty()
        val natOk = database.nationalityDao().getAll().isNotEmpty()
        //val prescriberOk = database.prescriberDao().getAll().isNotEmpty()

        /*
        val records = database.recordDao().getAll()
        Log.i("LabBookLite", "==== RECORDS ====")
        records.forEach {
            Log.i("LabBookLite", it.toString())
        }

        val patients = database.patientDao().getAll() //.filter { it.pat_code?.startsWith("LT") == true }
        Log.i("LabBookLite", "==== ALL PATIENTS ====")
        patients.forEach {
            Log.i("LabBookLite", "id=${it.id_data}, code=${it.pat_code}, name=${it.pat_name}, firstname=${it.pat_firstname}")
        }
        */

        Log.d("LabBookLite", "user=$userOk, analysis=$analysisOk, link=$linkOk, var=$varOk, dict=$dictOk, prefs=$prefsOk, nat=$natOk")

        userOk && analysisOk && linkOk && varOk && dictOk && prefsOk && natOk
    }
}
