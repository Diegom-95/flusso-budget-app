package com.diego.budgetmensile.utils

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Formatta un Double come valuta EUR, es: 1234.5 → "1.234,50 €" */
fun Double.toEuro(): String {
    val fmt = NumberFormat.getCurrencyInstance(Locale.ITALY)
    return fmt.format(this)
}

/** Restituisce la data odierna in formato "yyyy-MM-dd" */
fun todayString(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).format(Date())

/** Converte "yyyy-MM-dd" in "dd/MM/yyyy" per la visualizzazione */
fun String.toDisplayDate(): String = try {
    val src = SimpleDateFormat("yyyy-MM-dd", Locale.ITALY)
    val dst = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
    dst.format(src.parse(this)!!)
} catch (e: Exception) { this }

/** Estrae mese (1-12) da una stringa "yyyy-MM-dd" */
fun String.extractMonth(): Int = try {
    this.substring(5, 7).toInt()
} catch (e: Exception) { 1 }

/** Estrae anno da una stringa "yyyy-MM-dd" */
fun String.extractYear(): Int = try {
    this.substring(0, 4).toInt()
} catch (e: Exception) { 2026 }

/**
 * Imposta il titolo della ActionBar con "Mese Anno" (es. "Marzo 2026").
 * Da chiamare in onResume() di ogni Fragment che mostra dati mensili.
 */
fun Fragment.setToolbarMonthTitle(month: Int, year: Int) {
    val monthName = Constants.MONTHS.getOrElse(month - 1) { "?" }
    (requireActivity() as? AppCompatActivity)
        ?.supportActionBar?.title = "$monthName $year"
}

// Palette tutta sui toni del verde — coerente con il colore entrate
private val CHIP_PALETTE_GREEN = listOf(
    0xFF00C389, // verde principale (color_income)
    0xFF00E5A0, // verde chiaro
    0xFF64DD17, // verde lime
    0xFF00BFA5, // verde acqua
    0xFF1DE9B6, // turchese chiaro
    0xFF76FF03, // verde giallo
    0xFF00897B, // verde scuro
    0xFF43A047, // verde medio
    0xFF00ACC1, // ciano verde
    0xFF9CCC65, // verde salvia
    0xFF26A69A, // verde acquamarina
    0xFF66BB6A  // verde morbido
).map { it.toInt() }

// Palette tutta sui toni del giallo/oro — coerente con il colore risparmi
private val CHIP_PALETTE_YELLOW = listOf(
    0xFFC9A800, // giallo principale (color_saving)
    0xFFFFD600, // giallo puro
    0xFFFFCA28, // ambra chiara
    0xFFFFB300, // ambra
    0xFFFFA000, // ambra scura
    0xFFFF8F00, // arancio ambra
    0xFFFFE57F, // giallo pallido
    0xFFFFD740, // giallo acceso
    0xFFF9A825, // giallo ocra
    0xFFF57F17, // giallo bruciato
    0xFFE6AC00, // oro scuro
    0xFFFFCC02  // giallo limone
).map { it.toInt() }

private val CHIP_PALETTE = CHIP_PALETTE_GREEN // default

/**
 * Applica colore di sfondo (semi-trasparente) e testo al Chip
 * in modo coerente: stessa categoria → stesso colore in tutta l'app.
 */
fun Chip.styleForCategory(category: String, isSelected: Boolean = false) {
    applyChipStyle(category, isSelected, CHIP_PALETTE_GREEN)
}

/** Variante gialla per i chip dei risparmi. */
fun Chip.styleForSavingCategory(category: String, isSelected: Boolean = false) {
    applyChipStyle(category, isSelected, CHIP_PALETTE_YELLOW)
}

private fun Chip.applyChipStyle(category: String, isSelected: Boolean, palette: List<Int>) {
    val base  = palette[Math.abs(category.hashCode()) % palette.size]
    val alpha = if (isSelected) 0xFF else 0x33
    val bg    = Color.argb(alpha, Color.red(base), Color.green(base), Color.blue(base))
    chipBackgroundColor = android.content.res.ColorStateList.valueOf(bg)
    setTextColor(if (isSelected) Color.WHITE else base)
}
