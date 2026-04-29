package com.diego.budgetmensile.ui.income

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ListAdapter
import com.diego.budgetmensile.MainActivity
import com.diego.budgetmensile.R
import com.diego.budgetmensile.data.BudgetRepository
import com.diego.budgetmensile.data.entity.Income
import com.diego.budgetmensile.databinding.FragmentIncomeBinding
import com.diego.budgetmensile.databinding.ItemIncomeBinding
import com.diego.budgetmensile.utils.*import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar
import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch


class IncomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = BudgetRepository(app)
    private val _month = MutableLiveData<Int>()
    private val _year  = MutableLiveData<Int>()
    val incomes = _month.switchMap { m -> _year.switchMap { y -> repo.getIncomesByMonth(m, y) } }
    val total   = _month.switchMap { m -> _year.switchMap { y -> repo.getIncomeTotalByMonth(m, y) } }
    fun setMonthYear(m: Int, y: Int) { _month.value = m; _year.value = y }
    fun insert(i: Income) = viewModelScope.launch { repo.insertIncome(i) }
    fun update(i: Income) = viewModelScope.launch { repo.updateIncome(i) }
    fun delete(i: Income) = viewModelScope.launch { repo.deleteIncome(i) }
}

class IncomeAdapter(
    private val onEdit: (Income) -> Unit,
    private val onDelete: (Income) -> Unit
) : ListAdapter<Income, IncomeAdapter.VH>(DiffCb()) {
    inner class VH(private val b: ItemIncomeBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Income) {
            b.tvCategory.text    = item.category
            b.tvDescription.text = item.description.ifBlank { item.category }
            b.tvAmount.text      = "+${item.amount.toEuro()}"
            b.tvDate.text        = item.date.toDisplayDate()
            b.chipActive.visibility = if (item.isActive) View.VISIBLE else View.GONE
            b.root.setOnClickListener { onEdit(item) }
            b.btnDelete.setOnClickListener { onDelete(item) }
        }
    }
    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemIncomeBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
    class DiffCb : DiffUtil.ItemCallback<Income>() {
        override fun areItemsTheSame(a: Income, b: Income) = a.id == b.id
        override fun areContentsTheSame(a: Income, b: Income) = a == b
    }
}

class IncomeFragment : Fragment() {
    private var _binding: FragmentIncomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: IncomeViewModel by viewModels()
    private lateinit var adapter: IncomeAdapter

