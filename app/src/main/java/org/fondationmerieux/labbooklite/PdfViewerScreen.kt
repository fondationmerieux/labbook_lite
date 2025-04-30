package org.fondationmerieux.labbooklite

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.io.File
import androidx.core.graphics.createBitmap
import kotlin.math.min


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(filePath: String, navController: NavController) {
    val context = LocalContext.current
    val file = File(filePath)

    var scale by remember { mutableFloatStateOf(1.5f) }
    var isLoading by remember { mutableStateOf(false) }

    // ✅ Liste régénérée à chaque zoom
    var pageBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    val screenWidth = remember {
        context.resources.displayMetrics.widthPixels
    }

    LaunchedEffect(scale, filePath) {
        if (!file.exists()) return@LaunchedEffect

        isLoading = true
        pageBitmaps = emptyList()

        try {
            val bitmaps = mutableListOf<Bitmap>()
            val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            PdfRenderer(descriptor).use { renderer ->
                repeat(renderer.pageCount) { index ->
                    val page = renderer.openPage(index)

                    val targetWidth = min((screenWidth * scale).toInt(), 2000)
                    val targetHeight = (targetWidth * page.height) / page.width

                    val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    bitmaps.add(bitmap)
                }
            }
            pageBitmaps = bitmaps
        } catch (e: Exception) {
            Log.e("PdfViewerScreen", "Erreur PDF", e)
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zoom x${"%.1f".format(scale)}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (scale < 3.0f) scale += 0.25f
                    }) { Text("+") }

                    IconButton(onClick = {
                        if (scale > 0.5f) scale -= 0.25f
                    }) { Text("-") }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            pageBitmaps.isEmpty() -> Box(Modifier.fillMaxSize()) {
                Text("Fichier introuvable ou vide", Modifier.align(Alignment.Center))
            }

            else -> LazyColumn(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pageBitmaps, key = { it.hashCode() }) { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
