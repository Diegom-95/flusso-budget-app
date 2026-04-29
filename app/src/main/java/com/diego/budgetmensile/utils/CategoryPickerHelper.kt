package com.diego.budgetmensile.utils

import android.content.Context
import android.widget.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

object CategoryPickerHelper {

    private const val ADD_NEW = "＋  Nuova categoria..."

    /**
     * Configura l'AutoCompleteTextView con la lista categorie per [type].
     * Il bottone [manageBtn] (ImageButton con icona matita) apre il gestore
     * modifica/elimina categorie. Passa null se non vuoi il bottone.
     */
    fun setup(
        ctx: Context,
        actv: AutoCompleteTextView,
        manageBtn: ImageButton?,
        type: String,
        initial: String?,
        onCategorySelected: (String) -> Unit
    ) {
        var selected = initial ?: CategoryManager.getCategories(ctx, type).firstOrNull() ?: ""

        fun buildItems() = CategoryManager.getCategories(ctx, type) + ADD_NEW

        fun refresh() {
            actv.setAdapter(
                ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, buildItems())
            )
            actv.setText(selected, false)
            onCategorySelected(selected)
        }

        fun showAddDialog() {
            val til = makeInputLayout(ctx, "Nome categoria")
            val et = til.editText as TextInputEditText
            MaterialAlertDialogBuilder(ctx)
                .setTitle("Nuova categoria")
                .setView(til)
                .setPositiveButton("Aggiungi") { _, _ ->
                    val newCat = et.text.toString().trim()
                    if (newCat.isNotEmpty()) {
                        CategoryManager.addCategory(ctx, type, newCat)
                        selected = newCat
                        refresh()
                    } else {
                        actv.setText(selected, false)
                    }
                }
                .setNegativeButton("Annulla") { _, _ -> actv.setText(selected, false) }
                .show()
        }

        fun showManageDialog() {
            val cats = CategoryManager.getCategories(ctx, type)
            if (cats.isEmpty()) {
                Toast.makeText(ctx, "Nessuna categoria da gestire", Toast.LENGTH_SHORT).show()
                return
            }
            val labels = cats.toTypedArray()
            MaterialAlertDialogBuilder(ctx)
                .setTitle("Gestisci categorie")
                .setItems(labels) { _, which ->
                    val category = cats[which]
                    MaterialAlertDialogBuilder(ctx)
                        .setTitle(category)
                        .setItems(arrayOf("✏️  Rinomina", "🗑️  Elimina")) { _, action ->
                            when (action) {
                                0 -> { // Rinomina
                                    val til = makeInputLayout(ctx, "Nuovo nome")
                                    val et = til.editText as TextInputEditText
                                    et.setText(category); et.selectAll()
                                    MaterialAlertDialogBuilder(ctx)
                                        .setTitle("Rinomina categoria")
                                        .setView(til)
                                        .setPositiveButton("Salva") { _, _ ->
                                            val newName = et.text.toString().trim()
                                            if (newName.isNotEmpty() && newName != category) {
                                                CategoryManager.renameCategory(ctx, type, category, newName)
                                                if (selected == category) selected = newName
                                                refresh()
                                            }
                                        }
                                        .setNegativeButton("Annulla", null)
                                        .show()
                                }
                                1 -> { // Elimina
                                    MaterialAlertDialogBuilder(ctx)
                                        .setTitle("Elimina categoria")
                                        .setMessage("Eliminare \"$category\"?")
                                        .setPositiveButton("Elimina") { _, _ ->
                                            CategoryManager.removeCategory(ctx, type, category)
                                            if (selected == category)
                                                selected = CategoryManager.getCategories(ctx, type)
                                                    .firstOrNull() ?: ""
                                            refresh()
                                        }
                                        .setNegativeButton("Annulla", null)
                                        .show()
                                }
                            }
                        }
                        .show()
                }
                .setNegativeButton("Chiudi", null)
                .show()
        }

        actv.setOnItemClickListener { _, _, pos, _ ->
            val chosen = buildItems()[pos]
            if (chosen == ADD_NEW) showAddDialog()
            else { selected = chosen; onCategorySelected(selected) }
        }

        manageBtn?.setOnClickListener { showManageDialog() }

        refresh()
    }

    // Crea un TextInputLayout Material3 con padding corretto per i dialog
    private fun makeInputLayout(ctx: Context, hint: String): TextInputLayout {
        val til = TextInputLayout(ctx).apply {
            this.hint = hint
            setPadding(48, 24, 48, 8)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        }
        val et = TextInputEditText(ctx).apply { setSingleLine() }
        til.addView(et)
        return til
    }
}
