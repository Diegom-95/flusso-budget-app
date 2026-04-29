package com.diego.budgetmensile

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.diego.budgetmensile.worker.RecurringNotificationWorker
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.diego.budgetmensile.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    var selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    var selectedYear:  Int = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Applica il tema salvato prima di tutto
        val prefs = getSharedPreferences("flusso_prefs", android.content.Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", true)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        RecurringNotificationWorker.schedule(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, bars.top, 0, 0)
            insets
        }

        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Solo i 5 tab principali sono "top-level" (niente freccia indietro)
        val appBarConfig = AppBarConfiguration(
            setOf(R.id.nav_dashboard, R.id.nav_expenses,
                  R.id.nav_income, R.id.nav_savings, R.id.nav_debt)
        )
        setupActionBarWithNavController(navController, appBarConfig)
        binding.bottomNavigation.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()
}
