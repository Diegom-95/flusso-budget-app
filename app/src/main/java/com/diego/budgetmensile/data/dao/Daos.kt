package com.diego.budgetmensile.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.diego.budgetmensile.data.entity.*

@Dao interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(e: Expense)
    @Update suspend fun update(e: Expense)
    @Delete suspend fun delete(e: Expense)
    @Query("SELECT * FROM expenses WHERE month=:m AND year=:y ORDER BY date DESC") fun getByMonth(m:Int,y:Int): LiveData<List<Expense>>
    @Query("SELECT * FROM expenses WHERE year=:y ORDER BY date DESC") fun getByYear(y:Int): LiveData<List<Expense>>
    @Query("SELECT SUM(amount) FROM expenses WHERE month=:m AND year=:y") fun getTotalByMonth(m:Int,y:Int): LiveData<Double?>
    @Query("SELECT SUM(amount) FROM expenses WHERE month=:m AND year=:y AND isNeed=1") fun getNeedsTotalByMonth(m:Int,y:Int): LiveData<Double?>
    @Query("SELECT SUM(amount) FROM expenses WHERE month=:m AND year=:y AND isSubscription=1") fun getSubscriptionsTotalByMonth(m:Int,y:Int): LiveData<Double?>
    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT 20") fun getRecent(): LiveData<List<Expense>>
    @Query("SELECT COUNT(*) FROM expenses WHERE date=:date AND category=:cat AND ABS(amount-:amt)<0.001 AND month=:m AND year=:y") suspend fun exists(date:String,cat:String,amt:Double,m:Int,y:Int): Int
    @Query("SELECT * FROM expenses WHERE year=:y ORDER BY date DESC") suspend fun getByYearSync(y:Int): List<Expense>
}

@Dao interface IncomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(i: Income)
    @Update suspend fun update(i: Income)
    @Delete suspend fun delete(i: Income)
    @Query("SELECT * FROM incomes WHERE month=:m AND year=:y ORDER BY date DESC") fun getByMonth(m:Int,y:Int): LiveData<List<Income>>
    @Query("SELECT * FROM incomes WHERE year=:y ORDER BY date DESC") fun getByYear(y:Int): LiveData<List<Income>>
    @Query("SELECT SUM(amount) FROM incomes WHERE month=:m AND year=:y") fun getTotalByMonth(m:Int,y:Int): LiveData<Double?>
    @Query("SELECT SUM(amount) FROM incomes WHERE month=:m AND year=:y AND isActive=1") fun getActiveTotalByMonth(m:Int,y:Int): LiveData<Double?>
    @Query("SELECT COUNT(*) FROM incomes WHERE date=:date AND category=:cat AND ABS(amount-:amt)<0.001 AND month=:m AND year=:y") suspend fun exists(date:String,cat:String,amt:Double,m:Int,y:Int): Int
    @Query("SELECT * FROM incomes WHERE year=:y ORDER BY date DESC") suspend fun getByYearSync(y:Int): List<Income>
}

@Dao interface SavingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(s: Saving)
    @Update suspend fun update(s: Saving)
    @Delete suspend fun delete(s: Saving)
    @Query("SELECT * FROM savings WHERE month=:m AND year=:y ORDER BY category") fun getByMonth(m:Int,y:Int): LiveData<List<Saving>>
    @Query("SELECT * FROM savings WHERE year=:y ORDER BY date DESC") fun getByYear(y:Int): LiveData<List<Saving>>
    @Query("SELECT SUM(amount) FROM savings WHERE month=:m AND year=:y") fun getTotalByMonth(m:Int,y:Int): LiveData<Double?>
    @Query("SELECT COUNT(*) FROM savings WHERE date=:date AND category=:cat AND ABS(amount-:amt)<0.001 AND month=:m AND year=:y") suspend fun exists(date:String,cat:String,amt:Double,m:Int,y:Int): Int
    @Query("SELECT * FROM savings WHERE year=:y ORDER BY date DESC") suspend fun getByYearSync(y:Int): List<Saving>
}

