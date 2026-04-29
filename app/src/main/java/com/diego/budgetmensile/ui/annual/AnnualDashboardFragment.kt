package com.diego.budgetmensile.ui.annual

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.diego.budgetmensile.MainActivity
import com.diego.budgetmensile.R
import com.diego.budgetmensile.databinding.FragmentAnnualDashboardBinding
import com.diego.budgetmensile.utils.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.Calendar

class AnnualDashboardFragment : Fragment() {

    private var _binding: FragmentAnnualDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnnualDashboardViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentAnnualDashboardBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 2..currentYear + 1).map { it.toString() }
        binding.spinnerYearAnnual.adapter = ArrayAdapter(requireContext(), R.layout.item_spinner, years)
            .apply { setDropDownViewResource(R.layout.item_spinner_dropdown) }
        binding.spinnerYearAnnual.setSelection(years.indexOf(activity.selectedYear.toString()).coerceAtLeast(0))
        binding.spinnerYearAnnual.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                activity.selectedYear = years[pos].toInt()
                viewModel.setYear(activity.selectedYear)
            }
            override fun onNothingSelected(p: AdapterView<*>?) = Unit
        }

        ChartHelper.stylePieChart(binding.pieChartSpese,   0xFF162035.toInt())
        ChartHelper.stylePieChart(binding.pieChartEntrate, 0xFF162035.toInt())
        ChartHelper.styleBarChart(binding.barChartConfronto, Constants.MONTHS_SHORT)
        ChartHelper.styleBarChart(binding.barChartSpeseMensili, Constants.MONTHS_SHORT)
        styleLineChart()

        viewModel.setYear(activity.selectedYear)

        // ── SPESE ─────────────────────────────────────────────────────────────
        viewModel.expensesByYear.observe(viewLifecycleOwner) { expenses ->
            val monthly = (1..12).map { m -> expenses.filter { it.month == m }.sumOf { it.amount } }
            val total   = monthly.sum()
            binding.tvTotaleSpese.text = total.toEuro()

            val ds1entries = monthly.mapIndexed { i, v ->
                BarEntry(i.toFloat(), v.toFloat()) }
            val ds1 = BarDataSet(ds1entries, "Spese Mensili").apply {
                color = 0xFFFF4081.toInt(); setDrawValues(false) }
            binding.barChartSpeseMensili.data =
                BarData(ds1).apply { barWidth = 0.6f }
            ChartHelper.styleBarChart(binding.barChartSpeseMensili, Constants.MONTHS_SHORT)
            binding.barChartSpeseMensili.notifyDataSetChanged()
            binding.barChartSpeseMensili.invalidate()
            binding.barChartSpeseMensili.animateY(700)

            val byCat = expenses.groupBy { it.category }
                .mapValues { it.value.sumOf { e -> e.amount } }.filter { it.value > 0 }
            ChartHelper.setPieData(binding.pieChartSpese, byCat)

            val need = expenses.filter { it.isNeed }.sumOf { it.amount }
            val want = total - need
            binding.tvNecessarie.text  = need.toEuro()
            binding.tvVolontarie.text  = want.toEuro()
            binding.tvAbbonamenti.text = expenses.filter { it.isSubscription }.sumOf { it.amount }.toEuro()
            if (total > 0) {
                binding.tvPctNecessarie.text = "${(need / total * 100).toInt()}%"
                binding.tvPctVolontarie.text = "${(want / total * 100).toInt()}%"
            }
            val nonZero = monthly.filter { it > 0 }
            binding.tvMediaSpese.text = if (nonZero.isNotEmpty()) (total / nonZero.size).toEuro() else "0 €"
            val maxIdx = monthly.indexOfFirst { it == monthly.maxOrNull() }
            val minIdx = monthly.indexOfFirst { it > 0 && it == nonZero.minOrNull() }
            if (maxIdx >= 0) binding.tvMaxSpese.text = Constants.MONTHS_SHORT[maxIdx]
            if (minIdx >= 0) binding.tvMinSpese.text = Constants.MONTHS_SHORT[minIdx]
            updateGroupedBar()
        }

        // ── ENTRATE ───────────────────────────────────────────────────────────
        viewModel.incomesByYear.observe(viewLifecycleOwner) { incomes ->
            val monthly = (1..12).map { m -> incomes.filter { it.month == m }.sumOf { it.amount } }
            val total   = monthly.sum()
            binding.tvTotaleEntrate.text = total.toEuro()

            val byCat = incomes.groupBy { it.category }
                .mapValues { it.value.sumOf { i -> i.amount } }.filter { it.value > 0 }
            ChartHelper.setPieData(binding.pieChartEntrate, byCat)

            val nonZero = monthly.filter { it > 0 }
            binding.tvMediaEntrate.text = if (nonZero.isNotEmpty()) (total / nonZero.size).toEuro() else "0 €"
            val maxIdx = monthly.indexOfFirst { it == monthly.maxOrNull() }
            val minIdx = monthly.indexOfFirst { it > 0 && it == nonZero.minOrNull() }
            if (maxIdx >= 0) binding.tvMaxEntrate.text = Constants.MONTHS_SHORT[maxIdx]
            if (minIdx >= 0) binding.tvMinEntrate.text = Constants.MONTHS_SHORT[minIdx]
            updateGroupedBar()
        }

        // ── PRELIEVI OBIETTIVI ─────────────────────────────────────────────────
        viewModel.withdrawalsByYear.observe(viewLifecycleOwner) { withdrawals ->
            if (withdrawals.isNullOrEmpty()) {
                binding.cardWithdrawals.visibility = View.GONE
            } else {
                binding.cardWithdrawals.visibility = View.VISIBLE
                val totalW = withdrawals.sumOf { it.amount }
                binding.tvTotalePrelievi.text = totalW.toEuro()
                val byGoal = withdrawals.groupBy { it.goalName }
                    .mapValues { it.value.sumOf { w -> w.amount } }.filter { it.value > 0 }
                binding.containerPrelievi.removeAllViews()
                byGoal.entries.sortedByDescending { it.value }.forEach { (name, amount) ->
                    val row = LayoutInflater.from(requireContext())
                        .inflate(android.R.layout.simple_list_item_2, binding.containerPrelievi, false)
                    row.findViewById<TextView>(android.R.id.text1).apply {
                        text = name; setTextColor(Color.parseColor("#CCCCCC")); textSize = 14f }
                    row.findViewById<TextView>(android.R.id.text2).apply {
                        text = "-${amount.toEuro()}"; setTextColor(Color.parseColor("#FF4081")); textSize = 13f }
                    binding.containerPrelievi.addView(row)
                }
                ChartHelper.setPieData(binding.pieChartPrelievi, byGoal)
            }
            updateGroupedBar()
            updatePatrimonio()
        }

        // ── DEBITI e RISPARMI per PATRIMONIO ──────────────────────────────────
        viewModel.debtsByYear.observe(viewLifecycleOwner)    { updatePatrimonio() }
        viewModel.savingsByYear.observe(viewLifecycleOwner)  { updatePatrimonio() }
    }

    // ── Grafico confronto Entrate / Uscite totali ──────────────────────────────
    private fun updateGroupedBar() {
        val expenses    = viewModel.expensesByYear.value    ?: return
        val incomes     = viewModel.incomesByYear.value     ?: return
        val withdrawals = viewModel.withdrawalsByYear.value ?: emptyList()

        val expMonthly = (1..12).map { m -> expenses.filter { it.month == m }.sumOf { it.amount } }
        val incMonthly = (1..12).map { m -> incomes.filter { it.month == m }.sumOf { it.amount } }
        val wdMonthly  = (1..12).map { m ->
            withdrawals.filter {
                it.date.length >= 7 && it.date.substring(5, 7).toIntOrNull() == m
            }.sumOf { it.amount }
        }
        val totalExpMonthly = expMonthly.zip(wdMonthly).map { (e, w) -> e + w }

        ChartHelper.setGroupedBarData(
            binding.barChartConfronto,
            incMonthly,      "Entrate",       0xFF00E5A0.toInt(),
            totalExpMonthly, "Uscite totali", 0xFFFF4081.toInt(),
            Constants.MONTHS_SHORT
        )
    }

    // ── Trend Patrimonio Netto ─────────────────────────────────────────────────
    private fun updatePatrimonio() {
        val savings     = viewModel.savingsByYear.value     ?: return
        val debts       = viewModel.debtsByYear.value       ?: return
        val withdrawals = viewModel.withdrawalsByYear.value ?: emptyList()

        // Accumulo progressivo mese per mese
        var cumSavings = 0.0
        var cumDebts   = 0.0

        val savingsEntries  = mutableListOf<Entry>()
        val debtsEntries    = mutableListOf<Entry>()
        val netEntries      = mutableListOf<Entry>()

        for (m in 1..12) {
            val mSavings = savings.filter { it.month == m }.sumOf { it.amount }
            val mDebts   = debts.filter { it.month == m }.sumOf { it.amount }
            val mWd      = withdrawals.filter {
                it.date.length >= 7 && it.date.substring(5, 7).toIntOrNull() == m
            }.sumOf { it.amount }

            cumSavings += mSavings - mWd   // i prelievi riducono i risparmi effettivi
            cumDebts   += mDebts
            val net = cumSavings - cumDebts

            savingsEntries.add(Entry((m - 1).toFloat(), cumSavings.toFloat()))
            debtsEntries.add(Entry((m - 1).toFloat(), cumDebts.toFloat()))
            netEntries.add(Entry((m - 1).toFloat(), net.toFloat()))
        }

        // KPI finali (dicembre)
        val finalSavings = savingsEntries.last().y.toDouble()
        val finalDebts   = debtsEntries.last().y.toDouble()
        val finalNet     = netEntries.last().y.toDouble()

        binding.tvPatrimonioRisparmi.text = finalSavings.toEuro()
        binding.tvPatrimonioDebiti.text   = finalDebts.toEuro()
        binding.tvPatrimonioNetto.text    = finalNet.toEuro()
        binding.tvPatrimonioNetto.setTextColor(
            if (finalNet >= 0) Color.parseColor("#FFD600") else Color.parseColor("#FF4081"))

        // Dataset linee
        fun makeDataSet(entries: List<Entry>, label: String, color: Int): LineDataSet =
            LineDataSet(entries, label).apply {
                this.color = color
                setCircleColor(color)
                circleRadius    = 3f
                lineWidth       = 2.5f
                mode            = LineDataSet.Mode.CUBIC_BEZIER
                setDrawValues(false)
                setDrawFilled(true)
                fillAlpha       = 20
                fillColor       = color
            }

        val datasets: List<ILineDataSet> = listOf(
            makeDataSet(savingsEntries, "Risparmi",        Color.parseColor("#00E5A0")),
            makeDataSet(debtsEntries,   "Debiti",          Color.parseColor("#FF4081")),
            makeDataSet(netEntries,     "Patrimonio netto",Color.parseColor("#FFD600"))
        )

        binding.lineChartPatrimonio.data = LineData(datasets)
        binding.lineChartPatrimonio.notifyDataSetChanged()
        binding.lineChartPatrimonio.invalidate()
        binding.lineChartPatrimonio.animateX(800)
    }

    private fun styleLineChart() {
        binding.lineChartPatrimonio.apply {
            description.isEnabled  = false
            legend.isEnabled       = false
            setTouchEnabled(true)
            setScaleEnabled(false)
            setBackgroundColor(Color.TRANSPARENT)
            xAxis.apply {
                valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(Constants.MONTHS_SHORT)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = Color.parseColor("#7A90BB")
                granularity = 1f
            }
            axisLeft.apply {
                textColor = Color.parseColor("#7A90BB")
                setDrawGridLines(true)
                gridColor = Color.parseColor("#1AFFFFFF")
                axisLineColor = Color.TRANSPARENT
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float) =
                        if (value >= 1000) "${"%.0f".format(value / 1000)}k €" else "${value.toInt()} €"
                }
            }
            axisRight.isEnabled = false
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
