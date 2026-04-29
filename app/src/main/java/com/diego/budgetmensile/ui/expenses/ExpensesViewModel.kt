package com.diego.budgetmensile.ui.expenses

import android.app.Application
import androidx.lifecycle.*
import com.diego.budgetmensile.data.BudgetRepository
import com.diego.budgetmensile.data.entity.Expense
import com.diego.budgetmensile.data.entity.RecurringExpense
import com.diego.budgetmensile.utils.extractMonth
import com.diego.budgetmensile.utils.extractYear
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpensesViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = BudgetRepository(app)

    private val _month = MutableLiveData<Int>()
    private val _year  = MutableLiveData<Int>()

    val expenses = _month.switchMap { m ->
        _year.switchMap { y -> repo.getExpensesByMonth(m, y) }
    }
    val total = _month.switchMap { m ->
        _year.switchMap { y -> repo.getExpensesTotalByMonth(m, y) }
    }
    val allRecurring = repo.getAllRecurring()

    fun setMonthYear(month: Int, year: Int) {
        _month.value = month
        _year.value  = year
    }

    fun insert(expense: Expense) = viewModelScope.launch { repo.insertExpense(expense) }
    fun update(expense: Expense) = viewModelScope.launch { repo.updateExpense(expense) }
    fun delete(expense: Expense) = viewModelScope.launch { repo.deleteExpense(expense) }

    /**
     * Salva una RecurringExpense e genera subito tutte le Expense future
     * nei mesi successivi in base a frequenza e durata.
     *
     * @param baseExpense   la spesa del mese corrente (già inserita o da inserire)
     * @param recurring     il template ricorrente (id=0, verrà auto-generato)
     * @param insertBase    true se baseExpense non è ancora stata inserita
     */
    fun insertRecurringWithFutureExpenses(
        baseExpense: Expense,
        recurring: RecurringExpense,
        insertBase: Boolean = true
    ) = viewModelScope.launch {
        if (insertBase) repo.insertExpense(baseExpense)
        repo.insertRecurring(recurring)

        // Determina quante occorrenze generare
        // durationMonths == 0 → illimitata (generiamo 24 mesi in avanti)
        val maxOccurrences = when {
            recurring.durationMonths > 0 -> recurring.durationMonths - 1 // la prima è già stata inserita
            else -> 23 // 24 mesi totali incluso il primo
        }

        val startCal = Calendar.getInstance().apply {
            val parts = baseExpense.date.split("-")
            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        }

        // Calcola step in mesi in base alla frequenza
        val stepMonths = when (recurring.frequency) {
            "WEEKLY"    -> 0  // settimanale: gestiamo a parte
            "QUARTERLY" -> 3
            "ANNUAL"    -> 12
            else        -> 1  // MONTHLY default
        }

        if (recurring.frequency == "WEEKLY") {
            // Per settimanale generiamo settimana per settimana
            val weeklyMax = if (recurring.durationMonths > 0) recurring.durationMonths * 4 else 96
            for (w in 1..weeklyMax) {
                val cal = (startCal.clone() as Calendar).apply { add(Calendar.WEEK_OF_YEAR, w) }
                val date = "%04d-%02d-%02d".format(
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                repo.insertExpense(baseExpense.copy(
                    id = 0, date = date,
                    month = date.extractMonth(), year = date.extractYear()
                ))
            }
        } else {
            for (i in 1..maxOccurrences) {
                val cal = (startCal.clone() as Calendar).apply { add(Calendar.MONTH, stepMonths * i) }
                val date = "%04d-%02d-%02d".format(
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                repo.insertExpense(baseExpense.copy(
                    id = 0, date = date,
                    month = date.extractMonth(), year = date.extractYear()
                ))
            }
        }
    }

    fun deleteRecurring(r: RecurringExpense) = viewModelScope.launch { repo.deleteRecurring(r) }
    fun updateRecurring(r: RecurringExpense) = viewModelScope.launch { repo.updateRecurring(r) }
}
