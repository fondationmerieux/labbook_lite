package org.fondationmerieux.labbooklite

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.database.entity.RecordEntity
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import org.fondationmerieux.labbooklite.security.KeystoreHelper


@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
    val role = prefs.getString("role_type", "") ?: ""

    val dbPassword = KeystoreHelper.getOrCreatePassword(context)
    val db = LabBookLiteDatabase.getDatabase(context, dbPassword)

    var lastRecord by remember { mutableStateOf<RecordEntity?>(null) }
    var urgentPendingCount by remember { mutableStateOf(0) }

    val isLoggedIn = prefs.getBoolean("logged_in", false)

    LaunchedEffect(Unit) {
        Log.i("LabBookLite", "HomeScreen → logged_in = $isLoggedIn")

        val hasUsers = withContext(Dispatchers.IO) {
            val users = db.userDao().getAll()
            Log.i("LabBookLite", "HomeScreen → userDao().getAll() size = ${users.size}")
            users.isNotEmpty()
        }

        Log.i("LabBookLite", "HomeScreen → hasUsers = $hasUsers")


        if (!isLoggedIn || !hasUsers) {
            navController.navigate("settings") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (role) {
            // AGT → new-record + list-record
            "AGT" -> {
                lastRecord?.let { record ->
                    val recordNumber = record.rec_num_lite?.takeLast(4)?.toIntOrNull() ?: record.id_data
                    val recordDate = record.rec_date_save ?: "?"

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Dernier dossier ", style = MaterialTheme.typography.titleMedium)
                            Box(
                                modifier = Modifier
                                    .background(color = Color(0xFFC7AD70), shape = MaterialTheme.shapes.small)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$recordNumber",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                            }
                        }

                        Text(
                            text = "Date de création : $recordDate",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (urgentPendingCount > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFD32F2F), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "$urgentPendingCount dossier(s) avec analyse(s) urgente(s) non validée(s)",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                HomeCard(stringResource(R.string.nouveau_dossier), R.drawable.new_record) {
                    navController.navigate("new_record")
                }
                HomeCard(stringResource(R.string.liste_des_dossiers), R.drawable.list_record) {
                    navController.navigate("record_list")
                }
            }

            // A → settings + preferences
            "A" -> {
                HomeCard(stringResource(R.string.configuration), R.drawable.settings) {
                    navController.navigate("settings")
                }
                HomeCard(stringResource(R.string.pr_f_rences), R.drawable.preferences) {
                    navController.navigate("preferences")
                }
            }
        }
    }
}

@Composable
fun HomeCard(label: String, iconRes: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = label, fontSize = 16.sp)
        }
    }
}