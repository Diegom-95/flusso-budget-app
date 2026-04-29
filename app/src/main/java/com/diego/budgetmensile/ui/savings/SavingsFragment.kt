package com.diego.budgetmensile.ui.savings

import android.app.Application
import android.app.DatePickerDialog
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
import com.diego.budgetmensile.data.entity.Saving
import com.diego.budgetmensile.databinding.FragmentSavingsBinding
import com.diego.budgetmensile.databinding.ItemSavingBinding
import com.diego.budgetmensile.utils.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Calendar

class SavingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = BudgetRepository(app)
    private val _month = MutableLiveData<Int>()
    private val _year  = MutableLiveData<Int>()
    val savings = _month.switchMap { m -> _year.switchMap { y -> repo.getSavingsByMonth(m, y) } }
    val total   = _month.switchMap { m -> _year.switchMap { y -> repo.getSavingsTotalByMonth(m, y) } }
    fun setMonthYear(m: Int, y: Int) { _month.value = m; _year.value = y }
    fun insert(s: Saving) = viewModelScope.launch { repo.insertSaving(s) }
    fun update(s: Saving) = viewModelScope.launch { repo.updateSaving(s) }
    fun delete(s: Saving) = viewModelScope.launch { repo.deleteSaving(s) }
}

class SavingAdapter(
    private val onEdit: (Saving) -> Unit,
    private val onDelete: (Saving) -> Unit
) : ListAdapter<Saving, SavingAdapter.VH>(DiffCb()) {
    inner class VH(private val b: ItemSavingBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Saving) {
            b.tvCategory.text = item.category; b.tvAmount.text = item.amount.toEuro()
            b.tvDate.text = item.date.toDisplayDate()
            b.chipLong.visibility = if (item.isLongTerm) View.VISIBLE else View.GONE
            b.root.setOnClickListener { onEdit(item) }
            b.btnDelete.setOnClickListener { onDelete(item) }
        }
    }
    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemSavingBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
    class DiffCb : DiffUtil.ItemCallback<Saving>() {
        override fun areItemsTheSame(a: Saving, b: Saving) = a.id == b.id
        override fun areContentsTheSame(a: Saving, b: Saving) = a == b
    }
}

class SavingsFragment : Fragment() {
    private var _binding: FragmentSavingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SavingsViewModel by viewModels()
    private lateinit var adapter: SavingAdapter

