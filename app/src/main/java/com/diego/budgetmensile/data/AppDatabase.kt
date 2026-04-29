package com.diego.budgetmensile.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diego.budgetmensile.data.dao.*
import com.diego.budgetmensile.data.entity.*

@Database(
    entities = [Expense::class, Income::class, Saving::class, Debt::class,
                DebtGoal::class, BudgetGoal::class, RecurringExpense::class,
                SavingGoal::class, GoalWithdrawal::class],
    version = 6, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao():           ExpenseDao
    abstract fun incomeDao():            IncomeDao
    abstract fun savingDao():            SavingDao
    abstract fun debtDao():              DebtDao
    abstract fun debtGoalDao():          DebtGoalDao
    abstract fun budgetGoalDao():        BudgetGoalDao
    abstract fun recurringExpenseDao():  RecurringExpenseDao
    abstract fun savingGoalDao():        SavingGoalDao
    abstract fun goalWithdrawalDao():    GoalWithdrawalDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `debt_goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `totalAmount` REAL NOT NULL, `monthlyPayment` REAL NOT NULL, `startDate` TEXT NOT NULL, `notes` TEXT NOT NULL DEFAULT '', `isActive` INTEGER NOT NULL DEFAULT 1)")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `recurring_expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `category` TEXT NOT NULL, `amount` REAL NOT NULL, `frequency` TEXT NOT NULL, `startDate` TEXT NOT NULL, `notes` TEXT NOT NULL DEFAULT '', `isActive` INTEGER NOT NULL DEFAULT 1, `isNeed` INTEGER NOT NULL DEFAULT 0)")
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `recurring_expenses` ADD COLUMN `durationMonths` INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `saving_goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetAmount` REAL NOT NULL, `category` TEXT NOT NULL, `deadline` TEXT NOT NULL, `notes` TEXT NOT NULL DEFAULT '', `isCompleted` INTEGER NOT NULL DEFAULT 0)")
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `goal_withdrawals` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `goalId` INTEGER NOT NULL,
                        `goalName` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `description` TEXT NOT NULL,
                        `date` TEXT NOT NULL,
                        `notes` TEXT NOT NULL DEFAULT ''
                    )""".trimIndent())
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "budget_mensile.db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build().also { INSTANCE = it }
            }
    }
}
