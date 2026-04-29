package com.diego.budgetmensile.ui.expenses

import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ListAdapter
import com.diego.budgetmensile.MainActivity
import com.diego.budgetmensile.R
import com.diego.budgetmensile.data.BudgetRepository
import com.diego.budgetmensile.data.entity.BudgetGoal
import com.diego.budgetmensile.databinding.FragmentBudgetBinding
import com.diego.budgetmensile.databinding.ItemBudgetCategoryBinding
import com.diego.budgetmensile.utils.CategoryManager
import com.diego.budgetmensile.utils.Constants
import com.diego.budgetmensile.utils.toEuro
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

// ── Data class per la view ─────────────────────────────────────────────────────
data class BudgetRow(
    val goal: BudgetGoal,
    val spent: Double
)

// ── ViewModel ─────────────────────────────────────────────────────────────────
class BudgetViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = BudgetRepository(app)
    private val _month = MutableLiveData<Int>()
    private val _year  = MutableLiveData<Int>()

    val budgetGoals = _month.switchMap { m ->
        _year.switchMap { y -> repo.getGoalsByTypeAndMonth("expense", m, y) }
    }
    val expenses = _month.switchMap { m ->
        _year.switchMap { y -> repo.getExpensesByMonth(m, y) }
    }

    fun setMonthYear(m: Int, y: Int) { _month.value = m; _year.value = y }
    fun upsert(g: BudgetGoal) = viewModelScope.launch { repo.upsertGoal(g) }
    fun delete(g: BudgetGoal) = viewModelScope.launch { repo.deleteGoal(g) }

    val month get() = _month.value ?: 1
    val year  get() = _year.value  ?: 2024
}

