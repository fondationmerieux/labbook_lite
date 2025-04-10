package org.fondationmerieux.labbooklite

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.security.KeystoreHelper
import org.fondationmerieux.labbooklite.settings.SettingsScreen
import org.fondationmerieux.labbooklite.ui.screen.LoadPatientAnalysisRequestScreen
import org.fondationmerieux.labbooklite.ui.theme.LabBookLiteTheme

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
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
    val loggedIn = prefs.getBoolean("logged_in", false)
    var startDestination by remember { mutableStateOf<String?>(null) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val configMissing = isConfigurationMissing(context)
        initialized = isAppInitialized(database)

        startDestination = when {
            configMissing || !initialized -> "settings"
            loggedIn -> "home"
            else -> "login"
        }
    }

    if (startDestination != null) {
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
                        SettingsScreen(database = database, navController = navController)
                    }
                    composable("preferences") {
                        PreferencesScreen(database = database)
                    }
                    composable("new_record") {
                        PatientSearchScreen(database = database, navController = navController)
                    }
                    composable("patient_form") {
                        PatientFormScreen(database = database, navController = navController)
                    }

                    composable("patient_form/{patientId}", arguments = listOf(navArgument("patientId") { type = NavType.IntType })) { backStackEntry ->
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
                        var patient by remember { mutableStateOf<org.fondationmerieux.labbooklite.data.entity.PatientEntity?>(null) }
                        var dictionaries by remember { mutableStateOf(emptyList<org.fondationmerieux.labbooklite.data.entity.DictionaryEntity>()) }

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

                    composable("about") {
                        AboutScreen(navController)
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
        val analysisOk = database.analysisDao().getAll().isNotEmpty()
        val linkOk = database.anaLinkDao().getAll().isNotEmpty()
        val varOk = database.anaVarDao().getAll().isNotEmpty()
        val dictOk = database.dictionaryDao().getAll().isNotEmpty()
        val prefsOk = database.preferencesDao().getAll().isNotEmpty()
        val natOk = database.nationalityDao().getAll().isNotEmpty()

        Log.d("LabBookLite", "user=$userOk, analysis=$analysisOk, link=$linkOk, var=$varOk, dict=$dictOk, prefs=$prefsOk, nat=$natOk")

        userOk && analysisOk && linkOk && varOk && dictOk && prefsOk && natOk
    }
}