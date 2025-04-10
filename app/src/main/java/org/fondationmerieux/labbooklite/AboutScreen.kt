package org.fondationmerieux.labbooklite

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.layout.ContentScale

/**
 * Created by AlC on 10/04/2025.
 */
@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current
    val appVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            "?"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Fondation Mérieux logo
        Image(
            painter = painterResource(id = R.drawable.fmx),
            contentDescription = "Logo Fondation Mérieux",
            modifier = Modifier.size(160.dp),
            contentScale = ContentScale.Fit
        )

        // LabBook Lite description
        Text(
            text = "LabBookLite permet d'étendre l'usage de LabBook dans le laboratoire aux agents communautaires sur le terrain.",
            modifier = Modifier.padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )

        // AEGLE logo
        Image(
            painter = painterResource(id = R.drawable.aegle),
            contentDescription = "Logo Aegle",
            modifier = Modifier.size(140.dp),
            contentScale = ContentScale.Fit
        )

        // AEGLE description
        Text(
            text = "La société AEGLE développe cette application depuis 2025",
            modifier = Modifier.padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )

        // App version footer
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Version $appVersion",
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}