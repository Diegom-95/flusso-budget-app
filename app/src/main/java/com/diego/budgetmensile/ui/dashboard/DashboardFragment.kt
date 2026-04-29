package com.diego.budgetmensile.ui.dashboard

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.diego.budgetmensile.MainActivity
import com.diego.budgetmensile.R
import com.diego.budgetmensile.databinding.FragmentDashboardBinding
import com.diego.budgetmensile.utils.*
import com.github.mikephil.charting.data.*
import java.util.Calendar

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @Suppress("DEPRECATION")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard_overflow, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_goals    -> { findNavController().navigate(R.id.action_dashboard_to_goals); true }
            R.id.action_settings -> { findNavController().navigate(R.id.action_dashboard_to_settings); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity

        setupMonthSpinner(activity)
        setupCharts()
        viewModel.setMonthYear(activity.selectedMonth, activity.selectedYear)

        binding.btnAnnual.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_annual)
        }

        // KPI semplici
        viewModel.totalIncome.observe(viewLifecycleOwner)   { binding.tvTotaleEntrate.text = (it ?: 0.0).toEuro(); refreshKpi() }
        viewModel.totalExpenses.observe(viewLifecycleOwner) { binding.tvTotaleSpese.text   = (it ?: 0.0).toEuro(); refreshKpi() }
        viewModel.totalSavings.observe(viewLifecycleOwner)  { binding.tvTotaleRisparmi.text = (it ?: 0.0).toEuro(); refreshKpi() }
        viewModel.totalDebts.observe(viewLifecycleOwner)    { binding.tvTotaleDebiti.text  = (it ?: 0.0).toEuro() }
        viewModel.rimastoAllocare.observe(viewLifecycleOwner) { v ->
            binding.tvRimasto.text = v.toEuro()
            binding.tvRimasto.setTextColor(resources.getColor(
                if (v >= 0) R.color.color_income else R.color.color_expense, null))
        }
        viewModel.needsTotal.observe(viewLifecycleOwner)  { binding.tvNecessarie.text = (it ?: 0.0).toEuro(); refreshNeedWant() }
        viewModel.subscriptions.observe(viewLifecycleOwner) { binding.tvAbbonamenti.text = (it ?: 0.0).toEuro() }

        // ── SPESE ──────────────────────────────────────────────────────────────
        viewModel.expenses.observe(viewLifecycleOwner) { expenses ->
            // Donut spese per categoria (corretto)
            val byCat = expenses.groupBy { it.category }
                .mapValues { it.value.sumOf { e -> e.amount } }
                .filter { it.value > 0 }
            ChartHelper.setPieData(binding.pieChartSpese, byCat)

            // Bar chart categorie (corretto)
            ChartHelper.setBarData(binding.barChartCategorie, byCat)

            // Line chart spese giornaliere cumulative
            val dailyMap = expenses.groupBy { it.date.substring(8, 10).toIntOrNull() ?: 1 }
                .mapValues { it.value.sumOf { e -> e.amount } }
                .toSortedMap()

            var cum = 0.0
            val lineEntries = mutableListOf<Entry>()
            val maxDay = dailyMap.keys.maxOrNull() ?: 1
            for (day in 1..maxDay) {
                cum += dailyMap[day] ?: 0.0
                lineEntries.add(Entry(day.toFloat(), cum.toFloat()))
            }
            val totalExp = viewModel.totalExpenses.value ?: 0.0
            val budgetEntries = listOf(Entry(1f, totalExp.toFloat()), Entry(31f, totalExp.toFloat()))

            if (lineEntries.isNotEmpty()) {
                val ds1 = ChartHelper.buildLineDataSet(lineEntries, "Actual", 0xFFFF4081.toInt())
                val ds2 = ChartHelper.buildDashedLineDataSet(budgetEntries, "Budget", 0xFFFF4081.toInt())
                binding.lineChartSpese.data = LineData(ds1, ds2)
                binding.lineChartSpese.notifyDataSetChanged()
                binding.lineChartSpese.invalidate()
                binding.lineChartSpese.animateX(700)
            } else {
                binding.lineChartSpese.clear()
                binding.lineChartSpese.invalidate()
            }
        }

        // ── ENTRATE donut ──────────────────────────────────────────────────────
        viewModel.incomes.observe(viewLifecycleOwner) { incomes ->
            val byCat = incomes.groupBy { it.category }
                .mapValues { it.value.sumOf { i -> i.amount } }
                .filter { it.value > 0 }
            ChartHelper.setPieData(binding.pieChartEntrate, byCat)
        }

        // ── RISPARMI donut ─────────────────────────────────────────────────────
        viewModel.savings.observe(viewLifecycleOwner) { savings ->
            val byCat = savings.groupBy { it.category }
                .mapValues { it.value.sumOf { s -> s.amount } }
                .filter { it.value > 0 }
            ChartHelper.setPieData(binding.pieChartRisparmi, byCat)

            binding.tvLungoTermine.text = savings.filter { it.isLongTerm }.sumOf { it.amount }.toEuro()
            binding.tvBreveTermine.text = savings.filter { !it.isLongTerm }.sumOf { it.amount }.toEuro()
        }
    }

    private fun setupMonthSpinner(activity: MainActivity) {
        val months = Constants.MONTHS
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 2..currentYear + 1).map { it.toString() }

        binding.spinnerMonth.adapter = ArrayAdapter(requireContext(), R.layout.item_spinner, months)
            .apply { setDropDownViewResource(R.layout.item_spinner_dropdown) }
        binding.spinnerYear.adapter = ArrayAdapter(requireContext(), R.layout.item_spinner, years)
            .apply { setDropDownViewResource(R.layout.item_spinner_dropdown) }

        binding.spinnerMonth.setSelection(activity.selectedMonth - 1)
        binding.spinnerYear.setSelection(years.indexOf(activity.selectedYear.toString()).coerceAtLeast(0))

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                val m = binding.spinnerMonth.selectedItemPosition + 1
                val y = years[binding.spinnerYear.selectedItemPosition].toInt()
                activity.selectedMonth = m; activity.selectedYear = y
                binding.tvMeseHeader.text = "${Constants.MONTHS[m-1]} $y"
                viewModel.setMonthYear(m, y)
            }
            override fun onNothingSelected(p: AdapterView<*>?) = Unit
        }
        binding.spinnerMonth.onItemSelectedListener = listener
        binding.spinnerYear.onItemSelectedListener  = listener
        binding.tvMeseHeader.text = "${Constants.MONTHS[activity.selectedMonth-1]} ${activity.selectedYear}"
    }

    private fun setupCharts() {
        ChartHelper.stylePieChart(binding.pieChartSpese,    0xFF162035.toInt())
        ChartHelper.stylePieChart(binding.pieChartEntrate,  0xFF162035.toInt())
        ChartHelper.stylePieChart(binding.pieChartRisparmi, 0xFF162035.toInt())
        ChartHelper.styleLineChart(binding.lineChartSpese)
        ChartHelper.styleBarChart(binding.barChartCategorie)
    }

    private fun refreshKpi() {
        val inc = viewModel.totalIncome.value   ?: 0.0
        val exp = viewModel.totalExpenses.value ?: 0.0
        val sav = viewModel.totalSavings.value  ?: 0.0
        if (inc > 0) {
            val pctExp = (exp / inc * 100).toInt()
            val pctSav = (sav / inc * 100).toInt()
            binding.tvPctSpese.text    = "$pctExp%"
            binding.tvPctRisparmi.text = "$pctSav%"
            binding.progressSpese.progress    = pctExp.coerceIn(0, 100)
            binding.progressRisparmi.progress = pctSav.coerceIn(0, 100)
        }
    }

    private fun refreshNeedWant() {
        val exp  = viewModel.totalExpenses.value ?: 0.0
        val need = viewModel.needsTotal.value     ?: 0.0
        if (exp > 0) {
            val want = exp - need
            binding.tvVolontarie.text = want.toEuro()
            binding.tvPctNeed.text  = "${(need / exp * 100).toInt()}%"
            binding.tvPctWant.text  = "${(want / exp * 100).toInt()}%"
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
