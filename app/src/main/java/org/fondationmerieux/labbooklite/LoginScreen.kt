package org.fondationmerieux.labbooklite

/**
 * Created by AlC on 01/04/2025.
 */
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.navigation.NavController
import org.fondationmerieux.labbooklite.database.LabBookLiteDatabase
import org.fondationmerieux.labbooklite.databinding.ActivityLoginBinding
import org.fondationmerieux.labbooklite.security.getPasswordDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import android.content.Context

@SuppressLint("SetTextI18n")
@Composable
fun LoginScreen(database: LabBookLiteDatabase, navController: NavController) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)

    AndroidViewBinding(ActivityLoginBinding::inflate) {
        val versionName = context.packageManager
            .getPackageInfo(context.packageName, 0).versionName
        versionText.text = "v$versionName"

        btnLogin.setOnClickListener {
            val username = inputUsername.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    context,
                    context.getString(R.string.veuillez_saisir_identifiant_et_mot_de_passe),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            errorText.text = ""
            errorText.visibility = android.view.View.GONE

            CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
                val userDao = database.userDao()
                val user = withContext(Dispatchers.IO) {
                    userDao.getUserByUsername(username)
                }

                if (user != null) {
                    user.password?.let { storedHash ->
                        val computedHash = getPasswordDB(password, storedHash)
                        if (computedHash == user.password) {
                            with(sharedPrefs.edit()) {
                                // Store session information after successful login
                                putString("username", user.username)
                                putInt("pat_id", user.id_data)
                                putString("role_type", user.role_type)
                                putString("firstname", user.firstname)
                                putString("lastname", user.lastname)
                                putInt("user_id", user.id_data)
                                putBoolean("logged_in", true) // Flag used to restore session on next app launch
                                apply()
                            }
                            Toast.makeText(context,
                                context.getString(R.string.connexion_reussie), Toast.LENGTH_SHORT).show()
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            errorText.text = context.getString(R.string.mot_de_passe_incorrect)
                            errorText.visibility = android.view.View.VISIBLE
                        }
                    } ?: run {
                        errorText.text =
                            context.getString(R.string.utilisateur_mal_configure_pas_de_mot_de_passe)
                        errorText.visibility = android.view.View.VISIBLE
                    }
                } else {
                    errorText.text = context.getString(R.string.utilisateur_inconnu)
                    errorText.visibility = android.view.View.VISIBLE
                }
            }
        }
    }
}

