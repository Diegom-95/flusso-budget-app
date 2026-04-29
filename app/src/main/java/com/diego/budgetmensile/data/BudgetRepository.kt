package com.diego.budgetmensile.data

import android.content.Context
import com.diego.budgetmensile.data.entity.*

class BudgetRepository(context: Context) {

    private val db                = AppDatabase.getInstance(context)
    private val expenseDao        = db.expenseDao()
    private val incomeDao         = db.incomeDao()
    private val savingDao         = db.savingDao()
    private val debtDao           = db.debtDao()
    private val debtGoalDao       = db.debtGoalDao()
    private val budgetGoalDao     = db.budgetGoalDao()
    private val recurringDao      = db.recurringExpenseDao()
    private val savingGoalDao     = db.savingGoalDao()
    private val goalWithdrawalDao = db.goalWithdrawalDao()

    // ── Spese ─────────────────────────────────────────────────────────────────
    fun getExpensesByMonth(m: Int, y: Int)      = expenseDao.getByMonth(m, y)
    fun getExpensesByYear(y: Int)               = expenseDao.getByYear(y)
    fun getExpensesTotalByMonth(m: Int, y: Int) = expenseDao.getTotalByMonth(m, y)
    fun getNeedsTotalByMonth(m: Int, y: Int)    = expenseDao.getNeedsTotalByMonth(m, y)
    fun getSubscriptionsByMonth(m: Int, y: Int) = expenseDao.getSubscriptionsTotalByMonth(m, y)
    fun getRecentExpenses()                     = expenseDao.getRecent()
    suspend fun insertExpense(e: Expense) = expenseDao.insert(e)
    suspend fun updateExpense(e: Expense) = expenseDao.update(e)
    suspend fun deleteExpense(e: Expense) = expenseDao.delete(e)
    suspend fun getExpensesByYearSync(y: Int)  = expenseDao.getByYearSync(y)
    suspend fun getIncomesByYearSync(y: Int)   = incomeDao.getByYearSync(y)
    suspend fun getSavingsByYearSync(y: Int)   = savingDao.getByYearSync(y)
    suspend fun getDebtsByYearSync(y: Int)     = debtDao.getByYearSync(y)
    suspend fun getAllWithdrawalsSync()         = goalWithdrawalDao.getAllSync()

    suspend fun expenseExists(date: String, cat: String, amt: Double, m: Int, y: Int) =
        expenseDao.exists(date, cat, amt, m, y) > 0

    // ── Entrate ───────────────────────────────────────────────────────────────
    fun getIncomesByMonth(m: Int, y: Int)     = incomeDao.getByMonth(m, y)
    fun getIncomesByYear(y: Int)              = incomeDao.getByYear(y)
    fun getIncomeTotalByMonth(m: Int, y: Int) = incomeDao.getTotalByMonth(m, y)
    suspend fun insertIncome(i: Income) = incomeDao.insert(i)
    suspend fun updateIncome(i: Income) = incomeDao.update(i)
    suspend fun deleteIncome(i: Income) = incomeDao.delete(i)
    suspend fun incomeExists(date: String, cat: String, amt: Double, m: Int, y: Int) =
        incomeDao.exists(date, cat, amt, m, y) > 0

    // ── Risparmi ──────────────────────────────────────────────────────────────
    fun getSavingsByMonth(m: Int, y: Int)      = savingDao.getByMonth(m, y)
    fun getSavingsByYear(y: Int)               = savingDao.getByYear(y)
    fun getSavingsTotalByMonth(m: Int, y: Int) = savingDao.getTotalByMonth(m, y)
    suspend fun insertSaving(s: Saving) = savingDao.insert(s)
    suspend fun updateSaving(s: Saving) = savingDao.update(s)
    suspend fun deleteSaving(s: Saving) = savingDao.delete(s)
    suspend fun savingExists(date: String, cat: String, amt: Double, m: Int, y: Int) =
        savingDao.exists(date, cat, amt, m, y) > 0