    private var allIncomes: List<Income> = emptyList()
    private var activeCategoryFilter: String? = null
    private var sortDesc = true
    private var searchQuery: String = ""

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentIncomeBinding.inflate(i, c, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity

        adapter = IncomeAdapter(onEdit = { showDialog(it) }, onDelete = { viewModel.delete(it) })
        binding.rvIncomes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvIncomes.adapter = adapter

        SwipeToDeleteCallback.attach(binding.rvIncomes, binding.root,
            { adapter.currentList }, { it.description.ifBlank { it.category } }) { viewModel.delete(it) }

        viewModel.setMonthYear(activity.selectedMonth, activity.selectedYear)
        viewModel.incomes.observe(viewLifecycleOwner) { list ->
            allIncomes = list ?: emptyList()
            applyFilterAndSort()
            updateCategoryChips()
            updatePieChart()
        }
        viewModel.total.observe(viewLifecycleOwner) { v ->
            binding.tvTotal.text = "Totale Entrate: ${(v ?: 0.0).toEuro()}"
        }

        binding.fabAddIncome.setOnClickListener { showDialog(null) }
        binding.btnSort.setOnClickListener {
            sortDesc = !sortDesc
            applyFilterAndSort()
        }

        // 🔍 Ricerca
        binding.btnSearch.setOnClickListener {
            val isVisible = binding.searchViewIncome.visibility == View.VISIBLE
            if (isVisible) {
                binding.searchViewIncome.visibility = View.GONE
                binding.searchViewIncome.setQuery("", false)
                searchQuery = ""
                applyFilterAndSort()
            } else {
                binding.searchViewIncome.visibility = View.VISIBLE
                binding.searchViewIncome.requestFocus()
            }
        }
        binding.searchViewIncome.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText?.trim() ?: ""
                applyFilterAndSort()
                return true
            }
        })

        binding.pieChartMiniIncome.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val ds = binding.pieChartMiniIncome.data?.getDataSetByIndex(0) as? PieDataSet ?: return
                val sliceLabel = ds.getEntryForIndex(h?.x?.toInt() ?: 0)?.label ?: return
                activeCategoryFilter = if (activeCategoryFilter == sliceLabel) null else sliceLabel
                updateCategoryChips(); applyFilterAndSort()
            }
            override fun onNothingSelected() { activeCategoryFilter = null; updateCategoryChips(); applyFilterAndSort() }
        })
    }

    private fun applyFilterAndSort() {
        var list = allIncomes
        if (activeCategoryFilter != null) list = list.filter { it.category == activeCategoryFilter }
        if (searchQuery.isNotEmpty()) {
            val q = searchQuery.lowercase()
            list = list.filter { inc ->
                inc.description.lowercase().contains(q) ||
                inc.category.lowercase().contains(q) ||
                inc.notes.lowercase().contains(q) ||
                inc.amount.toString().contains(q) ||
                inc.date.contains(q)
            }
        }
        list = if (sortDesc) list.sortedByDescending { it.date } else list.sortedBy { it.date }
        adapter.submitList(list)
        binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateCategoryChips() {
        val group = binding.chipGroupIncome
        group.removeAllViews()
        val cats = allIncomes.map { it.category }.distinct().sorted()
        if (cats.isEmpty()) { group.visibility = View.GONE; return }
        group.visibility = View.VISIBLE
        val allChip = Chip(requireContext()).apply {
            text = "Tutti"; isCheckable = true; isChecked = activeCategoryFilter == null
            styleForCategory("__all__", activeCategoryFilter == null)
            setOnClickListener { activeCategoryFilter = null; clearChips(group); isChecked = true; styleForCategory("__all__", true); applyFilterAndSort() }
        }
        group.addView(allChip)
        cats.forEach { cat ->
            group.addView(Chip(requireContext()).apply {
                text = cat; isCheckable = true; isChecked = activeCategoryFilter == cat
                styleForCategory(cat, activeCategoryFilter == cat)
                setOnClickListener {
                    activeCategoryFilter = if (activeCategoryFilter == cat) null else cat
                    clearChips(group)
                    isChecked = activeCategoryFilter == cat
                    styleForCategory(cat, activeCategoryFilter == cat)
                    if (activeCategoryFilter == null) { allChip.isChecked = true; allChip.styleForCategory("__all__", true) }
                    applyFilterAndSort(); updatePieChart()
                }
            })
        }
    }

    private fun clearChips(group: ChipGroup) {
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as? Chip ?: continue
            chip.isChecked = false
            chip.styleForCategory(chip.text.toString(), false)
        }
    }

    private fun updatePieChart() {
        val byCat = allIncomes.groupBy { it.category }.mapValues { it.value.sumOf { e -> e.amount } }.filter { it.value > 0 }
        if (byCat.isEmpty()) { binding.pieChartMiniIncome.visibility = View.GONE; return }
        binding.pieChartMiniIncome.visibility = View.VISIBLE
        val colors = listOf(0xFF00E5A0,0xFF00B0FF,0xFFFFD600,0xFF7C4DFF,0xFFFF6D00,0xFF64DD17).map { it.toInt() }
        val entries = byCat.entries.map { (cat, amt) -> PieEntry(amt.toFloat(), cat) }
        val ds = PieDataSet(entries, "").apply { this.colors = colors.take(entries.size).toMutableList(); setDrawValues(false); sliceSpace = 2f }
        binding.pieChartMiniIncome.apply {
            data = PieData(ds); description.isEnabled = false; legend.isEnabled = false
            isDrawHoleEnabled = true; holeRadius = 50f; setHoleColor(Color.TRANSPARENT)
            setTouchEnabled(true); animateY(500); invalidate()
        }
    }

    private fun showDialog(existing: Income?) {
        val v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_income, null)
        val etDesc   = v.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDescription)
        val etAmount = v.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etAmount)
        val etDate   = v.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDate)
        val etNotes  = v.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNotes)
        val actv     = v.findViewById<AutoCompleteTextView>(R.id.spinnerCategory)
        val btnMng   = v.findViewById<android.widget.ImageButton>(R.id.btnManageCategories)
        val cbActive = v.findViewById<android.widget.CheckBox>(R.id.cbActive)

        var selectedCat = existing?.category ?: CategoryManager.getCategories(requireContext(), "income").firstOrNull() ?: ""
        CategoryPickerHelper.setup(requireContext(), actv, btnMng, "income", selectedCat) { selectedCat = it }
        existing?.let { e -> etDesc.setText(e.description); etAmount.setText(e.amount.toString()); etDate.setText(e.date); etNotes.setText(e.notes); cbActive.isChecked = e.isActive; actv.setText(e.category, false) }
            ?: run { etDate.setText(todayString()); cbActive.isChecked = true }
        etDate.setOnClickListener { val c = Calendar.getInstance(); DatePickerDialog(requireContext(), { _, y, m, d -> etDate.setText("%04d-%02d-%02d".format(y, m+1, d)) }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() }
        MaterialAlertDialogBuilder(requireContext()).setTitle(if (existing != null) "Modifica Entrata" else "Nuova Entrata").setView(v)
            .setPositiveButton("Salva") { _, _ ->
                val amt = etAmount.text.toString().toDoubleOrNull() ?: return@setPositiveButton
                val date = etDate.text.toString().ifBlank { todayString() }
                val inc = Income(id=existing?.id ?: 0, date=date, description=etDesc.text.toString(), category=selectedCat, amount=amt, isActive=cbActive.isChecked, notes=etNotes.text.toString(), month=date.extractMonth(), year=date.extractYear())
                if (existing != null) viewModel.update(inc) else viewModel.insert(inc)
            }.setNegativeButton("Annulla", null).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }

    override fun onResume() {
        super.onResume()
        val activity = requireActivity() as MainActivity
        setToolbarMonthTitle(activity.selectedMonth, activity.selectedYear)
    }
}
