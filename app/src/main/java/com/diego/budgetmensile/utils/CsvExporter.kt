package com.diego.budgetmensile.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.diego.budgetmensile.data.BudgetRepository
import com.diego.budgetmensile.data.entity.*
import java.io.File
import java.io.FileOutputStream

object CsvExporter {

    fun exportMonth(
        context: Context, expenses: List<Expense>, incomes: List<Income>,
        savings: List<Saving>, month: Int, year: Int
    ) {
        val monthName = Constants.MONTHS_SHORT.getOrElse(month - 1) { "M$month" }
        val sb = StringBuilder()
        sb.appendLine("Tipo;Data;Descrizione;Categoria;Importo;Note;Flags")
        expenses.forEach { e ->
            val f = listOfNotNull(if (e.isSubscription) "Abbonamento" else null, if (e.isNeed) "Necessità" else null).joinToString("|")
            sb.appendLine("Spesa;${e.date};${e.description.safe()};${e.category.safe()};${e.amount.csv()};${e.notes.safe()};$f")
        }
        incomes.forEach { i -> sb.appendLine("Entrata;${i.date};${i.description.safe()};${i.category.safe()};${i.amount.csv()};${i.notes.safe()};") }
        savings.forEach { s -> sb.appendLine("Risparmio;${s.date};;${s.category.safe()};${s.amount.csv()};${s.notes.safe()};") }
        saveToDownloads(context, sb.toString(), "Flusso_${monthName}_${year}.csv")
    }

    suspend fun exportAllSuspend(context: Context) {
        val repo = BudgetRepository(context)
        val sb   = StringBuilder()
        sb.appendLine("Tipo;Anno;Mese;Data;Descrizione;Categoria;Importo;Note;Flags")

        for (y in 2020..2030) {
            repo.getExpensesByYearSync(y).forEach { e ->
                val f = listOfNotNull(if (e.isSubscription) "Abbonamento" else null, if (e.isNeed) "Necessità" else null).joinToString("|")
                sb.appendLine("Spesa;${e.year};${e.month};${e.date};${e.description.safe()};${e.category.safe()};${e.amount.csv()};${e.notes.safe()};$f")
            }
            repo.getIncomesByYearSync(y).forEach { i ->
                sb.appendLine("Entrata;${i.year};${i.month};${i.date};${i.description.safe()};${i.category.safe()};${i.amount.csv()};${i.notes.safe()};")
            }
            repo.getSavingsByYearSync(y).forEach { s ->
                sb.appendLine("Risparmio;${s.year};${s.month};${s.date};;${s.category.safe()};${s.amount.csv()};${s.notes.safe()};${if (s.isLongTerm) "LungoPeriodo" else ""}")
            }
            repo.getDebtsByYearSync(y).forEach { d ->
                sb.appendLine("Debito;${d.year};${d.month};${d.date};${d.description.safe()};${d.category.safe()};${d.amount.csv()};${d.notes.safe()};${if (d.isHighPriority) "AltaPriorità" else ""}")
            }
        }

        // Obiettivi di debito — servono per visualizzare i debiti nella UI
        repo.getAllDebtGoalsSync().forEach { g ->
            sb.appendLine("ObiettivoDebito;;;;${g.name.safe()};${g.startDate};${g.totalAmount.csv()};${g.monthlyPayment.csv()};${if (g.isActive) "Attivo" else "Chiuso"}|${g.notes.safe()}")
        }

        repo.getAllWithdrawalsSync().forEach { w ->
            sb.appendLine("Prelievo;;;;;${w.goalName.safe()};${w.amount.csv()};${w.notes.safe()};${w.description.safe()}")
        }

        repo.getAllBudgetGoals().forEach { g ->
            sb.appendLine("Budget;${g.year};${g.month};;${g.type};${g.category.safe()};${g.goal.csv()};;")
        }

        saveToDownloads(context, sb.toString(), "Flusso_export_completo.csv")
    }

