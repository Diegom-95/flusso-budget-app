package com.diego.budgetmensile.ui.expenses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ListAdapter
import com.diego.budgetmensile.data.entity.Expense
import com.diego.budgetmensile.databinding.ItemExpenseBinding
import com.diego.budgetmensile.utils.toDisplayDate
import com.diego.budgetmensile.utils.toEuro

class ExpenseAdapter(
    private val onEdit:   (Expense) -> Unit,
    private val onDelete: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.VH>(DiffCb()) {

    inner class VH(private val b: ItemExpenseBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Expense) {
            b.tvCategory.text    = item.category
            b.tvDescription.text = item.description.ifBlank { item.category }
            b.tvAmount.text      = "-${item.amount.toEuro()}"
            b.tvDate.text        = item.date.toDisplayDate()
            b.chipSub.visibility = if (item.isSubscription) android.view.View.VISIBLE else android.view.View.GONE
            b.chipNeed.visibility = if (item.isNeed) android.view.View.VISIBLE else android.view.View.GONE
            b.tvNotes.text       = item.notes
            b.tvNotes.visibility = if (item.notes.isBlank()) android.view.View.GONE else android.view.View.VISIBLE
            b.root.setOnClickListener { onEdit(item) }
            b.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))

    class DiffCb : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(a: Expense, b: Expense) = a.id == b.id
        override fun areContentsTheSame(a: Expense, b: Expense) = a == b
    }
}
