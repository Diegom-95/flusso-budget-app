package com.diego.budgetmensile.ui.recurring

import android.app.Application
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ListAdapter
import com.diego.budgetmensile.R
import com.diego.budgetmensile.data.BudgetRepository
import com.diego.budgetmensile.data.entity.RecurringExpense
import com.diego.budgetmensile.databinding.FragmentRecurringManagerBinding
import com.diego.budgetmensile.databinding.ItemRecurringBinding
import com.diego.budgetmensile.utils.toEuro
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RecurringViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = BudgetRepository(app)
    val recurring = repo.getAllRecurring()
    fun insert(r: RecurringExpense) = viewModelScope.launch { repo.insertRecurring(r) }
    fun update(r: RecurringExpense) = viewModelScope.launch { repo.updateRecurring(r) }
    fun delete(r: RecurringExpense) = viewModelScope.launch { repo.deleteRecurring(r) }
    fun updateWithFuture(old: RecurringExpense, new: RecurringExpense) = viewModelScope.launch {
        repo.updateRecurring(new)
        // Aggiorna le spese future già generate (oggi in poi)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        repo.updateFutureExpensesByName(old.name, new.amount, new.category, today)
    }
}

class RecurringAdapter(
    private val onEdit:   (RecurringExpense) -> Unit,
    private val onDelete: (RecurringExpense) -> Unit
) : ListAdapter<RecurringExpense, RecurringAdapter.VH>(DiffCb()) {

    inner class VH(private val b: ItemRecurringBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: RecurringExpense) {
            b.tvName.text      = item.name
            b.tvCategory.text  = item.category
            b.tvFrequency.text = freqLabel(item.frequency)
            b.tvAmount.text    = "-${item.amount.toEuro()}"
            val durText = if (item.durationMonths > 0) "${item.durationMonths} mesi" else "Senza scadenza"
            b.tvDuration.text = "Dal ${item.startDate}  ·  $durText"
            val monthly = monthlyEquiv(item)
            b.tvMonthlyEquiv.text = if (item.frequency != "MONTHLY") "≈ ${monthly.toEuro()}/mese" else ""
            b.btnEdit.setOnClickListener   { onEdit(item) }
            b.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    fun freqLabel(f: String) = when(f) {
        "WEEKLY" -> "Sett."; "MONTHLY" -> "Mens."
        "QUARTERLY" -> "Trim."; "ANNUAL" -> "Ann."; else -> f
    }
    fun monthlyEquiv(r: RecurringExpense) = when(r.frequency) {
        "WEEKLY" -> r.amount * 52 / 12; "QUARTERLY" -> r.amount / 3
        "ANNUAL" -> r.amount / 12; else -> r.amount
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemRecurringBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
    class DiffCb : DiffUtil.ItemCallback<RecurringExpense>() {
        override fun areItemsTheSame(a: RecurringExpense, b: RecurringExpense) = a.id == b.id
        override fun areContentsTheSame(a: RecurringExpense, b: RecurringExpense) = a == b
    }
}

class RecurringManagerBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentRecurringManagerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecurringViewModel by viewModels()
    private lateinit var adapter: RecurringAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentRecurringManagerBinding.inflate(i, c, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = RecurringAdapter(onEdit = { showEditDialog(it) }, onDelete = { confirmDelete(it) })
        binding.rvRecurring.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecurring.adapter = adapter
        viewModel.recurring.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            val monthly = list.filter { it.isActive }.sumOf { adapter.monthlyEquiv(it) }
            binding.tvTotaleMensile.text = monthly.toEuro()
        }
        binding.btnBack.setOnClickListener { dismiss() }
    }

    private fun showEditDialog(r: RecurringExpense) {
        val v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_set_recurring, null)
        val spinnerFreq = v.findViewById<AutoCompleteTextView>(R.id.spinnerFrequency)
        val etAmount    = v.findViewById<TextInputEditText>(R.id.etRecurringAmount)
        val etDuration  = v.findViewById<TextInputEditText>(R.id.etDuration)
        val etStartDate = v.findViewById<TextInputEditText>(R.id.etRecurringStartDate)

        val FREQUENCIES     = listOf("Mensile","Settimanale","Trimestrale","Annuale")
        val FREQUENCY_CODES = listOf("MONTHLY","WEEKLY","QUARTERLY","ANNUAL")
        spinnerFreq.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, FREQUENCIES))
        var selectedFreqCode = r.frequency
        spinnerFreq.setText(FREQUENCIES[FREQUENCY_CODES.indexOf(r.frequency).coerceAtLeast(0)], false)
        spinnerFreq.setOnItemClickListener { _, _, pos, _ -> selectedFreqCode = FREQUENCY_CODES[pos] }

        etAmount?.setText(r.amount.toString())
        etDuration.setText(r.durationMonths.toString())
        etStartDate.setText(r.startDate)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Modifica: ${r.name}")
            .setView(v)
            .setPositiveButton("Salva") { _, _ ->
                val newAmount = etAmount?.text.toString().toDoubleOrNull() ?: r.amount
                val updated = r.copy(
                    frequency      = selectedFreqCode,
                    amount         = newAmount,
                    durationMonths = etDuration.text.toString().toIntOrNull() ?: 0,
                    startDate      = etStartDate.text.toString()
                )
                // Importo o categoria cambiati? Chiedi se aggiornare anche le future
                val amountChanged = newAmount != r.amount
                if (amountChanged) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Aggiorna spese future?")
                        .setMessage("Vuoi aggiornare anche le spese già pianificate nei prossimi mesi?")
                        .setPositiveButton("Sì, aggiorna tutto") { _, _ ->
                            viewModel.updateWithFuture(r, updated)
                        }
                        .setNegativeButton("Solo il template") { _, _ ->
                            viewModel.update(updated)
                        }
                        .show()
                } else {
                    viewModel.update(updated)
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun confirmDelete(r: RecurringExpense) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Elimina ricorrente")
            .setMessage("Eliminare \"${r.name}\"?\n\nLe spese già inserite nei mesi passati non verranno cancellate.")
            .setPositiveButton("Elimina") { _, _ -> viewModel.delete(r) }
            .setNegativeButton("Annulla", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
