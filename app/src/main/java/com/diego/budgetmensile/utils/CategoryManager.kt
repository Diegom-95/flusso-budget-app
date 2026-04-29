package com.diego.budgetmensile.utils

import android.content.Context

/**
 * Gestisce le categorie utente salvate in SharedPreferences.
 * Nessuna categoria di default: l'utente parte da zero e può
 * aggiungere, rinominare ed eliminare ogni categoria.
 */
object CategoryManager {

    private const val PREFS_NAME = "budget_categories"

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Restituisce le categorie salvate per il tipo dato, ordinate alfabeticamente */
    fun getCategories(ctx: Context, type: String): List<String> =
        prefs(ctx).getStringSet("cats_$type", emptySet())
            ?.toList()?.sorted() ?: emptyList()

    /** Aggiunge una nuova categoria */
    fun addCategory(ctx: Context, type: String, name: String) {
        val p = prefs(ctx)
        val current = p.getStringSet("cats_$type", mutableSetOf())!!.toMutableSet()
        current.add(name.trim())
        p.edit().putStringSet("cats_$type", current).apply()
    }

    /** Rinomina una categoria esistente (aggiorna la chiave) */
    fun renameCategory(ctx: Context, type: String, oldName: String, newName: String) {
        val p = prefs(ctx)
        val current = p.getStringSet("cats_$type", mutableSetOf())!!.toMutableSet()
        current.remove(oldName)
        current.add(newName.trim())
        p.edit().putStringSet("cats_$type", current).apply()
    }

    /** Elimina una categoria */
    fun removeCategory(ctx: Context, type: String, name: String) {
        val p = prefs(ctx)
        val current = p.getStringSet("cats_$type", mutableSetOf())!!.toMutableSet()
        current.remove(name)
        p.edit().putStringSet("cats_$type", current).apply()
    }
}
