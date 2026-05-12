package com.badaboomi.acustomgpt

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.result.contract.ActivityResultContracts

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import com.badaboomi.acustomgpt.domain.repository.SettingsRepository
import com.badaboomi.acustomgpt.presentation.navigation.AppNavigation
import com.badaboomi.acustomgpt.presentation.ui.theme.ACustomGPTTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            val email = account?.email
            if (!email.isNullOrBlank()) {
                userRepository.saveUserEmail(email)
                recreate() // UI neu laden
            }
        } catch (e: Exception) {
            // Fehlerhandling: ggf. Hinweis anzeigen
        }
    }


    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var userRepository: com.badaboomi.acustomgpt.domain.repository.UserRepository

    private var permissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.GET_ACCOUNTS), 1001)
        } else {
            saveGoogleMailIfNeeded()
        }

        setContent {
            ACustomGPTTheme {
                if (permissionGranted) {
                    AppNavigation(settingsRepository = settingsRepository)
                } else {
                    androidx.compose.material3.Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                        androidx.compose.material3.Text(
                            text = "Die App benötigt Zugriff auf das Google-Konto, um fortzufahren. Bitte erlaube die Berechtigung.",
                            modifier = androidx.compose.ui.Modifier.padding(32.dp)
                        )
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true
            saveGoogleMailIfNeeded()
            recreate() // Activity neu starten, damit setContent neu aufgebaut wird
        }
    }

    private fun saveGoogleMailIfNeeded() {
        val email = userRepository.getUserEmail()
        if (com.badaboomi.acustomgpt.tools.ToolConfig.logLevel == "DEBUG") {
            android.util.Log.d("MainActivity", "UserRepository.getUserEmail() liefert: $email")
        }
        if (email.isNullOrBlank()) {
            val googleMail = com.badaboomi.acustomgpt.util.AccountUtils.getGoogleAccountEmail(this)
            if (com.badaboomi.acustomgpt.tools.ToolConfig.logLevel == "DEBUG") {
                android.util.Log.d("MainActivity", "AccountUtils.getGoogleAccountEmail liefert: $googleMail")
            }
            if (!googleMail.isNullOrBlank()) {
                userRepository.saveUserEmail(googleMail)
                if (com.badaboomi.acustomgpt.tools.ToolConfig.logLevel == "DEBUG") {
                    android.util.Log.d("MainActivity", "E-Mail gespeichert: $googleMail")
                }
            } else {
                // Fallback: Google Sign-In
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build()
                googleSignInClient = GoogleSignIn.getClient(this, gso)
                val signInIntent: Intent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }
    }
}