    // ── Debiti ────────────────────────────────────────────────────────────────
    fun getDebtsByMonth(m: Int, y: Int)      = debtDao.getByMonth(m, y)
    fun getDebtsByYear(y: Int)               = debtDao.getByYear(y)
    fun getDebtsTotalByMonth(m: Int, y: Int) = debtDao.getTotalByMonth(m, y)
    fun getTotalPaidForGoal(name: String)    = debtDao.getTotalPaidForGoal(name)
    suspend fun insertDebt(d: Debt) = debtDao.insert(d)
    suspend fun updateDebt(d: Debt) = debtDao.update(d)
    suspend fun deleteDebt(d: Debt) = debtDao.delete(d)
    suspend fun debtExists(date: String, cat: String, amt: Double, m: Int, y: Int) =
        debtDao.exists(date, cat, amt, m, y) > 0

    // ── Obiettivi debito ──────────────────────────────────────────────────────
    fun getAllDebtGoals()                   = debtGoalDao.getAll()
    suspend fun insertDebtGoal(g: DebtGoal)         = debtGoalDao.insert(g)
    suspend fun getAllDebtGoalsSync()               = debtGoalDao.getAllSync()
    suspend fun debtGoalExistsByName(name: String) = debtGoalDao.existsByName(name) > 0
    suspend fun updateDebtGoal(g: DebtGoal) = debtGoalDao.update(g)
    suspend fun deleteDebtGoal(g: DebtGoal) = debtGoalDao.delete(g)

    // ── Budget per categoria ──────────────────────────────────────────────────
    fun getGoalsByTypeAndMonth(t: String, m: Int, y: Int) = budgetGoalDao.getByTypeAndMonth(t, m, y)
    suspend fun upsertGoal(g: BudgetGoal)  = budgetGoalDao.insert(g)
    suspend fun deleteGoal(g: BudgetGoal)  = budgetGoalDao.delete(g)
    suspend fun getTotalGoal(t: String, m: Int, y: Int) = budgetGoalDao.getTotalGoal(t, m, y) ?: 0.0
    suspend fun getAllBudgetGoals() = budgetGoalDao.getAll()

    // ── Spese ricorrenti ──────────────────────────────────────────────────────
    fun getAllRecurring()                          = recurringDao.getAll()
    fun getActiveRecurring()                      = recurringDao.getActive()
    suspend fun getActiveRecurringSync()          = recurringDao.getActiveSync()
    suspend fun insertRecurring(r: RecurringExpense) = recurringDao.insert(r)
    suspend fun updateRecurring(r: RecurringExpense) = recurringDao.update(r)
    suspend fun deleteRecurring(r: RecurringExpense) = recurringDao.delete(r)
    suspend fun updateFutureExpensesByName(name: String, amount: Double, category: String, fromDate: String) =
        recurringDao.updateFutureByName(name, amount, category, fromDate)

    // ── Obiettivi di risparmio ────────────────────────────────────────────────
    fun getAllSavingGoals()                      = savingGoalDao.getAll()
    fun getTotalSavedForCategory(cat: String)   = savingGoalDao.getTotalSavedForCategory(cat)
    suspend fun insertSavingGoal(g: SavingGoal) = savingGoalDao.insert(g)
    suspend fun updateSavingGoal(g: SavingGoal) = savingGoalDao.update(g)
    suspend fun deleteSavingGoal(g: SavingGoal) = savingGoalDao.delete(g)

    // ── Prelievi dagli obiettivi ──────────────────────────────────────────────
    fun getAllWithdrawals()                              = goalWithdrawalDao.getAll()
    fun getWithdrawalsByGoal(goalId: Int)               = goalWithdrawalDao.getByGoal(goalId)
    fun getWithdrawalsByYear(year: String)              = goalWithdrawalDao.getByYear(year)
    fun getTotalWithdrawnForGoal(goalId: Int)           = goalWithdrawalDao.getTotalWithdrawnForGoal(goalId)
    suspend fun insertWithdrawal(w: GoalWithdrawal)    = goalWithdrawalDao.insert(w)
    suspend fun deleteWithdrawal(w: GoalWithdrawal)    = goalWithdrawalDao.delete(w)
}