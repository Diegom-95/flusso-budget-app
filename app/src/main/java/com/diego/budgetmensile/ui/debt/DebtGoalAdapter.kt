package com.diego.budgetmensile.ui.debt

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ListAdapter
import com.diego.budgetmensile.R
import com.diego.budgetmensile.data.entity.DebtGoal
import com.diego.budgetmensile.utils.toEuro
import kotlin.math.ceil

class DebtGoalAdapter(
    private val onPay:    (DebtGoal) -> Unit,
    private val onEdit:   (DebtGoal) -> Unit,
    private val onDelete: (DebtGoal) -> Unit
) : RecyclerView.Adapter<DebtGoalAdapter.VH>() {

    private var goals   = listOf<DebtGoal>()
    private var paidMap = mapOf<String, Double>()

    fun submitGoals(g: List<DebtGoal>, paid: Map<String, Double>) {
        goals = g; paidMap = paid; notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName        = v.findViewById<TextView>(R.id.tvDebtName)
        val tvTotal       = v.findViewById<TextView>(R.id.tvDebtTotal)
        val tvPaid        = v.findViewById<TextView>(R.id.tvDebtPaid)
        val tvRemaining   = v.findViewById<TextView>(R.id.tvDebtRemaining)
        val tvMonthly     = v.findViewById<TextView>(R.id.tvDebtMonthly)
        val tvMonthsLeft  = v.findViewById<TextView>(R.id.tvMonthsLeft)
        val tvStatus      = v.findViewById<TextView>(R.id.tvDebtStatus)
        val progressBar   = v.findViewById<ProgressBar>(R.id.progressDebt)
        val btnPay        = v.findViewById<Button>(R.id.btnPay)
        val btnEdit       = v.findViewById<ImageButton>(R.id.btnEditDebt)
        val btnDelete     = v.findViewById<ImageButton>(R.id.btnDeleteDebt)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_debt_goal, p, false))

    override fun getItemCount() = goals.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val goal   = goals[pos]
        val paid   = paidMap[goal.name] ?: 0.0
        val left   = (goal.totalAmount - paid).coerceAtLeast(0.0)
        val pct    = if (goal.totalAmount > 0) (paid / goal.totalAmount * 100).toInt() else 0
        val mLeft  = if (goal.monthlyPayment > 0) ceil(left / goal.monthlyPayment).toInt() else 0

        h.tvName.text       = goal.name
        h.tvTotal.text      = "Totale: ${goal.totalAmount.toEuro()}"
        h.tvPaid.text       = "Pagato: ${paid.toEuro()}"
        h.tvRemaining.text  = "Rimanente: ${left.toEuro()}"
        h.tvMonthly.text    = "Rata: ${goal.monthlyPayment.toEuro()}/mese"
        h.tvMonthsLeft.text = if (left <= 0) "✅ Estinto!" else "~$mLeft rate rimanenti"
        h.tvStatus.text     = "$pct%"
        h.tvStatus.setTextColor(
            if (pct >= 100) 0xFF00E5A0.toInt() else 0xFF9C6FFF.toInt()
        )
        h.progressBar.progress = pct.coerceIn(0, 100)
        h.btnPay.isEnabled  = goal.isActive && left > 0
        h.btnPay.alpha      = if (h.btnPay.isEnabled) 1f else 0.4f
        h.btnPay.setOnClickListener    { onPay(goal) }
        h.btnEdit.setOnClickListener   { onEdit(goal) }
        h.btnDelete.setOnClickListener { onDelete(goal) }
    }
}
