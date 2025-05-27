package org.fondationmerieux.labbooklite

/**
 * Created by AlC on 01/04/2025.
 */
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.core.content.edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(navController: NavController, showMenu: Boolean = true) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)

    val firstname = prefs.getString("firstname", null)
    val lastname = prefs.getString("lastname", null)
    val username = prefs.getString("username", "") ?: ""
    val role = prefs.getString("role_type", "") ?: ""
    val isLoggedIn = prefs.getBoolean("logged_in", false)

    val userLabel = if (!lastname.isNullOrEmpty() || !firstname.isNullOrEmpty()) {
        listOfNotNull(lastname, firstname).joinToString(" ")
    } else {
        username
    }

    var menuExpanded = remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.labbook_lite_logo_long),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .height(32.dp)

                        .clickable {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(text = userLabel)
            }
        },
        actions = {
            if (showMenu) {
                IconButton(onClick = { menuExpanded.value = true }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",

                        )
                }

                DropdownMenu(
                    expanded = menuExpanded.value,
                    onDismissRequest = { menuExpanded.value = false }
                ) {
                    if (role == "A") {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.configuration)) },
                            onClick = {
                                menuExpanded.value = false
                                navController.navigate("settings")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.pr_f_rences)) },
                            onClick = {
                                menuExpanded.value = false
                                navController.navigate("preferences")
                            }
                        )
                        HorizontalDivider()
                    }

                    if (role == "AGT") {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.nouveau_dossier)) },
                            onClick = {
                                menuExpanded.value = false
                                navController.navigate("new_record")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.liste_des_dossiers)) },
                            onClick = {
                                menuExpanded.value = false
                                navController.navigate("record_list")
                            }
                        )
                        HorizontalDivider()
                    }

                    // Common menu entries (shown for all roles)
                    DropdownMenuItem(
                        text = { Text("Général") },
                        onClick = {
                            menuExpanded.value = false
                            navController.navigate("general")
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("À propos") },
                        onClick = {
                            menuExpanded.value = false
                            navController.navigate("about")
                        }
                    )

                    HorizontalDivider()

                    if (isLoggedIn) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.deconnexion)) },
                            onClick = {
                                menuExpanded.value = false
                                prefs.edit {
                                    remove("username")
                                    remove("pat_id")
                                    remove("firstname")
                                    remove("lastname")
                                    remove("role_type")
                                    putBoolean("logged_in", false)
                                }
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}