    private var allSavings: List<Saving> = emptyList()
    private var activeCategoryFilter: String? = null
    private var sortDesc = true

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentSavingsBinding.inflate(i, c, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        adapter = SavingAdapter(onEdit = { showDialog(it) }, onDelete = { viewModel.delete(it) })
        binding.rvSavings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSavings.adapter = adapter

        SwipeToDeleteCallback.attach(binding.rvSavings, binding.root,
            { adapter.currentList }, { it.category }) { viewModel.delete(it) }

        viewModel.setMonthYear(activity.selectedMonth, activity.selectedYear)
        viewModel.savings.observe(viewLifecycleOwner) { list ->
            allSavings = list ?: emptyList()
            applyFilterAndSort()
            updateCategoryChips()
            updatePieChart()
            binding.tvLongTerm.text  = "Lungo: ${allSavings.filter { it.isLongTerm }.sumOf { it.amount }.toEuro()}"
            binding.tvShortTerm.text = "Breve: ${allSavings.filter { !it.isLongTerm }.sumOf { it.amount }.toEuro()}"
        }
        viewModel.total.observe(viewLifecycleOwner) { v ->
            binding.tvTotal.text = "Totale Risparmi: ${(v ?: 0.0).toEuro()}"
        }
        binding.fabAddSaving.setOnClickListener { showDialog(null) }
        binding.btnSort.setOnClickListener { sortDesc = !sortDesc; applyFilterAndSort() }

        binding.pieChartMiniSavings.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val ds = binding.pieChartMiniSavings.data?.getDataSetByIndex(0) as? PieDataSet ?: return
                val label = ds.getEntryForIndex(h?.x?.toInt() ?: 0)?.label ?: return
                activeCategoryFilter = if (activeCategoryFilter == label) null else label
                updateCategoryChips(); applyFilterAndSort()
            }
            override fun onNothingSelected() { activeCategoryFilter = null; updateCategoryChips(); applyFilterAndSort() }
        })
    }

    private fun applyFilterAndSort() {
        var list = allSavings
        if (activeCategoryFilter != null) list = list.filter { it.category == activeCategoryFilter }
        list = if (sortDesc) list.sortedByDescending { it.date } else list.sortedBy { it.date }
        adapter.submitList(list)
        binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateCategoryChips() {
        val group = binding.chipGroupSavings; group.removeAllViews()
        val cats = allSavings.map { it.category }.distinct().sorted()
        if (cats.isEmpty()) { group.visibility = View.GONE; return }
        group.visibility = View.VISIBLE
        val allChip = Chip(requireContext()).apply {
            text = "Tutti"; isCheckable = true; isChecked = activeCategoryFilter == null
            styleForSavingCategory("__all__", activeCategoryFilter == null)
            setOnClickListener { activeCategoryFilter = null; clearChips(group); isChecked = true; styleForSavingCategory("__all__", true); applyFilterAndSort() }
        }
        group.addView(allChip)
        cats.forEach { cat ->
            group.addView(Chip(requireContext()).apply {
                text = cat; isCheckable = true; isChecked = activeCategoryFilter == cat
                styleForSavingCategory(cat, activeCategoryFilter == cat)
                setOnClickListener {
                    activeCategoryFilter = if (activeCategoryFilter == cat) null else cat
                    clearChips(group); isChecked = activeCategoryFilter == cat
                    styleForSavingCategory(cat, activeCategoryFilter == cat)
                    if (activeCategoryFilter == null) { allChip.isChecked = true; allChip.styleForSavingCategory("__all__", true) }
                    applyFilterAndSort(); updatePieChart()
                }
            })
        }
    }

    private fun clearChips(group: ChipGroup) {
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as? Chip ?: continue
            chip.isChecked = false
            chip.styleForSavingCategory(chip.text.toString(), false)
        }
    }
    private fun updatePieChart() {
        val byCat = allSavings.groupBy { it.category }.mapValues { it.value.sumOf { s -> s.amount } }.filter { it.value > 0 }
        if (byCat.isEmpty()) { binding.pieChartMiniSavings.visibility = View.GONE; return }
        binding.pieChartMiniSavings.visibility = View.VISIBLE
        val colors = listOf(0xFFFFD600,0xFF00E5A0,0xFF7C4DFF,0xFF00B0FF,0xFFFF6D00).map { it.toInt() }
        val ds = PieDataSet(byCat.entries.map { (cat, amt) -> PieEntry(amt.toFloat(), cat) }, "").apply { this.colors = colors.take(byCat.size).toMutableList(); setDrawValues(false); sliceSpace = 2f }
        binding.pieChartMiniSavings.apply {
            data = PieData(ds); description.isEnabled = false; legend.isEnabled = false
            isDrawHoleEnabled = true; holeRadius = 50f; setHoleColor(Color.TRANSPARENT)
            setTouchEnabled(true); animateY(500); invalidate()
        }
    }

    private fun showDialog(existing: Saving?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_saving, null)
        val etAmount   = dialogView.findViewById<EditText>(R.id.etAmount)
        val etDate     = dialogView.findViewById<EditText>(R.id.etDate)
        val etNotes    = dialogView.findViewById<EditText>(R.id.etNotes)
        val actv       = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerCategory)
        val btnManage  = dialogView.findViewById<ImageButton>(R.id.btnManageCategories)
        val cbLongTerm = dialogView.findViewById<CheckBox>(R.id.cbLongTerm)
        var selectedCategory = existing?.category ?: CategoryManager.getCategories(requireContext(), "saving").firstOrNull() ?: ""
        CategoryPickerHelper.setup(requireContext(), actv, btnManage, "saving", selectedCategory) { selectedCategory = it }
        existing?.let { s -> etAmount.setText(s.amount.toString()); etDate.setText(s.date); etNotes.setText(s.notes); cbLongTerm.isChecked = s.isLongTerm; actv.setText(s.category, false) } ?: run { etDate.setText(todayString()) }
        etDate.setOnClickListener { val c = Calendar.getInstance(); DatePickerDialog(requireContext(), { _, y, m, d -> etDate.setText("%04d-%02d-%02d".format(y, m+1, d)) }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() }
        MaterialAlertDialogBuilder(requireContext()).setTitle(if (existing != null) "Modifica Risparmio" else "Nuovo Risparmio").setView(dialogView)
            .setPositiveButton("Salva") { _, _ ->
                val amt = etAmount.text.toString().toDoubleOrNull() ?: return@setPositiveButton
                val date = etDate.text.toString().ifBlank { todayString() }
                val saving = Saving(id=existing?.id ?: 0, date=date, category=selectedCategory, amount=amt, isLongTerm=cbLongTerm.isChecked, notes=etNotes.text.toString(), month=date.extractMonth(), year=date.extractYear())
                if (existing != null) viewModel.update(saving) else viewModel.insert(saving)
            }.setNegativeButton("Annulla", null).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }

    override fun onResume() {
        super.onResume()
        val activity = requireActivity() as MainActivity
        setToolbarMonthTitle(activity.selectedMonth, activity.selectedYear)
    }
}
