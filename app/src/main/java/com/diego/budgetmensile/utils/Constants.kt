package com.diego.budgetmensile.utils

object Constants {

    // ── Categorie Entrate ──────────────────────────────────────────────────────
    val INCOME_CATEGORIES = listOf(
        "Stipendio", "Paypal", "Rimborsi", "Regali",
        "Investimenti", "Trasf. da altra banca", "Tredicesima", "Quattordicesima"
    )

    // ── Categorie Spese ────────────────────────────────────────────────────────
    val EXPENSE_CATEGORIES = listOf(
        "Casa", "Auto", "Bancomat", "Ristorante", "Abbonamenti",
        "Benzina", "Regali", "Schedline", "Shopping", "Caffè Lavoro",
        "Commissioni", "Prestiti", "Formazione", "Salute", "Vacanze",
        "Unipol Move", "Svago", "Altro"
    )

    // Spese "necessarie" (Need = true)
    val EXPENSE_NEEDS = setOf(
        "Casa", "Bancomat", "Benzina", "Commissioni",
        "Formazione", "Salute", "Unipol Move"
    )

    // ── Categorie Risparmi ─────────────────────────────────────────────────────
    val SAVINGS_CATEGORIES = listOf(
        "Revolut Macchina", "Investimenti", "Conto Bper",
        "Fondo Revolut Vacanza", "Fondo Sanitario"
    )

    // Risparmi a lungo termine
    val SAVINGS_LONG_TERM = setOf("Investimenti")

    // ── Categorie Debiti ───────────────────────────────────────────────────────
    val DEBT_CATEGORIES = listOf<String>()  // Personalizzabili dall'utente

    // ── Mesi ───────────────────────────────────────────────────────────────────
    val MONTHS = listOf(
        "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
        "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"
    )

    val MONTHS_SHORT = listOf(
        "Gen", "Feb", "Mar", "Apr", "Mag", "Giu",
        "Lug", "Ago", "Set", "Ott", "Nov", "Dic"
    )

    // ── Tipi di transazione ────────────────────────────────────────────────────
    const val TYPE_EXPENSE = "EXPENSE"
    const val TYPE_INCOME  = "INCOME"
    const val TYPE_SAVING  = "SAVING"
    const val TYPE_DEBT    = "DEBT"
}
