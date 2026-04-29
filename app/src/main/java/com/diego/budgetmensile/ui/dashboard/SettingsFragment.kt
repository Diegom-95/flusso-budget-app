package com.diego.budgetmensile.ui.dashboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.diego.budgetmensile.MainActivity
import com.diego.budgetmensile.R
import com.diego.budgetmensile.data.BudgetRepository
import com.diego.budgetmensile.databinding.FragmentSettingsBinding
import com.diego.budgetmensile.ui.LockActivity
import com.diego.budgetmensile.utils.CsvExporter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // Launcher per selezione file CSV da importare
    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> handleImport(uri) }
        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(i, c, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()

        // ── TEMA ──────────────────────────────────────────────────────────────
        val prefs  = ctx.getSharedPreferences("flusso_prefs", Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", true)
        binding.switchTheme.isChecked = isDark
        binding.switchTheme.setOnCheckedChangeListener { _, dark ->
            prefs.edit().putBoolean("dark_mode", dark).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (dark) AppCompatDelegate.MODE_NIGHT_YES
                else      AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // ── PIN ───────────────────────────────────────────────────────────────
        updatePinStatus()
        binding.btnSetPin.setOnClickListener { showSetPinDialog() }

        // ── BIOMETRICO ────────────────────────────────────────────────────────
        val canBio = BiometricManager.from(ctx)
            .canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
        binding.switchBio.isEnabled = canBio && LockActivity.isPinSet(ctx)
        binding.switchBio.isChecked = LockActivity.isBioEnabled(ctx)
        binding.switchBio.setOnCheckedChangeListener { _, checked ->
            if (checked && !LockActivity.isPinSet(ctx)) {
                binding.switchBio.isChecked = false
                Toast.makeText(ctx, "Imposta prima un PIN", Toast.LENGTH_SHORT).show()
            } else {
                LockActivity.enableBio(ctx, checked)
            }
        }

        // ── EXPORT MESE ───────────────────────────────────────────────────────
        binding.btnExportMonth.setOnClickListener {
            val activity = requireActivity() as MainActivity
            val m = activity.selectedMonth; val y = activity.selectedYear
            val repo = BudgetRepository(ctx)
            repo.getExpensesByMonth(m, y).observeOnce(viewLifecycleOwner) { expenses ->
                repo.getIncomesByMonth(m, y).observeOnce(viewLifecycleOwner) { incomes ->
                    repo.getSavingsByMonth(m, y).observeOnce(viewLifecycleOwner) { savings ->
                        CsvExporter.exportMonth(
                            ctx,
                            expenses ?: emptyList(),
                            incomes  ?: emptyList(),
                            savings  ?: emptyList(),
                            m, y
                        )
                    }
                }
            }
        }

        // ── EXPORT COMPLETO ───────────────────────────────────────────────────
        binding.btnExportAll.setOnClickListener {
            lifecycleScope.launch {
                CsvExporter.exportAllSuspend(ctx)
            }
        }

        binding.btnExportDrive.setOnClickListener {
            lifecycleScope.launch {
                CsvExporter.exportAllAndShareToDrive(ctx)
            }
        }

        // ── IMPORT ────────────────────────────────────────────────────────────
        binding.btnImportCsv.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "text/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            importLauncher.launch(Intent.createChooser(intent, "Seleziona CSV Flusso"))
        }
    }

    private fun handleImport(uri: Uri) {
        try {
            val content = requireContext().contentResolver
                .openInputStream(uri)?.bufferedReader()?.readText() ?: return

            val result = kotlinx.coroutines.runBlocking { CsvExporter.parseCsvDedup(requireContext(), content) }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("📥 Conferma importazione")
                .setMessage(buildString {
                        appendLine("Trovati:")
                        appendLine("• ${result.expenses.size} spese")
                        appendLine("• ${result.incomes.size} entrate")
                        appendLine("• ${result.savings.size} risparmi")
                        appendLine("• ${result.debts.size} debiti")
                        if (result.debtGoals.isNotEmpty()) appendLine("• ${result.debtGoals.size} obiettivi debito")
                        if (result.budgetGoals.isNotEmpty()) appendLine("• ${result.budgetGoals.size} budget categorie")
                        if (result.skipped > 0) appendLine("\n🔁 ${result.skipped} duplicati ignorati")
                        if (result.errors  > 0) appendLine("⚠️ ${result.errors} righe non valide")
                        append("\nVuoi procedere?")
                    }
                )
                .setPositiveButton("Importa") { _, _ ->
                    val repo = BudgetRepository(requireContext())
                    lifecycleScope.launch {
                        result.expenses.forEach    { repo.insertExpense(it) }
                        result.incomes.forEach     { repo.insertIncome(it) }
                        result.savings.forEach     { repo.insertSaving(it) }
                        result.debts.forEach       { repo.insertDebt(it) }
                        result.debtGoals.forEach   { repo.insertDebtGoal(it) }
                        result.budgetGoals.forEach { repo.upsertGoal(it) }
                        Toast.makeText(
                            requireContext(),
                            "✅ Importati ${result.total} movimenti",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .setNegativeButton("Annulla", null).show()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Errore lettura file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updatePinStatus() {
        binding.tvPinStatus.text = if (LockActivity.isPinSet(requireContext()))
            "✅ PIN impostato" else "Non impostato"
    }

    private fun showSetPinDialog() {
        val v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_set_pin, null)
        val etNew     = v.findViewById<TextInputEditText>(R.id.etNewPin)
        val etConfirm = v.findViewById<TextInputEditText>(R.id.etConfirmPin)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Imposta PIN")
            .setView(v)
            .setPositiveButton("Salva") { _, _ ->
                val pin = etNew.text.toString()
                if (pin.length < 4) {
                    Toast.makeText(requireContext(), "PIN di almeno 4 cifre", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (pin != etConfirm.text.toString()) {
                    Toast.makeText(requireContext(), "I PIN non coincidono", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                LockActivity.savePin(requireContext(), pin)
                updatePinStatus()
                binding.switchBio.isEnabled = true
                Toast.makeText(requireContext(), "PIN salvato!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annulla", null).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

fun <T> androidx.lifecycle.LiveData<T>.observeOnce(
    owner: androidx.lifecycle.LifecycleOwner,
    observer: (T?) -> Unit
) {
    observe(owner, object : androidx.lifecycle.Observer<T> {
        override fun onChanged(value: T) { observer(value); removeObserver(this) }
    })
}
