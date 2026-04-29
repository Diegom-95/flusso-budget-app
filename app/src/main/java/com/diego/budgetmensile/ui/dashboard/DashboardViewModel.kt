package com.diego.budgetmensile.ui.dashboard

import android.app.Application
import androidx.lifecycle.*
import com.diego.budgetmensile.data.BudgetRepository
import com.diego.budgetmensile.utils.Constants

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = BudgetRepository(app)
    private val _month = MutableLiveData<Int>()
    private val _year  = MutableLiveData<Int>()

    val totalExpenses = _month.switchMap { m -> _year.switchMap { y -> repo.getExpensesTotalByMonth(m, y) } }
    val totalIncome   = _month.switchMap { m -> _year.switchMap { y -> repo.getIncomeTotalByMonth(m, y) } }
    val totalSavings  = _month.switchMap { m -> _year.switchMap { y -> repo.getSavingsTotalByMonth(m, y) } }
    val totalDebts    = _month.switchMap { m -> _year.switchMap { y -> repo.getDebtsTotalByMonth(m, y) } }
    val needsTotal    = _month.switchMap { m -> _year.switchMap { y -> repo.getNeedsTotalByMonth(m, y) } }
    val subscriptions = _month.switchMap { m -> _year.switchMap { y -> repo.getSubscriptionsByMonth(m, y) } }

    val expenses = _month.switchMap { m -> _year.switchMap { y -> repo.getExpensesByMonth(m, y) } }
    val incomes  = _month.switchMap { m -> _year.switchMap { y -> repo.getIncomesByMonth(m, y) } }
    val savings  = _month.switchMap { m -> _year.switchMap { y -> repo.getSavingsByMonth(m, y) } }
    val debts    = _month.switchMap { m -> _year.switchMap { y -> repo.getDebtsByMonth(m, y) } }

    val rimastoAllocare: LiveData<Double> = MediatorLiveData<Double>().apply {
        val update = {
            val inc  = totalIncome.value   ?: 0.0
            val exp  = totalExpenses.value ?: 0.0
            val sav  = totalSavings.value  ?: 0.0
            val debt = totalDebts.value    ?: 0.0
            value = inc - exp - sav - debt
        }
        addSource(totalIncome)   { update() }
        addSource(totalExpenses) { update() }
        addSource(totalSavings)  { update() }
        addSource(totalDebts)    { update() }
    }

    fun setMonthYear(month: Int, year: Int) {
        _month.value = month
        _year.value  = year
    }
}
