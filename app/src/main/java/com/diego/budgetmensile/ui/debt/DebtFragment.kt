package com.diego.budgetmensile.ui.debt

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ListAdapter
import com.diego.budgetmensile.R
import com.diego.budgetmensile.data.entity.Debt
import com.diego.budgetmensile.data.entity.DebtGoal
import com.diego.budgetmensile.databinding.FragmentDebtBinding
import com.diego.budgetmensile.utils.toEuro
import com.diego.budgetmensile.utils.setToolbarMonthTitle
import com.diego.budgetmensile.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

class DebtFragment : Fragment() {

    private var _binding: FragmentDebtBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DebtViewModel by viewModels()
    private lateinit var adapter: DebtGoalAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentDebtBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DebtGoalAdapter(
            onPay    = { goal -> showPaymentDialog(goal) },
            onEdit   = { goal -> showAddEditDialog(goal) },
            onDelete = { goal -> confirmDelete(goal) }
        )
        binding.rvDebtGoals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDebtGoals.adapter = adapter

        viewModel.debtGoals.observe(viewLifecycleOwner) { goals ->
            adapter.submitGoals(goals, viewModel.paidMap)
            binding.tvEmpty.visibility = if (goals.isEmpty()) View.VISIBLE else View.GONE
            // KPI totali
            val totalDebt  = goals.sumOf { it.totalAmount }
            val totalPaid  = viewModel.paidMap.values.sum()
            val totalLeft  = (totalDebt - totalPaid).coerceAtLeast(0.0)
            val totalMonthly = goals.filter { it.isActive }.sumOf { it.monthlyPayment }
            binding.tvTotaleDebito.text   = totalDebt.toEuro()
            binding.tvTotalePagato.text   = totalPaid.toEuro()
            binding.tvTotaleRimanente.text = totalLeft.toEuro()
            binding.tvRataMensile.text    = totalMonthly.toEuro()
        }

        viewModel.refreshPaid()

        binding.fabAddDebt.setOnClickListener { showAddEditDialog(null) }
    }

    private fun showAddEditDialog(existing: DebtGoal?) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_debt_goal, null)

        val etName    = dialogView.findViewById<EditText>(R.id.etDebtName)
        val etTotal   = dialogView.findViewById<EditText>(R.id.etDebtTotal)
        val etMonthly = dialogView.findViewById<EditText>(R.id.etDebtMonthly)
        val tvDate    = dialogView.findViewById<TextView>(R.id.tvDebtStartDate)
        val etNotes   = dialogView.findViewById<EditText>(R.id.etDebtNotes)
        val tvDuration = dialogView.findViewById<TextView>(R.id.tvEstimatedDuration)

        val cal = Calendar.getInstance()
        var selectedDate = existing?.startDate ?: "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH))
        tvDate.text = selectedDate

        existing?.let {
            etName.setText(it.name)
            etTotal.setText(it.totalAmount.toString())
            etMonthly.setText(it.monthlyPayment.toString())
            etNotes.setText(it.notes)
        }

        // Calcola durata stimata dinamicamente
        val updateDuration = {
            val tot = etTotal.text.toString().toDoubleOrNull() ?: 0.0
            val rat = etMonthly.text.toString().toDoubleOrNull() ?: 0.0
            tvDuration.text = if (tot > 0 && rat > 0) {
                val months = Math.ceil(tot / rat).toInt()
                val y = months / 12; val m = months % 12
                "Durata stimata: " + if (y > 0) "$y anni e $m mesi" else "$m mesi"
            } else "Durata stimata: —"
        }
        etTotal.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) = updateDuration()
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) = Unit
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) = Unit
        })
        etMonthly.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) = updateDuration()
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) = Unit
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) = Unit
        })

        tvDate.setOnClickListener {
            val parts = selectedDate.split("-")
            DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedDate = "%04d-%02d-%02d".format(y, m+1, d)
                tvDate.text = selectedDate
            }, parts[0].toInt(), parts[1].toInt()-1, parts[2].toInt()).show()
        }

        val title = if (existing == null) "Nuovo Obiettivo Debito" else "Modifica Debito"
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Salva") { _, _ ->
                val name    = etName.text.toString().trim()
                val total   = etTotal.text.toString().toDoubleOrNull() ?: 0.0
                val monthly = etMonthly.text.toString().toDoubleOrNull() ?: 0.0
                val notes   = etNotes.text.toString().trim()
                if (name.isBlank() || total <= 0 || monthly <= 0) {
                    Toast.makeText(requireContext(), "Compila tutti i campi obbligatori", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val goal = DebtGoal(
                    id = existing?.id ?: 0,
                    name = name, totalAmount = total,
                    monthlyPayment = monthly, startDate = selectedDate,
                    notes = notes, isActive = existing?.isActive ?: true
                )
                if (existing == null) viewModel.addGoal(goal) else viewModel.updateGoal(goal)
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun showPaymentDialog(goal: DebtGoal) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_debt_payment, null)
        val etAmount  = dialogView.findViewById<EditText>(R.id.etPaymentAmount)
        val tvDate    = dialogView.findViewById<TextView>(R.id.tvPaymentDate)

        val cal = Calendar.getInstance()
        var selectedDate = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH))
        tvDate.text = selectedDate
        etAmount.setText(goal.monthlyPayment.toString())

        tvDate.setOnClickListener {
            val parts = selectedDate.split("-")
            DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedDate = "%04d-%02d-%02d".format(y, m+1, d)
                tvDate.text = selectedDate
            }, parts[0].toInt(), parts[1].toInt()-1, parts[2].toInt()).show()
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Registra pagamento — ${goal.name}")
            .setView(dialogView)
            .setPositiveButton("Registra") { _, _ ->
                val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
                if (amount <= 0) return@setPositiveButton
                val parts = selectedDate.split("-")
                val payment = Debt(
                    date = selectedDate,
                    description = "Rata ${goal.name}",
                    category = goal.name,
                    amount = amount,
                    month = parts[1].toInt(),
                    year  = parts[0].toInt()
                )
                viewModel.recordPayment(payment, goal)
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun confirmDelete(goal: DebtGoal) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Elimina debito")
            .setMessage("Eliminare \"${goal.name}\"? I pagamenti già registrati rimarranno.")
            .setPositiveButton("Elimina") { _, _ -> viewModel.deleteGoal(goal) }
            .setNegativeButton("Annulla", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }

    override fun onResume() {
        super.onResume()
        val activity = requireActivity() as MainActivity
        setToolbarMonthTitle(activity.selectedMonth, activity.selectedYear)
    }
}
