package com.diego.budgetmensile.ui.annual

import android.app.Application
import androidx.lifecycle.*
import com.diego.budgetmensile.data.BudgetRepository

class AnnualDashboardViewModel(app: Application) : AndroidViewModel(app) {
    private val repo  = BudgetRepository(app)
    private val _year = MutableLiveData<Int>()

    val expensesByYear    = _year.switchMap { y -> repo.getExpensesByYear(y) }
    val incomesByYear     = _year.switchMap { y -> repo.getIncomesByYear(y) }
    val savingsByYear     = _year.switchMap { y -> repo.getSavingsByYear(y) }
    val debtsByYear       = _year.switchMap { y -> repo.getDebtsByYear(y) }
    val withdrawalsByYear = _year.switchMap { y -> repo.getWithdrawalsByYear(y.toString()) }

    fun setYear(y: Int) { _year.value = y }
}
