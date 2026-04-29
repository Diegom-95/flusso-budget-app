package com.diego.budgetmensile.ui.goals

import android.app.Application
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ListAdapter
import com.diego.budgetmensile.R
import com.diego.budgetmensile.data.BudgetRepository
import com.diego.budgetmensile.data.entity.GoalWithdrawal
import com.diego.budgetmensile.data.entity.SavingGoal
import com.diego.budgetmensile.databinding.FragmentGoalsBinding
import com.diego.budgetmensile.databinding.ItemGoalBinding
import com.diego.budgetmensile.utils.CategoryManager
import com.diego.budgetmensile.utils.toDisplayDate
import com.diego.budgetmensile.utils.toEuro
import com.diego.budgetmensile.utils.todayString
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Calendar

// ── ViewModel ─────────────────────────────────────────────────────────────────
class GoalsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = BudgetRepository(app)
    val goals = repo.getAllSavingGoals()
    fun insert(g: SavingGoal)       = viewModelScope.launch { repo.insertSavingGoal(g) }
    fun update(g: SavingGoal)       = viewModelScope.launch { repo.updateSavingGoal(g) }
    fun delete(g: SavingGoal)       = viewModelScope.launch { repo.deleteSavingGoal(g) }
    fun getTotalSaved(cat: String)  = repo.getTotalSavedForCategory(cat)
    fun getWithdrawals(goalId: Int) = repo.getWithdrawalsByGoal(goalId)
    fun getTotalWithdrawn(goalId: Int) = repo.getTotalWithdrawnForGoal(goalId)
    fun insertWithdrawal(w: GoalWithdrawal) = viewModelScope.launch { repo.insertWithdrawal(w) }
    fun deleteWithdrawal(w: GoalWithdrawal) = viewModelScope.launch { repo.deleteWithdrawal(w) }
}

