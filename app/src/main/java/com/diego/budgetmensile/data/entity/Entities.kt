package com.diego.budgetmensile.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, val description: String, val category: String,
    val amount: Double, val isSubscription: Boolean = false,
    val isNeed: Boolean = false, val notes: String = "",
    val month: Int, val year: Int
)

@Entity(tableName = "incomes")
data class Income(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, val description: String, val category: String,
    val amount: Double, val isActive: Boolean = true,
    val notes: String = "", val month: Int, val year: Int
)

@Entity(tableName = "savings")
data class Saving(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, val category: String, val amount: Double,
    val isLongTerm: Boolean = false, val notes: String = "",
    val month: Int, val year: Int
)

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, val description: String, val category: String,
    val amount: Double, val isHighPriority: Boolean = false,
    val notes: String = "", val month: Int, val year: Int
)

@Entity(tableName = "debt_goals")
data class DebtGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, val totalAmount: Double,
    val monthlyPayment: Double, val startDate: String,
    val notes: String = "", val isActive: Boolean = true
)

@Entity(tableName = "budget_goals", primaryKeys = ["category", "type", "month", "year"])
data class BudgetGoal(
    val category: String, val type: String,
    val goal: Double, val month: Int, val year: Int
)

@Entity(tableName = "recurring_expenses")
data class RecurringExpense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, val category: String, val amount: Double,
    val frequency: String, val startDate: String,
    val notes: String = "", val isActive: Boolean = true,
    val isNeed: Boolean = false, val durationMonths: Int = 0
)

@Entity(tableName = "saving_goals")
data class SavingGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, val targetAmount: Double, val category: String,
    val deadline: String, val notes: String = "",
    val isCompleted: Boolean = false
)
