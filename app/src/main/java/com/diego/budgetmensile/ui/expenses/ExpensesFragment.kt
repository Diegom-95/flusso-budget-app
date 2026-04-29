package com.diego.budgetmensile.ui.expenses

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ListAdapter
import com.diego.budgetmensile.MainActivity
import com.diego.budgetmensile.R
import com.diego.budgetmensile.data.entity.Expense
import com.diego.budgetmensile.data.entity.RecurringExpense
import com.diego.budgetmensile.databinding.FragmentExpensesBinding
import com.diego.budgetmensile.ui.recurring.RecurringManagerBottomSheet
import com.diego.budgetmensile.utils.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpensesViewModel by viewModels()
    private lateinit var adapter: ExpenseAdapter

    private var allExpenses: List<Expense> = emptyList()
    private var activeCategoryFilter: String? = null
    private var currentSort = SortMode.DATE_DESC
    private var searchQuery: String = ""

    enum class SortMode { DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentExpensesBinding.inflate(i, c, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity

        adapter = ExpenseAdapter(onEdit = { showDialog(it) }, onDelete = { deleteWithUndo(it) })
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = adapter

        SwipeToDeleteCallback.attach(
            recyclerView = binding.rvExpenses,
            rootView     = binding.root,
            getList      = { adapter.currentList },
            itemLabel    = { it.description.ifBlank { it.category } },
            onDelete     = { viewModel.delete(it) }
        )

        viewModel.setMonthYear(activity.selectedMonth, activity.selectedYear)

        viewModel.expenses.observe(viewLifecycleOwner) { list ->
            allExpenses = list ?: emptyList()
            applyFilterAndSort()
            updateCategoryChips()
            updatePieChart()
        }
        viewModel.total.observe(viewLifecycleOwner) { v ->
            binding.tvTotal.text = "Totale: ${(v ?: 0.0).toEuro()}"
        }

        binding.fabAddExpense.setOnClickListener { showDialog(null) }
        binding.btnBudget.setOnClickListener { findNavController().navigate(R.id.action_expenses_to_budget) }
        binding.btnSort.setOnClickListener { showSortMenu() }

        // 🔍 Ricerca
        binding.btnSearch.setOnClickListener {
            val isVisible = binding.searchViewExpenses.visibility == View.VISIBLE
            if (isVisible) {
                binding.searchViewExpenses.visibility = View.GONE
                binding.searchViewExpenses.setQuery("", false)
                searchQuery = ""
                applyFilterAndSort()
            } else {
                binding.searchViewExpenses.visibility = View.VISIBLE
                binding.searchViewExpenses.requestFocus()
            }
        }
        binding.searchViewExpenses.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText?.trim() ?: ""
                applyFilterAndSort()
                return true
            }
        })

        // Bottone ⚙️ Ricorrenti — apre il bottom sheet
        binding.btnExport.setOnClickListener {
            RecurringManagerBottomSheet().show(parentFragmentManager, "recurring")
        }

        setupPieChartTap()
    }

    // ── Filtro + ordinamento ──────────────────────────────────────────────────
    private fun applyFilterAndSort() {
        var list = allExpenses
        if (activeCategoryFilter != null) list = list.filter { it.category == activeCategoryFilter }
        if (searchQuery.isNotEmpty()) {
            val q = searchQuery.lowercase()
            list = list.filter { exp ->
                exp.description.lowercase().contains(q) ||
                exp.category.lowercase().contains(q) ||
                exp.notes.lowercase().contains(q) ||
                exp.amount.toString().contains(q) ||
                exp.date.contains(q)
            }
        }
        list = when (currentSort) {
            SortMode.DATE_DESC   -> list.sortedByDescending { it.date }
            SortMode.DATE_ASC    -> list.sortedBy { it.date }
            SortMode.AMOUNT_DESC -> list.sortedByDescending { it.amount }
            SortMode.AMOUNT_ASC  -> list.sortedBy { it.amount }
        }
        adapter.submitList(list)
        binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateCategoryChips() {
        val group = binding.chipGroupCategories
        group.removeAllViews()
        val cats = allExpenses.map { it.category }.distinct().sorted()
        if (cats.isEmpty()) { group.visibility = View.GONE; return }
        group.visibility = View.VISIBLE

        val allChip = Chip(requireContext()).apply {
            text = "Tutti"; isCheckable = true; isChecked = activeCategoryFilter == null
            styleForCategory("__all__", activeCategoryFilter == null)
            setOnClickListener { activeCategoryFilter = null; updateChipSelection(group, this); applyFilterAndSort() }
        }
        group.addView(allChip)
        cats.forEach { cat ->
            val chip = Chip(requireContext()).apply {
                text = cat; isCheckable = true; isChecked = activeCategoryFilter == cat
                styleForCategory(cat, activeCategoryFilter == cat)
                setOnClickListener {
                    activeCategoryFilter = if (activeCategoryFilter == cat) null else cat
                    updateChipSelection(group, if (activeCategoryFilter == null) allChip else this)
                    applyFilterAndSort(); updatePieChart()
                }
            }
            group.addView(chip)
        }
    }

    private fun updateChipSelection(group: ChipGroup, selected: Chip) {
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as? Chip ?: continue
            chip.isChecked = chip == selected
            val cat = if (chip == selected) chip.text.toString() else chip.text.toString()
            chip.styleForCategory(cat, chip == selected)
        }
    }

    private fun showSortMenu() {
        val opts = arrayOf("Data ↓ (più recenti)", "Data ↑ (più vecchie)", "Importo ↓", "Importo ↑")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Ordina per")
            .setSingleChoiceItems(opts, currentSort.ordinal) { dlg, which ->
                currentSort = SortMode.entries[which]; applyFilterAndSort(); dlg.dismiss()
            }.show()
    }

    // ── Grafico a torta interattivo ───────────────────────────────────────────
    private fun setupPieChartTap() {
        binding.pieChartMiniExpenses.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e == null || h == null) return
                val dataSet = binding.pieChartMiniExpenses.data?.dataSet as? PieDataSet ?: return
                val entry = dataSet.getEntryForIndex(h.x.toInt()) as? PieEntry
                val sliceLabel = entry?.label
                if (sliceLabel != null) {
                    activeCategoryFilter = if (activeCategoryFilter == sliceLabel) null else sliceLabel
                    updateCategoryChips(); applyFilterAndSort()
                }
            }
            override fun onNothingSelected() {
                activeCategoryFilter = null; updateCategoryChips(); applyFilterAndSort()
            }
        })
    }

    private fun updatePieChart() {
        val byCat = allExpenses.groupBy { it.category }
            .mapValues { it.value.sumOf { e -> e.amount } }.filter { it.value > 0 }
        if (byCat.isEmpty()) { binding.pieChartMiniExpenses.visibility = View.GONE; return }
        binding.pieChartMiniExpenses.visibility = View.VISIBLE
        val colors = listOf(0xFFFF4081,0xFF00E5A0,0xFFFFD600,0xFF7C4DFF,0xFF00B0FF,0xFFFF6D00,0xFFE040FB,0xFF64DD17).map { it.toInt() }
        val entries = byCat.entries.map { (cat, amt) -> PieEntry(amt.toFloat(), cat) }
        val ds = PieDataSet(entries, "").apply {
            this.colors = colors.take(entries.size).toMutableList(); setDrawValues(false); sliceSpace = 2f
        }
        binding.pieChartMiniExpenses.apply {
            data = PieData(ds); description.isEnabled = false; legend.isEnabled = false
            isDrawHoleEnabled = true; holeRadius = 50f; setHoleColor(Color.TRANSPARENT)
            centerText = activeCategoryFilter ?: ""
            setCenterTextColor(Color.parseColor("#AABBDD")); setCenterTextSize(10f)
            setTouchEnabled(true); animateY(500); invalidate()
        }
    }

    // ── Dialog aggiunta/modifica spesa ────────────────────────────────────────
    private fun showDialog(existing: Expense?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_expense, null)
        val etDescription  = dialogView.findViewById<TextInputEditText>(R.id.etDescription)
        val etAmount       = dialogView.findViewById<TextInputEditText>(R.id.etAmount)
        val etDate         = dialogView.findViewById<TextInputEditText>(R.id.etDate)
        val etNotes        = dialogView.findViewById<TextInputEditText>(R.id.etNotes)
        val actv           = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerCategory)
        val btnManage      = dialogView.findViewById<ImageButton>(R.id.btnManageCategories)
        val cbSubscription = dialogView.findViewById<android.widget.CheckBox>(R.id.cbSubscription)
        val cbNeed         = dialogView.findViewById<android.widget.CheckBox>(R.id.cbNeed)
        val btnSetRecurring = dialogView.findViewById<android.widget.Button>(R.id.btnSetRecurring)
        val tvBadge         = dialogView.findViewById<android.widget.TextView>(R.id.tvRecurringBadge)

        // Stato ricorrenza impostata nel dialog
        var pendingRecurring: RecurringExpense? = null

        var selectedCategory = existing?.category
            ?: CategoryManager.getCategories(requireContext(), "expense").firstOrNull() ?: ""
        CategoryPickerHelper.setup(requireContext(), actv, btnManage, "expense", selectedCategory) { selectedCategory = it }

        existing?.let { e ->
            etDescription.setText(e.description); etAmount.setText(e.amount.toString())
            etDate.setText(e.date); etNotes.setText(e.notes)
            cbSubscription.isChecked = e.isSubscription; cbNeed.isChecked = e.isNeed
            selectedCategory = e.category; actv.setText(e.category, false)
            // Nasconde il bottone ricorrente in modalità modifica
            btnSetRecurring.visibility = View.GONE
            tvBadge.visibility = View.GONE
        } ?: run { etDate.setText(todayString()) }

        etDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                etDate.setText("%04d-%02d-%02d".format(y, m + 1, d))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        // ── Bottone "🔁 Imposta come spesa ricorrente" ────────────────────────
        btnSetRecurring.setOnClickListener {
            showRecurringDialog(
                defaultAmount    = etAmount.text.toString().toDoubleOrNull() ?: 0.0,
                defaultName      = etDescription.text.toString().ifBlank { selectedCategory },
                defaultCategory  = selectedCategory,
                defaultDate      = etDate.text.toString().ifBlank { todayString() }
            ) { recurring ->
                pendingRecurring = recurring
                // Aggiorna badge
                val freqLabel = when (recurring.frequency) {
                    "WEEKLY" -> "Settimanale"; "MONTHLY" -> "Mensile"
                    "QUARTERLY" -> "Trimestrale"; "ANNUAL" -> "Annuale"; else -> recurring.frequency
                }
                val durLabel = if (recurring.durationMonths > 0) "· ${recurring.durationMonths} mesi" else "· senza scadenza"
                tvBadge.text = "🔁 Ricorrente: $freqLabel $durLabel"
                tvBadge.visibility = View.VISIBLE
                btnSetRecurring.text = "✏️  Modifica ricorrenza"
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing != null) "Modifica Spesa" else "Nuova Spesa")
            .setView(dialogView)
            .setPositiveButton("Salva") { _, _ ->
                val amt  = etAmount.text.toString().toDoubleOrNull() ?: return@setPositiveButton
                val date = etDate.text.toString().ifBlank { todayString() }
                val expense = Expense(
                    id = existing?.id ?: 0, date = date,
                    description = etDescription.text.toString(),
                    category = selectedCategory, amount = amt,
                    isSubscription = cbSubscription.isChecked, isNeed = cbNeed.isChecked,
                    notes = etNotes.text.toString(),
                    month = date.extractMonth(), year = date.extractYear()
                )
                if (existing != null) {
                    viewModel.update(expense)
                } else {
                    val rec = pendingRecurring
                    if (rec != null) {
                        // Salva spesa + ricorrente + genera occorrenze future
                        viewModel.insertRecurringWithFutureExpenses(expense, rec, insertBase = true)
                    } else {
                        viewModel.insert(expense)
                    }
                }
            }
            .setNegativeButton("Annulla", null).show()
    }

    // ── Dialog secondario per impostare la ricorrenza ─────────────────────────
    private fun showRecurringDialog(
        defaultAmount: Double,
        defaultName: String,
        defaultCategory: String,
        defaultDate: String,
        onConfirm: (RecurringExpense) -> Unit
    ) {
        val v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_set_recurring, null)
        val etAmount    = v.findViewById<TextInputEditText>(R.id.etRecurringAmount)
        val spinnerFreq = v.findViewById<AutoCompleteTextView>(R.id.spinnerFrequency)
        val etDuration  = v.findViewById<TextInputEditText>(R.id.etDuration)
        val etStartDate = v.findViewById<TextInputEditText>(R.id.etRecurringStartDate)

        val FREQUENCIES     = listOf("Mensile", "Settimanale", "Trimestrale", "Annuale")
        val FREQUENCY_CODES = listOf("MONTHLY", "WEEKLY", "QUARTERLY", "ANNUAL")

        spinnerFreq.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, FREQUENCIES))
        var selectedFreqCode = "MONTHLY"
        spinnerFreq.setText(FREQUENCIES[0], false)
        spinnerFreq.setOnItemClickListener { _, _, pos, _ -> selectedFreqCode = FREQUENCY_CODES[pos] }

        etAmount.setText(if (defaultAmount > 0) defaultAmount.toString() else "")
        etDuration.setText("0")
        etStartDate.setText(defaultDate)

        etStartDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                etStartDate.setText("%04d-%02d-%02d".format(y, m + 1, d))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Imposta ricorrenza")
            .setView(v)
            .setPositiveButton("Conferma") { _, _ ->
                val amt      = etAmount.text.toString().toDoubleOrNull() ?: defaultAmount
                val duration = etDuration.text.toString().toIntOrNull() ?: 0
                val startDate = etStartDate.text.toString().ifBlank { defaultDate }
                onConfirm(RecurringExpense(
                    name          = defaultName.ifBlank { defaultCategory },
                    category      = defaultCategory,
                    amount        = amt,
                    frequency     = selectedFreqCode,
                    startDate     = startDate,
                    durationMonths = duration,
                    isNeed        = false
                ))
            }
            .setNegativeButton("Annulla", null).show()
    }

    private fun deleteWithUndo(expense: Expense) { viewModel.delete(expense) }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }

    override fun onResume() {
        super.onResume()
        val activity = requireActivity() as MainActivity
        setToolbarMonthTitle(activity.selectedMonth, activity.selectedYear)
    }
}