package org.fondationmerieux.labbooklite

/**
 * Created by AlC on 01/04/2025.
 */
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Get references to UI components
        val usernameInput = findViewById<EditText>(R.id.input_username)
        val passwordInput = findViewById<EditText>(R.id.input_password)
        val loginButton = findViewById<Button>(R.id.btn_login)
        val versionText = findViewById<TextView>(R.id.version_text)

        // Set application version dynamically
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        versionText.text = "v$versionName"

        // Handle login button click
        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // TODO: Replace with real authentication logic
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
            } else {
                // Temporary mock login success
                Toast.makeText(this, "Login successful for $username", Toast.LENGTH_SHORT).show()

                // TODO: Navigate to next screen after successful login
            }
        }
    }
}