// ── Adapter ───────────────────────────────────────────────────────────────────
class BudgetAdapter(
    private val onEdit:   (BudgetRow) -> Unit,
    private val onDelete: (BudgetRow) -> Unit
) : ListAdapter<BudgetRow, BudgetAdapter.VH>(DiffCb()) {

    inner class VH(private val b: ItemBudgetCategoryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(row: BudgetRow) {
            val goal  = row.goal
            val spent = row.spent
            val limit = goal.goal
            val pct   = if (limit > 0) (spent / limit * 100).toInt().coerceIn(0, 150) else 0
            val remaining = limit - spent

            b.tvBudgetCategory.text = goal.category
            b.tvSpent.text  = spent.toEuro()
            b.tvLimit.text  = limit.toEuro()
            b.tvBudgetPct.text = "$pct% utilizzato"
            b.progressBudget.progress = pct.coerceIn(0, 100)

            // Colore dinamico: verde < 80%, arancione 80–100%, rosso > 100%
            val color = when {
                pct >= 100 -> Color.parseColor("#FF4081")  // rosso — superato
                pct >= 80  -> Color.parseColor("#FFB300")  // arancione — attenzione
                else       -> Color.parseColor("#00E5A0")  // verde — ok
            }
            b.progressBudget.progressTintList =
                android.content.res.ColorStateList.valueOf(color)
            b.tvBudgetPct.setTextColor(color)

            // Alert visivo
            when {
                pct >= 100 -> {
                    b.tvBudgetAlert.visibility = View.VISIBLE
                    b.tvBudgetAlert.text = "🚨 Budget superato!"
                    b.tvBudgetAlert.setTextColor(Color.parseColor("#FF4081"))
                }
                pct >= 80 -> {
                    b.tvBudgetAlert.visibility = View.VISIBLE
                    b.tvBudgetAlert.text = "⚠️ Stai per raggiungere il limite"
                    b.tvBudgetAlert.setTextColor(Color.parseColor("#FFB300"))
                }
                else -> b.tvBudgetAlert.visibility = View.GONE
            }

            // Rimanente
            if (remaining >= 0) {
                b.tvRemaining.text = "Rimangono ${remaining.toEuro()}"
                b.tvRemaining.setTextColor(Color.parseColor("#00E5A0"))
            } else {
                b.tvRemaining.text = "Sforato di ${(-remaining).toEuro()}"
                b.tvRemaining.setTextColor(Color.parseColor("#FF4081"))
            }

            b.btnEditBudget.setOnClickListener   { onEdit(row) }
            b.btnDeleteBudget.setOnClickListener { onDelete(row) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemBudgetCategoryBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    class DiffCb : DiffUtil.ItemCallback<BudgetRow>() {
        override fun areItemsTheSame(a: BudgetRow, b: BudgetRow) =
            a.goal.category == b.goal.category
        override fun areContentsTheSame(a: BudgetRow, b: BudgetRow) = a == b
    }
}

// ── Fragment ──────────────────────────────────────────────────────────────────
class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var adapter: BudgetAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentBudgetBinding.inflate(i, c, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity

        adapter = BudgetAdapter(onEdit = { showDialog(it.goal) }, onDelete = { confirmDelete(it.goal) })
        binding.rvBudget.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBudget.adapter = adapter

        viewModel.setMonthYear(activity.selectedMonth, activity.selectedYear)

        val monthName = Constants.MONTHS_SHORT.getOrElse(activity.selectedMonth - 1) { "" }
        binding.tvBudgetMonth.text = "$monthName ${activity.selectedYear}"

        // Combina goals + spese per costruire le BudgetRow
        viewModel.budgetGoals.observe(viewLifecycleOwner) { updateRows() }
        viewModel.expenses.observe(viewLifecycleOwner)    { updateRows() }

        binding.fabAddBudget.setOnClickListener { showDialog(null) }
    }

    private fun updateRows() {
        val goals   = viewModel.budgetGoals.value ?: return
        val expenses = viewModel.expenses.value   ?: emptyList()

        val rows = goals.map { goal ->
            val spent = expenses.filter { it.category == goal.category }.sumOf { it.amount }
            BudgetRow(goal, spent)
        }

        adapter.submitList(rows)
        binding.tvEmptyBudget.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE

        // KPI totale
        val totalLimit = rows.sumOf { it.goal.goal }
        val totalSpent = rows.sumOf { it.spent }
        val totalPct   = if (totalLimit > 0) (totalSpent / totalLimit * 100).toInt().coerceIn(0, 100) else 0

        binding.tvBudgetTotalSpent.text = totalSpent.toEuro()
        binding.tvBudgetTotalLimit.text = "su ${totalLimit.toEuro()} di budget"
        binding.tvTotalPct.text = "$totalPct%"
        binding.progressTotal.progress = totalPct

        val totalColor = when {
            totalPct >= 100 -> android.graphics.Color.parseColor("#FF4081")
            totalPct >= 80  -> android.graphics.Color.parseColor("#FFB300")
            else            -> android.graphics.Color.parseColor("#00E5A0")
        }
        binding.progressTotal.progressTintList =
            android.content.res.ColorStateList.valueOf(totalColor)
    }

    private fun showDialog(existing: BudgetGoal?) {
        val v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_set_budget, null)
        val spinner = v.findViewById<AutoCompleteTextView>(R.id.spinnerBudgetCategory)
        val etLimit = v.findViewById<TextInputEditText>(R.id.etBudgetLimit)

        val cats = CategoryManager.getCategories(requireContext(), "expense")
        spinner.setAdapter(ArrayAdapter(requireContext(),
            android.R.layout.simple_dropdown_item_1line, cats))

        var selectedCat = existing?.category ?: cats.firstOrNull() ?: ""
        spinner.setText(selectedCat, false)
        spinner.setOnItemClickListener { _, _, pos, _ -> selectedCat = cats[pos] }

        existing?.let { etLimit.setText(it.goal.toString()) }

        // Se stiamo modificando, blocca la categoria
        if (existing != null) spinner.isEnabled = false

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing != null) "Modifica Budget" else "Nuovo Budget")
            .setView(v)
            .setPositiveButton("Salva") { _, _ ->
                val limit = etLimit.text.toString().toDoubleOrNull() ?: return@setPositiveButton
                viewModel.upsert(BudgetGoal(
                    category = selectedCat, type = "expense", goal = limit,
                    month = viewModel.month, year = viewModel.year
                ))
            }
            .setNegativeButton("Annulla", null).show()
    }

    private fun confirmDelete(g: BudgetGoal) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Rimuovi budget")
            .setMessage("Rimuovere il limite per \"${g.category}\"?")
            .setPositiveButton("Rimuovi") { _, _ -> viewModel.delete(g) }
            .setNegativeButton("Annulla", null).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
