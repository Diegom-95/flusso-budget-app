package com.diego.budgetmensile.utils

import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

object ChartHelper {

    val CHART_COLORS = intArrayOf(
        0xFFFF4081.toInt(), 0xFF00E5A0.toInt(), 0xFFFFD600.toInt(),
        0xFF7C4DFF.toInt(), 0xFF00B0FF.toInt(), 0xFFFF6D00.toInt(),
        0xFFE040FB.toInt(), 0xFF64DD17.toInt(), 0xFFF50057.toInt(),
        0xFF00BFA5.toInt(), 0xFFFF9100.toInt(), 0xFF82B1FF.toInt()
    )

    private val BG_COLOR        = 0xFF162035.toInt()
    private val TEXT_COLOR      = 0xFFFFFFFF.toInt()
    private val TEXT_SECONDARY  = 0xFF7A90BB.toInt()
    private val GRID_COLOR      = 0x331E3050.toInt()

    fun stylePieChart(chart: PieChart, holeColor: Int = BG_COLOR) {
        chart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 58f
            transparentCircleRadius = 62f
            setHoleColor(holeColor)
            setTransparentCircleColor(holeColor)
            setTransparentCircleAlpha(80)
            // MPAndroidChart calcola le % internamente dai valori grezzi
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            legend.apply {
                isEnabled = true
                textColor = TEXT_COLOR
                textSize = 11f
                form = Legend.LegendForm.CIRCLE
                formSize = 10f
                xEntrySpace = 12f
                yEntrySpace = 4f
                isWordWrapEnabled = true
            }
            setTouchEnabled(true)
        }
    }

    /**
     * Costruisce il PieData passando i valori grezzi (€) a MPAndroidChart,
     * che calcola le percentuali internamente in modo corretto.
     * @param rawValues mappa categoria → importo grezzo
     */
    fun buildPieDataFromMap(rawValues: Map<String, Double>): PieData {
        val entries = rawValues.entries
            .sortedByDescending { it.value }
            .map { (label, value) -> PieEntry(value.toFloat(), label) }

        val ds = PieDataSet(entries, "").apply {
            val colorList = ArrayList<Int>()
            for (i in entries.indices) colorList.add(CHART_COLORS[i % CHART_COLORS.size])
            colors = colorList
            valueTextSize = 10f
            valueTextColor = TEXT_COLOR
            sliceSpace = 2f
            selectionShift = 6f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float) =
                    if (value >= 1f) "%.1f%%".format(value) else ""
            }
        }
        return PieData(ds)
    }

    /** Imposta dati + animazione sul pie chart, cancellando prima i dati vecchi */
    fun setPieData(chart: PieChart, rawValues: Map<String, Double>) {
        if (rawValues.isEmpty() || rawValues.values.sum() == 0.0) {
            chart.clear()
            chart.invalidate()
            return
        }
        chart.data = buildPieDataFromMap(rawValues)
        chart.notifyDataSetChanged()
        chart.invalidate()
        chart.animateY(700)
    }

    /** Bar chart (mensile/categorie) */
    fun styleBarChart(chart: BarChart, xLabels: List<String> = emptyList()) {
        chart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setFitBars(true)
            setScaleEnabled(false)
            setPinchZoom(false)
            legend.apply {
                isEnabled = true
                textColor = TEXT_COLOR
                textSize = 10f
            }
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = TEXT_SECONDARY
                textSize = 10f
                granularity = 1f
                if (xLabels.isNotEmpty()) {
                    valueFormatter = IndexAxisValueFormatter(xLabels)
                    labelCount = xLabels.size
                }
            }
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = GRID_COLOR
                textColor = TEXT_SECONDARY
                textSize = 10f
                axisLineColor = GRID_COLOR
                axisMinimum = 0f   // ← forza partenza da 0
            }
            axisRight.isEnabled = false
        }
    }

    fun setBarData(chart: BarChart, rawValues: Map<String, Double>, color: Int = CHART_COLORS[0]) {
        if (rawValues.isEmpty()) { chart.clear(); chart.invalidate(); return }
        val sorted  = rawValues.entries.sortedByDescending { it.value }
        val entries = sorted.mapIndexed { i, e -> BarEntry(i.toFloat(), e.value.toFloat()) }
        val labels  = sorted.map { it.key.take(7) }

        val ds = BarDataSet(entries, "").apply {
            val colorList = ArrayList<Int>()
            for (i in entries.indices) colorList.add(CHART_COLORS[i % CHART_COLORS.size])
            colors = colorList
            setDrawValues(false)
        }
        chart.data = BarData(ds).apply { barWidth = 0.6f }
        styleBarChart(chart, labels)
        chart.notifyDataSetChanged()
        chart.invalidate()
        chart.animateY(700)
    }

    /** Bar chart raggruppato (es. Entrate vs Spese per mese) */
    fun setGroupedBarData(
        chart: BarChart,
        series1: List<Double>, label1: String, color1: Int,
        series2: List<Double>, label2: String, color2: Int,
        xLabels: List<String>
    ) {
        val e1 = series1.mapIndexed { i, v -> BarEntry(i.toFloat(), v.toFloat()) }
        val e2 = series2.mapIndexed { i, v -> BarEntry(i.toFloat(), v.toFloat()) }
        val ds1 = BarDataSet(e1, label1).apply { this.color = color1; setDrawValues(false) }
        val ds2 = BarDataSet(e2, label2).apply { this.color = color2; setDrawValues(false) }
        val data = BarData(ds1, ds2).apply {
            barWidth = 0.35f
            groupBars(-0.5f, 0.1f, 0.05f)
        }
        chart.data = data
        styleBarChart(chart, xLabels)
        chart.axisLeft.axisMinimum = 0f
        chart.xAxis.apply {
            axisMinimum = -0.5f
            axisMaximum = xLabels.size.toFloat() - 0.5f
            setCenterAxisLabels(true)
        }
        chart.notifyDataSetChanged()
        chart.invalidate()
        chart.animateY(700)
    }

    /** Line chart (spese giornaliere) */
    fun styleLineChart(chart: LineChart, xLabels: List<String> = emptyList()) {
        chart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            legend.apply {
                isEnabled = true
                textColor = TEXT_COLOR
                textSize = 10f
            }
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = TEXT_SECONDARY
                textSize = 10f
                granularity = 5f
                if (xLabels.isNotEmpty()) valueFormatter = IndexAxisValueFormatter(xLabels)
            }
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = GRID_COLOR
                textColor = TEXT_SECONDARY
                textSize = 10f
                axisLineColor = GRID_COLOR
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
        }
    }

    fun buildLineDataSet(entries: List<Entry>, label: String, color: Int): LineDataSet {
        return LineDataSet(entries, label).apply {
            this.color = color
            lineWidth = 2.5f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = color
            fillAlpha = 30
            isHighlightEnabled = true
            highLightColor = 0x88FFFFFF.toInt()
        }
    }

    fun buildDashedLineDataSet(entries: List<Entry>, label: String, color: Int): LineDataSet {
        return LineDataSet(entries, label).apply {
            this.color = color
            lineWidth = 1.5f
            setDrawCircles(false)
            setDrawValues(false)
            enableDashedLine(10f, 8f, 0f)
            setDrawFilled(false)
        }
    }

    /** Bar chart a 3 serie (Entrate/Spese/Risparmi per mese) */
    fun setTripleBarData(
        chart: BarChart,
        s1: List<Double>, l1: String, c1: Int,
        s2: List<Double>, l2: String, c2: Int,
        s3: List<Double>, l3: String, c3: Int,
        xLabels: List<String>
    ) {
        val e1 = s1.mapIndexed { i, v -> BarEntry(i.toFloat(), v.toFloat()) }
        val e2 = s2.mapIndexed { i, v -> BarEntry(i.toFloat(), v.toFloat()) }
        val e3 = s3.mapIndexed { i, v -> BarEntry(i.toFloat(), v.toFloat()) }
        val ds1 = BarDataSet(e1, l1).apply { this.color = c1; setDrawValues(false) }
        val ds2 = BarDataSet(e2, l2).apply { this.color = c2; setDrawValues(false) }
        val ds3 = BarDataSet(e3, l3).apply { this.color = c3; setDrawValues(false) }
        val data = BarData(ds1, ds2, ds3).apply {
            barWidth = 0.25f
            groupBars(-0.5f, 0.08f, 0.02f)
        }
        chart.data = data
        styleBarChart(chart, xLabels)
        chart.axisLeft.axisMinimum = 0f
        chart.xAxis.apply {
            axisMinimum = -0.5f
            axisMaximum = xLabels.size.toFloat() - 0.5f
            setCenterAxisLabels(true)
        }
        chart.notifyDataSetChanged()
        chart.invalidate()
        chart.animateY(700)
    }
}
