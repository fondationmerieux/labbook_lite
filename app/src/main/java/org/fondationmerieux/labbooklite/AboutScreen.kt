import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.ClickableText

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val appVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (_: Exception) {
            "?"
        }
    }

    val annotatedText = buildAnnotatedString {
        append("Cette application a été développée dans le cadre d’un partenariat entre :\n\n")

        append("- L’Unité d’Appui à la Gestion et à la Coordination des Programmes (UAGCP) du Ministère de la Santé et de l’Hygiène Publique, République de Guinée\n")
        append("- ")

        pushStringAnnotation(tag = "URL", annotation = "https://www.fondation-merieux.org/")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
            append("Fondation Mérieux")
        }
        pop()
        append("\n\n")

        append("LabBook Lite permet d’étendre l’usage de LabBook (")
        pushStringAnnotation(tag = "URL", annotation = "https://www.lab-book.org/")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append("https://www.lab-book.org/")
        }
        pop()
        append(") dans les laboratoires jusqu’aux agents communautaires sur le terrain.\n")
        append("Il s’agit d’une application Android, pour mobile et tablette, fonctionnant sans connexion internet.\n\n")

        append("LabBook Lite offre la possibilité de collecter les données des patients, de saisir et de valider les résultats des analyses, puis d’imprimer les comptes rendus.\n")
        append("Une fois l’utilisateur de LabBook Lite de retour au laboratoire, l’application peut être utilisée pour transférer les données vers le serveur LabBook.\n\n")

        append("LabBook Lite est une application développée par ")
        pushStringAnnotation(tag = "URL", annotation = "https://www.aegle.fr/")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
            append("AEGLE")
        }
        pop()
        append(" en 2025.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ClickableText(
            text = annotatedText,
            style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Start),
            onClick = { offset ->
                annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                        context.startActivity(browserIntent)
                    }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Version $appVersion",
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}