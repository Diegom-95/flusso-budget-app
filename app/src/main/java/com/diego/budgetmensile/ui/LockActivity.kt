package com.diego.budgetmensile.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.diego.budgetmensile.ui.onboarding.OnboardingActivity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.diego.budgetmensile.MainActivity
import com.diego.budgetmensile.R

class LockActivity : AppCompatActivity() {

    companion object {
        private const val PREFS = "flusso_security"
        private const val KEY_PIN = "app_pin"
        private const val KEY_BIO = "bio_enabled"

        fun isPinSet(ctx: Context) =
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_PIN, null) != null

        fun isBioEnabled(ctx: Context) =
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_BIO, false)

        fun savePin(ctx: Context, pin: String) =
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_PIN, pin).apply()

        fun enableBio(ctx: Context, enabled: Boolean) =
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_BIO, enabled).apply()

        fun getPin(ctx: Context) =
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_PIN, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Prima esecuzione → onboarding
        if (OnboardingActivity.shouldShow(this)) {
            startActivity(android.content.Intent(this, OnboardingActivity::class.java))
            finish(); return
        }
        setContentView(R.layout.activity_lock)

        // Se nessun PIN è configurato, apri direttamente l'app
        if (!isPinSet(this)) { openApp(); return }

        val canUseBio = BiometricManager.from(this)
            .canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS

        if (canUseBio && isBioEnabled(this)) {
            showBiometricPrompt()
        } else {
            showPinInput()
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    openApp()
                }
                override fun onAuthenticationError(code: Int, msg: CharSequence) {
                    // Fallback al PIN
                    showPinInput()
                }
                override fun onAuthenticationFailed() {
                    Toast.makeText(this@LockActivity, "Autenticazione fallita", Toast.LENGTH_SHORT).show()
                }
            })

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Sblocca Flusso")
            .setSubtitle("Usa impronta o PIN dispositivo")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()
        prompt.authenticate(info)
    }

    private fun showPinInput() {
        val pinLayout = findViewById<LinearLayout>(R.id.layoutPin)
        pinLayout.visibility = android.view.View.VISIBLE
        val etPin  = findViewById<EditText>(R.id.etPin)
        val btnOk  = findViewById<Button>(R.id.btnPinOk)
        val tvError = findViewById<TextView>(R.id.tvPinError)

        btnOk.setOnClickListener {
            val entered = etPin.text.toString()
            if (entered == getPin(this)) {
                openApp()
            } else {
                tvError.visibility = android.view.View.VISIBLE
                etPin.text?.clear()
            }
        }
    }

    private fun openApp() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