    /**
     * Genera il CSV completo (stesso contenuto di exportAllSuspend) e apre
     * il selettore di condivisione Android — l'utente può scegliere
     * Google Drive, Gmail, WhatsApp ecc. Nessuna API key richiesta.
     */
    suspend fun exportAllAndShareToDrive(context: Context) {
        val repo = BudgetRepository(context)
        val sb   = StringBuilder()
        sb.appendLine("Tipo;Anno;Mese;Data;Descrizione;Categoria;Importo;Note;Flags")

        for (y in 2020..2030) {
            repo.getExpensesByYearSync(y).forEach { e ->
                val f = listOfNotNull(if (e.isSubscription) "Abbonamento" else null, if (e.isNeed) "Necessità" else null).joinToString("|")
                sb.appendLine("Spesa;${e.year};${e.month};${e.date};${e.description.safe()};${e.category.safe()};${e.amount.csv()};${e.notes.safe()};$f")
            }
            repo.getIncomesByYearSync(y).forEach { i ->
                sb.appendLine("Entrata;${i.year};${i.month};${i.date};${i.description.safe()};${i.category.safe()};${i.amount.csv()};${i.notes.safe()};")
            }
            repo.getSavingsByYearSync(y).forEach { s ->
                sb.appendLine("Risparmio;${s.year};${s.month};${s.date};;${s.category.safe()};${s.amount.csv()};${s.notes.safe()};${if (s.isLongTerm) "LungoPeriodo" else ""}")
            }
            repo.getDebtsByYearSync(y).forEach { d ->
                sb.appendLine("Debito;${d.year};${d.month};${d.date};${d.description.safe()};${d.category.safe()};${d.amount.csv()};${d.notes.safe()};${if (d.isHighPriority) "AltaPriorità" else ""}")
            }
        }
        repo.getAllDebtGoalsSync().forEach { g ->
            sb.appendLine("ObiettivoDebito;;;;${g.name.safe()};${g.startDate};${g.totalAmount.csv()};${g.monthlyPayment.csv()};${if (g.isActive) "Attivo" else "Chiuso"}|${g.notes.safe()}")
        }
        repo.getAllWithdrawalsSync().forEach { w ->
            sb.appendLine("Prelievo;;;;;${w.goalName.safe()};${w.amount.csv()};${w.notes.safe()};${w.description.safe()}")
        }
        repo.getAllBudgetGoals().forEach { g ->
            sb.appendLine("Budget;${g.year};${g.month};;${g.type};${g.category.safe()};${g.goal.csv()};;")
        }

        try {
            // Scrivi in cache (FileProvider-compatibile, nessuna permission richiesta)
            val fileName = "Flusso_export_completo.csv"
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.writeText(sb.toString(), Charsets.UTF_8)

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context, "${context.packageName}.provider", cacheFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Flusso – Export completo")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Condividi su…"))
        } catch (e: Exception) {
            Toast.makeText(context, "Errore export: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveToDownloads(context: Context, content: String, fileName: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { it.write(content.toByteArray(Charsets.UTF_8)) }
                    values.clear(); values.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    Toast.makeText(context, "✅ Salvato in Download/$fileName", Toast.LENGTH_LONG).show()
                }
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                dir.mkdirs()
                FileOutputStream(File(dir, fileName)).use { it.write(content.toByteArray(Charsets.UTF_8)) }
                Toast.makeText(context, "✅ Salvato in Download/$fileName", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Errore: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    suspend fun parseCsvDedup(context: Context, content: String): ImportResult {
        val repo        = BudgetRepository(context)
        val expenses    = mutableListOf<Expense>()
        val incomes     = mutableListOf<Income>()
        val savings     = mutableListOf<Saving>()
        val debts       = mutableListOf<Debt>()
        val debtGoals   = mutableListOf<DebtGoal>()
        val budgetGoals = mutableListOf<BudgetGoal>()
        var errors = 0; var skipped = 0

        val lines = content.lines().drop(1).filter { it.isNotBlank() }
        for (line in lines) {
            try {
                val cols   = line.split(";")
                if (cols.size < 7) { errors++; continue }
                val type   = cols[0].trim()
                val year   = cols[1].trim().toIntOrNull() ?: 0
                val month  = cols[2].trim().toIntOrNull() ?: 0
                val date   = cols[3].trim().ifBlank { if (year > 0 && month > 0) "$year-${month.toString().padStart(2,'0')}-01" else "" }
                val desc   = cols[4].trim()
                val cat    = cols[5].trim()
                val amount = cols[6].trim().replace(",", ".").toDoubleOrNull() ?: continue
                val notes  = cols.getOrNull(7)?.trim() ?: ""
                val flags  = cols.getOrNull(8)?.trim() ?: ""

                when (type) {
                    "Spesa" -> {
                        if (year == 0 || month == 0 || date.isEmpty()) { errors++; continue }
                        if (repo.expenseExists(date, cat, amount, month, year)) { skipped++; continue }
                        expenses.add(Expense(date=date, description=desc, category=cat, amount=amount,
                            notes=notes, month=month, year=year,
                            isSubscription=flags.contains("Abbonamento"), isNeed=flags.contains("Necessità")))
                    }
                    "Entrata" -> {
                        if (year == 0 || month == 0 || date.isEmpty()) { errors++; continue }
                        if (repo.incomeExists(date, cat, amount, month, year)) { skipped++; continue }
                        incomes.add(Income(date=date, description=desc, category=cat, amount=amount,
                            notes=notes, month=month, year=year))
                    }
                    "Risparmio" -> {
                        if (year == 0 || month == 0 || date.isEmpty()) { errors++; continue }
                        if (repo.savingExists(date, cat, amount, month, year)) { skipped++; continue }
                        savings.add(Saving(date=date, category=cat, amount=amount,
                            notes=notes, month=month, year=year,
                            isLongTerm=flags.contains("LungoPeriodo")))
                    }
                    "Debito" -> {
                        if (year == 0 || month == 0 || date.isEmpty()) { errors++; continue }
                        if (repo.debtExists(date, cat, amount, month, year)) { skipped++; continue }
                        debts.add(Debt(date=date, description=desc, category=cat, amount=amount,
                            notes=notes, month=month, year=year,
                            isHighPriority=flags.contains("AltaPriorità")))
                    }
                    "ObiettivoDebito" -> {
                        if (desc.isBlank()) continue
                        val name = desc
                        if (repo.debtGoalExistsByName(name)) { skipped++; continue }
                        val startDate   = cat.ifBlank { "2024-01-01" }
                        val monthly     = notes.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val flagParts   = flags.split("|")
                        val isActive    = flagParts.getOrNull(0)?.trim() != "Chiuso"
                        val goalNotes   = flagParts.getOrNull(1)?.trim() ?: ""
                        debtGoals.add(DebtGoal(name=name, totalAmount=amount,
                            monthlyPayment=monthly, startDate=startDate,
                            notes=goalNotes, isActive=isActive))
                    }
                    "Budget" -> {
                        if (year == 0 || month == 0) { errors++; continue }
                        budgetGoals.add(BudgetGoal(category=cat, type=desc, goal=amount, month=month, year=year))
                    }
                    else -> { /* Prelievo e righe sconosciute: ignora */ }
                }
            } catch (e: Exception) { errors++ }
        }
        return ImportResult(expenses, incomes, savings, debts, debtGoals, budgetGoals, errors, skipped)
    }

    data class ImportResult(
        val expenses:    List<Expense>,
        val incomes:     List<Income>,
        val savings:     List<Saving>,
        val debts:       List<Debt>,
        val debtGoals:   List<DebtGoal>   = emptyList(),
        val budgetGoals: List<BudgetGoal> = emptyList(),
        val errors:  Int,
        val skipped: Int = 0
    ) { val total get() = expenses.size + incomes.size + savings.size + debts.size + debtGoals.size + budgetGoals.size }

    private fun String.safe() = replace(";", ",").replace("\n", " ")
    private fun Double.csv()  = "%.2f".format(this).replace(".", ",")
}