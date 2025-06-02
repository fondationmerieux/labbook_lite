package org.fondationmerieux.labbooklite

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController

@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current
    val appVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (_: Exception) {
            "?"
        }
    }

    val annotatedText = buildAnnotatedString {
        append("Cette application a été développée suite au partenariat entre :\n\n")

        append("• La Direction Nationale des laboratoires du Ministère de la Santé et de l’Hygiène Publique, République de Guinée\n")
        append("• L’Unité d’Appui à la Gestion et à la Coordination des Programmes (UAGCP) du Ministère de la Santé et de l’Hygiène Publique, République de Guinée\n")
        append("• Le Fonds mondial de lutte contre le sida, la tuberculose et le paludisme\n")
        append("• ")

        pushStringAnnotation(tag = "URL", annotation = "https://www.fondation-merieux.org/")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
            append("La Fondation Mérieux")
        }
        pop()

        append("\n\nLabBook Lite permet d'étendre l'usage de LabBook (")
        pushStringAnnotation(tag = "URL", annotation = "https://www.lab-book.org/")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
            append("https://www.lab-book.org/")
        }
        pop()
        append(") dans le laboratoire aux agents communautaires sur le terrain.\n")
        append("Il s'agit d'une application Android pour mobile et tablette qui fonctionne sans connexion internet.\n\n")

        append("LabBook Lite offre la possibilité de collecter des données des patients, de saisir les résultats des analyses, de les valider et donc d'imprimer les comptes-rendus.\n")
        append("Une fois l'utilisateur de LabBook Lite de retour au laboratoire, l'application peut être utilisée pour transférer les données vers le serveur LabBook.\n\n")

        append("LabBook Lite a été développé par ")
        pushStringAnnotation(tag = "URL", annotation = "https://www.aegle.fr/")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
            append("AEGLE")
        }
        pop()
        append(" en 2025.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

        Text(
            text = annotatedText,
            style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Start),
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        layoutResult.value?.let { layout ->
                            val position = layout.getOffsetForPosition(tapOffset)
                            annotatedText.getStringAnnotations(tag = "URL", start = position, end = position)
                                .firstOrNull()?.let { annotation ->
                                    val intent = Intent(Intent.ACTION_VIEW, annotation.item.toUri())
                                    context.startActivity(intent)
                                }
                        }
                    }
                },
            onTextLayout = { layoutResult.value = it }
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = {
               navController.popBackStack()
            }) {
                Text("Retour")
            }

            Text(
                text = "Version $appVersion",
                style = MaterialTheme.typography.labelSmall
            )
        }

    }
}