// ── Adapter ───────────────────────────────────────────────────────────────────
class GoalAdapter(
    private val onDelete:   (SavingGoal) -> Unit,
    private val onEdit:     (SavingGoal) -> Unit,
    private val onWithdraw: (SavingGoal) -> Unit,
    private val vm: GoalsViewModel,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<SavingGoal, GoalAdapter.VH>(DiffCb()) {

    inner class VH(private val b: ItemGoalBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(goal: SavingGoal) {
            b.tvGoalName.text     = goal.name
            b.tvGoalCategory.text = goal.category
            b.tvDeadline.text     = if (goal.deadline.isNotBlank())
                "Scadenza: ${goal.deadline.toDisplayDate()}" else ""

            // Osserva risparmiato e prelevato in combinata
            val savedLd     = vm.getTotalSaved(goal.category)
            val withdrawnLd = vm.getTotalWithdrawn(goal.id)

            var totalSaved     = 0.0
            var totalWithdrawn = 0.0

            fun refresh() {
                val available = (totalSaved - totalWithdrawn).coerceAtLeast(0.0)
                val pct = if (goal.targetAmount > 0)
                    (available / goal.targetAmount * 100).toInt().coerceIn(0, 100) else 0

                b.tvSaved.text     = totalSaved.toEuro()
                b.tvWithdrawn.text = "-${totalWithdrawn.toEuro()}"
                b.tvAvailable.text = available.toEuro()
                b.tvTarget.text    = goal.targetAmount.toEuro()
                b.progressGoal.progress = pct
                b.tvPct.text = "$pct%"

                if (pct >= 100) {
                    b.tvGoalStatus.text = "✅ Raggiunto"
                    b.tvGoalStatus.setBackgroundColor(android.graphics.Color.parseColor("#1A00E5A0"))
                    b.tvGoalStatus.setTextColor(android.graphics.Color.parseColor("#00E5A0"))
                } else {
                    b.tvGoalStatus.text = "🎯 In corso"
                    b.tvGoalStatus.setBackgroundColor(android.graphics.Color.parseColor("#1AFFD600"))
                    b.tvGoalStatus.setTextColor(android.graphics.Color.parseColor("#FFD600"))
                }
            }

            savedLd.observe(lifecycleOwner)     { totalSaved = it ?: 0.0; refresh() }
            withdrawnLd.observe(lifecycleOwner) { totalWithdrawn = it ?: 0.0; refresh() }

            // Storico prelievi
            vm.getWithdrawals(goal.id).observe(lifecycleOwner) { list ->
                b.layoutWithdrawals.visibility =
                    if (list.isNullOrEmpty()) View.GONE else View.VISIBLE
                b.containerWithdrawals.removeAllViews()
                list?.forEach { w ->
                    val row = LayoutInflater.from(b.root.context)
                        .inflate(android.R.layout.simple_list_item_2, b.containerWithdrawals, false)
                    row.findViewById<TextView>(android.R.id.text1).apply {
                        text = "${w.description}  –${w.amount.toEuro()}"
                        setTextColor(android.graphics.Color.parseColor("#CCCCCC"))
                        textSize = 13f
                    }
                    row.findViewById<TextView>(android.R.id.text2).apply {
                        text = w.date.toDisplayDate() + if (w.notes.isNotBlank()) "  ·  ${w.notes}" else ""
                        setTextColor(android.graphics.Color.parseColor("#7A90BB"))
                        textSize = 11f
                    }
                    // Long press → elimina prelievo
                    row.setOnLongClickListener {
                        MaterialAlertDialogBuilder(b.root.context)
                            .setTitle("Elimina prelievo")
                            .setMessage("Eliminare \"${w.description}\"?")
                            .setPositiveButton("Elimina") { _, _ -> vm.deleteWithdrawal(w) }
                            .setNegativeButton("Annulla", null).show()
                        true
                    }
                    b.containerWithdrawals.addView(row)
                }
            }

            b.btnWithdraw.setOnClickListener   { onWithdraw(goal) }
            b.btnEditGoal.setOnClickListener    { onEdit(goal) }
            b.btnDeleteGoal.setOnClickListener  { onDelete(goal) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemGoalBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    class DiffCb : DiffUtil.ItemCallback<SavingGoal>() {
        override fun areItemsTheSame(a: SavingGoal, b: SavingGoal) = a.id == b.id
        override fun areContentsTheSame(a: SavingGoal, b: SavingGoal) = a == b
    }
}

// ── Fragment ──────────────────────────────────────────────────────────────────
class GoalsFragment : Fragment() {
    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GoalsViewModel by viewModels()
    private lateinit var adapter: GoalAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentGoalsBinding.inflate(i, c, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = GoalAdapter(
            onDelete   = { confirmDelete(it) },
            onEdit     = { showEditGoalDialog(it) },
            onWithdraw = { showWithdrawDialog(it) },
            vm = viewModel,
            lifecycleOwner = viewLifecycleOwner
        )
        binding.rvGoals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGoals.adapter = adapter

        viewModel.goals.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        binding.fabAddGoal.setOnClickListener { showAddGoalDialog() }
    }

    // ── Dialog nuovo obiettivo ────────────────────────────────────────────────
    private fun showAddGoalDialog() {
        val v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_goal, null)
        val etName     = v.findViewById<TextInputEditText>(R.id.etGoalName)
        val etTarget   = v.findViewById<TextInputEditText>(R.id.etGoalTarget)
        val spinnerCat = v.findViewById<AutoCompleteTextView>(R.id.spinnerGoalCategory)
        val etDeadline = v.findViewById<TextInputEditText>(R.id.etGoalDeadline)
        val etNotes    = v.findViewById<TextInputEditText>(R.id.etGoalNotes)

        val cats = CategoryManager.getCategories(requireContext(), "saving")
        spinnerCat.setAdapter(ArrayAdapter(requireContext(),
            android.R.layout.simple_dropdown_item_1line, cats))
        if (cats.isNotEmpty()) spinnerCat.setText(cats[0], false)
        var selectedCat = cats.firstOrNull() ?: ""
        spinnerCat.setOnItemClickListener { _, _, pos, _ -> selectedCat = cats[pos] }

        etDeadline.setOnClickListener { pickDate { etDeadline.setText(it) } }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nuovo Obiettivo")
            .setView(v)
            .setPositiveButton("Salva") { _, _ ->
                val name   = etName.text.toString().trim().ifEmpty { return@setPositiveButton }
                val target = etTarget.text.toString().toDoubleOrNull() ?: return@setPositiveButton
                viewModel.insert(SavingGoal(
                    name = name, targetAmount = target, category = selectedCat,
                    deadline = etDeadline.text.toString(), notes = etNotes.text.toString()
                ))
            }
            .setNegativeButton("Annulla", null).show()
    }

    // ── Dialog prelievo ───────────────────────────────────────────────────────
    private fun showWithdrawDialog(goal: SavingGoal) {
        val v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_withdraw, null)
        val tvInfo    = v.findViewById<TextView>(R.id.tvWithdrawInfo)
        val etDesc    = v.findViewById<TextInputEditText>(R.id.etWithdrawDesc)
        val etAmount  = v.findViewById<TextInputEditText>(R.id.etWithdrawAmount)
        val etDate    = v.findViewById<TextInputEditText>(R.id.etWithdrawDate)
        val etNotes   = v.findViewById<TextInputEditText>(R.id.etWithdrawNotes)

        tvInfo.text = "Stai prelevando dall'obiettivo \"${goal.name}\".\n" +
            "L'importo verrà scalato dal saldo disponibile."
        etDate.setText(todayString())
        etDate.setOnClickListener { pickDate { etDate.setText(it) } }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("💸 Preleva da \"${goal.name}\"")
            .setView(v)
            .setPositiveButton("Conferma prelievo") { _, _ ->
                val amount = etAmount.text.toString().toDoubleOrNull() ?: return@setPositiveButton
                val desc   = etDesc.text.toString().trim().ifEmpty { "Prelievo" }
                val date   = etDate.text.toString().ifBlank { todayString() }
                viewModel.insertWithdrawal(GoalWithdrawal(
                    goalId = goal.id, goalName = goal.name,
                    amount = amount, description = desc,
                    date = date, notes = etNotes.text.toString()
                ))
            }
            .setNegativeButton("Annulla", null).show()
    }


    // ── Dialog modifica obiettivo ─────────────────────────────────────────────
    private fun showEditGoalDialog(goal: SavingGoal) {
        val v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_goal, null)
        val etName     = v.findViewById<TextInputEditText>(R.id.etGoalName)
        val etTarget   = v.findViewById<TextInputEditText>(R.id.etGoalTarget)
        val spinnerCat = v.findViewById<AutoCompleteTextView>(R.id.spinnerGoalCategory)
        val etDeadline = v.findViewById<TextInputEditText>(R.id.etGoalDeadline)
        val etNotes    = v.findViewById<TextInputEditText>(R.id.etGoalNotes)

        // Pre-compila con i valori esistenti
        etName.setText(goal.name)
        etTarget.setText(goal.targetAmount.toString())
        etDeadline.setText(goal.deadline)
        etNotes.setText(goal.notes)

        val cats = CategoryManager.getCategories(requireContext(), "saving")
        spinnerCat.setAdapter(ArrayAdapter(requireContext(),
            android.R.layout.simple_dropdown_item_1line, cats))
        spinnerCat.setText(goal.category, false)
        var selectedCat = goal.category
        spinnerCat.setOnItemClickListener { _, _, pos, _ -> selectedCat = cats[pos] }

        etDeadline.setOnClickListener { pickDate { etDeadline.setText(it) } }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Modifica Obiettivo")
            .setView(v)
            .setPositiveButton("Salva modifiche") { _, _ ->
                val name   = etName.text.toString().trim().ifEmpty { return@setPositiveButton }
                val target = etTarget.text.toString().toDoubleOrNull() ?: return@setPositiveButton
                viewModel.update(goal.copy(
                    name = name, targetAmount = target, category = selectedCat,
                    deadline = etDeadline.text.toString(), notes = etNotes.text.toString()
                ))
            }
            .setNegativeButton("Annulla", null).show()
    }

    private fun confirmDelete(g: SavingGoal) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Elimina obiettivo")
            .setMessage("Eliminare \"${g.name}\"? Anche i prelievi associati verranno rimossi.")
            .setPositiveButton("Elimina") { _, _ -> viewModel.delete(g) }
            .setNegativeButton("Annulla", null).show()
    }

    private fun pickDate(onPicked: (String) -> Unit) {
        val c = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            onPicked("%04d-%02d-%02d".format(y, m + 1, d))
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
