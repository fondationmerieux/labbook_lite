package org.fondationmerieux.labbooklite

import android.content.Context
import androidx.compose.foundation.Image
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

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
    val role = prefs.getString("role_type", "") ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (role) {
            // AGT → new-record + list-record
            "AGT" -> {
                HomeCard(stringResource(R.string.nouveau_dossier), R.drawable.new_record) {
                    navController.navigate("new_record")
                }
                HomeCard(stringResource(R.string.liste_des_dossiers), R.drawable.list_record) {
                    // TODO: navigate to list record
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