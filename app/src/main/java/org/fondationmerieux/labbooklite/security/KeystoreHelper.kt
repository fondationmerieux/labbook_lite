package org.fondationmerieux.labbooklite.security

/**
 * Created by AlC on 19/03/2025.
 */
import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * Helper class to securely store and retrieve the database password using Android Keystore.
 */
object KeystoreHelper {

    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_PASSWORD = "db_password"

    /**
     * Generates and stores a secure password if not already set.
     * @param context The application context.
     */
    fun getOrCreatePassword(context: Context): String {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val sharedPreferences = EncryptedSharedPreferences.create(
            PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var password = sharedPreferences.getString(KEY_PASSWORD, null)

        if (password == null) {
            // Generate a new secure password
            password = generateSecurePassword()
            sharedPreferences.edit().putString(KEY_PASSWORD, password).apply()
        }

        return password
    }

    /**
     * Generates a secure random password.
     */
    private fun generateSecurePassword(): String {
        val randomBytes = ByteArray(16)
        java.security.SecureRandom().nextBytes(randomBytes)
        return Base64.encodeToString(randomBytes, Base64.NO_WRAP)
    }
}