@Dao interface DebtDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(d: Debt)
    @Update suspend fun update(d: Debt)
    @Delete suspend fun delete(d: Debt)
    @Query("SELECT * FROM debts WHERE month=:m AND year=:y ORDER BY date DESC") fun getByMonth(m:Int,y:Int): LiveData<List<Debt>>
    @Query("SELECT * FROM debts WHERE year=:y ORDER BY date DESC") fun getByYear(y:Int): LiveData<List<Debt>>
    @Query("SELECT SUM(amount) FROM debts WHERE month=:m AND year=:y") fun getTotalByMonth(m:Int,y:Int): LiveData<Double?>
    @Query("SELECT COUNT(*) FROM debts WHERE date=:date AND category=:cat AND ABS(amount-:amt)<0.001 AND month=:m AND year=:y") suspend fun exists(date:String,cat:String,amt:Double,m:Int,y:Int): Int
    @Query("SELECT SUM(amount) FROM debts WHERE category=:goalName") fun getTotalPaidForGoal(goalName:String): LiveData<Double?>
    @Query("SELECT * FROM debts WHERE year=:y ORDER BY date DESC") suspend fun getByYearSync(y:Int): List<Debt>
}

@Dao interface DebtGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(g: DebtGoal)
    @Update suspend fun update(g: DebtGoal)
    @Delete suspend fun delete(g: DebtGoal)
    @Query("SELECT * FROM debt_goals ORDER BY isActive DESC, name ASC") fun getAll(): LiveData<List<DebtGoal>>
    @Query("SELECT * FROM debt_goals WHERE id=:id") suspend fun getById(id:Int): DebtGoal?
    @Query("SELECT * FROM debt_goals ORDER BY isActive DESC, name ASC") suspend fun getAllSync(): List<DebtGoal>
    @Query("SELECT COUNT(*) FROM debt_goals WHERE name=:name") suspend fun existsByName(name:String): Int
}

@Dao interface BudgetGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(g: BudgetGoal)
    @Update suspend fun update(g: BudgetGoal)
    @Delete suspend fun delete(g: BudgetGoal)
    @Query("SELECT * FROM budget_goals WHERE type=:t AND month=:m AND year=:y") fun getByTypeAndMonth(t:String,m:Int,y:Int): LiveData<List<BudgetGoal>>
    @Query("SELECT goal FROM budget_goals WHERE category=:c AND type=:t AND month=:m AND year=:y") suspend fun getGoal(c:String,t:String,m:Int,y:Int): Double?
    @Query("SELECT SUM(goal) FROM budget_goals WHERE type=:t AND month=:m AND year=:y") suspend fun getTotalGoal(t:String,m:Int,y:Int): Double?
    @Query("SELECT * FROM budget_goals") suspend fun getAll(): List<BudgetGoal>
}

@Dao interface RecurringExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(r: RecurringExpense)
    @Update suspend fun update(r: RecurringExpense)
    @Delete suspend fun delete(r: RecurringExpense)
    @Query("SELECT * FROM recurring_expenses ORDER BY name ASC") fun getAll(): LiveData<List<RecurringExpense>>
    @Query("SELECT * FROM recurring_expenses WHERE isActive=1 ORDER BY name ASC") fun getActive(): LiveData<List<RecurringExpense>>
    @Query("SELECT * FROM recurring_expenses WHERE isActive=1") suspend fun getActiveSync(): List<RecurringExpense>
    @Query("UPDATE expenses SET amount=:amount, category=:category WHERE description=:name AND date >= :fromDate") suspend fun updateFutureByName(name:String, amount:Double, category:String, fromDate:String)
}

@Dao interface SavingGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(g: SavingGoal)
    @Update suspend fun update(g: SavingGoal)
    @Delete suspend fun delete(g: SavingGoal)
    @Query("SELECT * FROM saving_goals ORDER BY isCompleted ASC, deadline ASC") fun getAll(): LiveData<List<SavingGoal>>
    @Query("SELECT SUM(amount) FROM savings WHERE category = :cat") fun getTotalSavedForCategory(cat: String): LiveData<Double?>
}

