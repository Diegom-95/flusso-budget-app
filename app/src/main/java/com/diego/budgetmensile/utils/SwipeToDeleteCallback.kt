package com.diego.budgetmensile.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ListAdapter
import com.google.android.material.snackbar.Snackbar

/**
 * Swipe-to-delete generico con snackbar ANNULLA (5 secondi).
 *
 * Uso:
 *   SwipeToDeleteCallback.attach(recyclerView, rootView) { position ->
 *       val item = adapter.currentList[position]
 *       viewModel.delete(item)
 *       // La lambda viene chiamata solo se l'utente non preme ANNULLA
 *   }
 */
class SwipeToDeleteCallback<T>(
    private val adapter: () -> List<T>,
    private val rootView: View,
    private val itemLabel: (T) -> String,
    private val onDelete: (T) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val item = adapter()[position]
        val label = itemLabel(item)

        // Chiedi conferma tramite snackbar invece di eliminare subito
        var deleted = false
        val snack = Snackbar.make(rootView, "\"$label\" eliminato", Snackbar.LENGTH_LONG)
            .setDuration(5000)
            .setAction("ANNULLA") {
                // L'utente ha annullato — ripristina la riga
                (rootView.parent as? RecyclerView)?.adapter?.notifyItemChanged(position)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(sb: Snackbar?, event: Int) {
                    if (event != DISMISS_EVENT_ACTION && !deleted) {
                        deleted = true
                        onDelete(item)
                    }
                }
            })
        snack.show()
    }

    override fun onChildDraw(c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder,
                              dX: Float, dY: Float, actionState: Int, isActive: Boolean) {
        val itemView = vh.itemView
        val bgPaint  = Paint().apply { color = Color.parseColor("#CC2244") }
        val txtPaint = Paint().apply {
            color = Color.WHITE; textSize = 42f
            isAntiAlias = true; textAlign = Paint.Align.RIGHT
        }
        // Sfondo rosso
        c.drawRect(
            itemView.right + dX, itemView.top.toFloat(),
            itemView.right.toFloat(), itemView.bottom.toFloat(), bgPaint
        )
        // Testo 🗑 Elimina
        val textY = itemView.top + (itemView.height / 2f) + (txtPaint.textSize / 3f)
        c.drawText("🗑 Elimina  ", itemView.right.toFloat(), textY, txtPaint)
        super.onChildDraw(c, rv, vh, dX, dY, actionState, isActive)
    }

    companion object {
        fun <T> attach(
            recyclerView: RecyclerView,
            rootView: View,
            getList: () -> List<T>,
            itemLabel: (T) -> String,
            onDelete: (T) -> Unit
        ) {
            val cb = SwipeToDeleteCallback(getList, rootView, itemLabel, onDelete)
            ItemTouchHelper(cb).attachToRecyclerView(recyclerView)
        }
    }
}
