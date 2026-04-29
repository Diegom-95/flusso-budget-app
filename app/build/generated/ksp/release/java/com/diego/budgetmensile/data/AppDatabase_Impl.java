package com.diego.budgetmensile.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.diego.budgetmensile.data.dao.BudgetGoalDao;
import com.diego.budgetmensile.data.dao.BudgetGoalDao_Impl;
import com.diego.budgetmensile.data.dao.DebtDao;
import com.diego.budgetmensile.data.dao.DebtDao_Impl;
import com.diego.budgetmensile.data.dao.DebtGoalDao;
import com.diego.budgetmensile.data.dao.DebtGoalDao_Impl;
import com.diego.budgetmensile.data.dao.ExpenseDao;
import com.diego.budgetmensile.data.dao.ExpenseDao_Impl;
import com.diego.budgetmensile.data.dao.GoalWithdrawalDao;
import com.diego.budgetmensile.data.dao.GoalWithdrawalDao_Impl;
import com.diego.budgetmensile.data.dao.IncomeDao;
import com.diego.budgetmensile.data.dao.IncomeDao_Impl;
import com.diego.budgetmensile.data.dao.RecurringExpenseDao;
import com.diego.budgetmensile.data.dao.RecurringExpenseDao_Impl;
import com.diego.budgetmensile.data.dao.SavingDao;
import com.diego.budgetmensile.data.dao.SavingDao_Impl;
import com.diego.budgetmensile.data.dao.SavingGoalDao;
import com.diego.budgetmensile.data.dao.SavingGoalDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ExpenseDao _expenseDao;

  private volatile IncomeDao _incomeDao;

  private volatile SavingDao _savingDao;

  private volatile DebtDao _debtDao;

  private volatile DebtGoalDao _debtGoalDao;

  private volatile BudgetGoalDao _budgetGoalDao;

  private volatile RecurringExpenseDao _recurringExpenseDao;

  private volatile SavingGoalDao _savingGoalDao;

  private volatile GoalWithdrawalDao _goalWithdrawalDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(6) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `description` TEXT NOT NULL, `category` TEXT NOT NULL, `amount` REAL NOT NULL, `isSubscription` INTEGER NOT NULL, `isNeed` INTEGER NOT NULL, `notes` TEXT NOT NULL, `month` INTEGER NOT NULL, `year` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `incomes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `description` TEXT NOT NULL, `category` TEXT NOT NULL, `amount` REAL NOT NULL, `isActive` INTEGER NOT NULL, `notes` TEXT NOT NULL, `month` INTEGER NOT NULL, `year` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `savings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `category` TEXT NOT NULL, `amount` REAL NOT NULL, `isLongTerm` INTEGER NOT NULL, `notes` TEXT NOT NULL, `month` INTEGER NOT NULL, `year` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `debts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `description` TEXT NOT NULL, `category` TEXT NOT NULL, `amount` REAL NOT NULL, `isHighPriority` INTEGER NOT NULL, `notes` TEXT NOT NULL, `month` INTEGER NOT NULL, `year` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `debt_goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `totalAmount` REAL NOT NULL, `monthlyPayment` REAL NOT NULL, `startDate` TEXT NOT NULL, `notes` TEXT NOT NULL, `isActive` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `budget_goals` (`category` TEXT NOT NULL, `type` TEXT NOT NULL, `goal` REAL NOT NULL, `month` INTEGER NOT NULL, `year` INTEGER NOT NULL, PRIMARY KEY(`category`, `type`, `month`, `year`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `recurring_expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `category` TEXT NOT NULL, `amount` REAL NOT NULL, `frequency` TEXT NOT NULL, `startDate` TEXT NOT NULL, `notes` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `isNeed` INTEGER NOT NULL, `durationMonths` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `saving_goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetAmount` REAL NOT NULL, `category` TEXT NOT NULL, `deadline` TEXT NOT NULL, `notes` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `goal_withdrawals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `goalId` INTEGER NOT NULL, `goalName` TEXT NOT NULL, `amount` REAL NOT NULL, `description` TEXT NOT NULL, `date` TEXT NOT NULL, `notes` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '6969d8d1c76a4dd1222e52679628d631')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `expenses`");
        db.execSQL("DROP TABLE IF EXISTS `incomes`");
        db.execSQL("DROP TABLE IF EXISTS `savings`");
        db.execSQL("DROP TABLE IF EXISTS `debts`");
        db.execSQL("DROP TABLE IF EXISTS `debt_goals`");
        db.execSQL("DROP TABLE IF EXISTS `budget_goals`");
        db.execSQL("DROP TABLE IF EXISTS `recurring_expenses`");
        db.execSQL("DROP TABLE IF EXISTS `saving_goals`");
        db.execSQL("DROP TABLE IF EXISTS `goal_withdrawals`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsExpenses = new HashMap<String, TableInfo.Column>(10);
        _columnsExpenses.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("date", new TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("isSubscription", new TableInfo.Column("isSubscription", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("isNeed", new TableInfo.Column("isNeed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("month", new TableInfo.Column("month", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("year", new TableInfo.Column("year", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExpenses = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesExpenses = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoExpenses = new TableInfo("expenses", _columnsExpenses, _foreignKeysExpenses, _indicesExpenses);
        final TableInfo _existingExpenses = TableInfo.read(db, "expenses");
        if (!_infoExpenses.equals(_existingExpenses)) {
          return new RoomOpenHelper.ValidationResult(false, "expenses(com.diego.budgetmensile.data.entity.Expense).\n"
                  + " Expected:\n" + _infoExpenses + "\n"
                  + " Found:\n" + _existingExpenses);
        }
        final HashMap<String, TableInfo.Column> _columnsIncomes = new HashMap<String, TableInfo.Column>(9);
        _columnsIncomes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncomes.put("date", new TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncomes.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncomes.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncomes.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncomes.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncomes.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncomes.put("month", new TableInfo.Column("month", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncomes.put("year", new TableInfo.Column("year", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysIncomes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesIncomes = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoIncomes = new TableInfo("incomes", _columnsIncomes, _foreignKeysIncomes, _indicesIncomes);
        final TableInfo _existingIncomes = TableInfo.read(db, "incomes");
        if (!_infoIncomes.equals(_existingIncomes)) {
          return new RoomOpenHelper.ValidationResult(false, "incomes(com.diego.budgetmensile.data.entity.Income).\n"
                  + " Expected:\n" + _infoIncomes + "\n"
                  + " Found:\n" + _existingIncomes);
        }
        final HashMap<String, TableInfo.Column> _columnsSavings = new HashMap<String, TableInfo.Column>(8);
        _columnsSavings.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavings.put("date", new TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavings.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavings.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavings.put("isLongTerm", new TableInfo.Column("isLongTerm", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavings.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavings.put("month", new TableInfo.Column("month", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavings.put("year", new TableInfo.Column("year", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSavings = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSavings = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSavings = new TableInfo("savings", _columnsSavings, _foreignKeysSavings, _indicesSavings);
        final TableInfo _existingSavings = TableInfo.read(db, "savings");
        if (!_infoSavings.equals(_existingSavings)) {
          return new RoomOpenHelper.ValidationResult(false, "savings(com.diego.budgetmensile.data.entity.Saving).\n"
                  + " Expected:\n" + _infoSavings + "\n"
                  + " Found:\n" + _existingSavings);
        }
        final HashMap<String, TableInfo.Column> _columnsDebts = new HashMap<String, TableInfo.Column>(9);
        _columnsDebts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDebts.put("date", new TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDebts.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDebts.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDebts.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDebts.put("isHighPriority", new TableInfo.Column("isHighPriority", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDebts.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDebts.put("month", new TableInfo.Column("month", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDebts.put("year", new TableInfo.Column("year", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDebts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDebts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDebts = new TableInfo("debts", _columnsDebts, _foreignKeysDebts, _indicesDebts);
        final TableInfo _existingDebts = TableInfo.read(db, "debts");
        if (!_infoDebts.equals(_existingDebts)) {
          return new RoomOpenHelper.ValidationResult(false, "debts(com.diego.budgetmensile.data.entity.Debt).\n"
                  + " Expected:\n" + _infoDebts + "\n"
                  + " Found:\n" + _existingDebts);
        }
        final HashMap<String, TableInfo.Column> _columnsDebtGoals = new HashMap<String, TableInfo.Column>(7);
        _columnsDebtGoals.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDebtGoals.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDebtGoals.put("totalAmount", new TableInfo.Column("totalAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDebtGoals.put("monthlyPayment", new TableInfo.Column("monthlyPayment", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDebtGoals.put("startDate", new TableInfo.Column("startDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDebtGoals.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDebtGoals.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDebtGoals = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDebtGoals = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDebtGoals = new TableInfo("debt_goals", _columnsDebtGoals, _foreignKeysDebtGoals, _indicesDebtGoals);
        final TableInfo _existingDebtGoals = TableInfo.read(db, "debt_goals");
        if (!_infoDebtGoals.equals(_existingDebtGoals)) {
          return new RoomOpenHelper.ValidationResult(false, "debt_goals(com.diego.budgetmensile.data.entity.DebtGoal).\n"
                  + " Expected:\n" + _infoDebtGoals + "\n"
                  + " Found:\n" + _existingDebtGoals);
        }
        final HashMap<String, TableInfo.Column> _columnsBudgetGoals = new HashMap<String, TableInfo.Column>(5);
        _columnsBudgetGoals.put("category", new TableInfo.Column("category", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudgetGoals.put("type", new TableInfo.Column("type", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudgetGoals.put("goal", new TableInfo.Column("goal", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudgetGoals.put("month", new TableInfo.Column("month", "INTEGER", true, 3, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudgetGoals.put("year", new TableInfo.Column("year", "INTEGER", true, 4, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBudgetGoals = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBudgetGoals = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBudgetGoals = new TableInfo("budget_goals", _columnsBudgetGoals, _foreignKeysBudgetGoals, _indicesBudgetGoals);
        final TableInfo _existingBudgetGoals = TableInfo.read(db, "budget_goals");
        if (!_infoBudgetGoals.equals(_existingBudgetGoals)) {
          return new RoomOpenHelper.ValidationResult(false, "budget_goals(com.diego.budgetmensile.data.entity.BudgetGoal).\n"
                  + " Expected:\n" + _infoBudgetGoals + "\n"
                  + " Found:\n" + _existingBudgetGoals);
        }
        final HashMap<String, TableInfo.Column> _columnsRecurringExpenses = new HashMap<String, TableInfo.Column>(10);
        _columnsRecurringExpenses.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringExpenses.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringExpenses.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringExpenses.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringExpenses.put("frequency", new TableInfo.Column("frequency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringExpenses.put("startDate", new TableInfo.Column("startDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringExpenses.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringExpenses.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringExpenses.put("isNeed", new TableInfo.Column("isNeed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringExpenses.put("durationMonths", new TableInfo.Column("durationMonths", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRecurringExpenses = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRecurringExpenses = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRecurringExpenses = new TableInfo("recurring_expenses", _columnsRecurringExpenses, _foreignKeysRecurringExpenses, _indicesRecurringExpenses);
        final TableInfo _existingRecurringExpenses = TableInfo.read(db, "recurring_expenses");
        if (!_infoRecurringExpenses.equals(_existingRecurringExpenses)) {
          return new RoomOpenHelper.ValidationResult(false, "recurring_expenses(com.diego.budgetmensile.data.entity.RecurringExpense).\n"
                  + " Expected:\n" + _infoRecurringExpenses + "\n"
                  + " Found:\n" + _existingRecurringExpenses);
        }
        final HashMap<String, TableInfo.Column> _columnsSavingGoals = new HashMap<String, TableInfo.Column>(7);
        _columnsSavingGoals.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavingGoals.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavingGoals.put("targetAmount", new TableInfo.Column("targetAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavingGoals.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavingGoals.put("deadline", new TableInfo.Column("deadline", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavingGoals.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavingGoals.put("isCompleted", new TableInfo.Column("isCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSavingGoals = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSavingGoals = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSavingGoals = new TableInfo("saving_goals", _columnsSavingGoals, _foreignKeysSavingGoals, _indicesSavingGoals);
        final TableInfo _existingSavingGoals = TableInfo.read(db, "saving_goals");
        if (!_infoSavingGoals.equals(_existingSavingGoals)) {
          return new RoomOpenHelper.ValidationResult(false, "saving_goals(com.diego.budgetmensile.data.entity.SavingGoal).\n"
                  + " Expected:\n" + _infoSavingGoals + "\n"
                  + " Found:\n" + _existingSavingGoals);
        }
        final HashMap<String, TableInfo.Column> _columnsGoalWithdrawals = new HashMap<String, TableInfo.Column>(7);
        _columnsGoalWithdrawals.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGoalWithdrawals.put("goalId", new TableInfo.Column("goalId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGoalWithdrawals.put("goalName", new TableInfo.Column("goalName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGoalWithdrawals.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGoalWithdrawals.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGoalWithdrawals.put("date", new TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGoalWithdrawals.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysGoalWithdrawals = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesGoalWithdrawals = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoGoalWithdrawals = new TableInfo("goal_withdrawals", _columnsGoalWithdrawals, _foreignKeysGoalWithdrawals, _indicesGoalWithdrawals);
        final TableInfo _existingGoalWithdrawals = TableInfo.read(db, "goal_withdrawals");
        if (!_infoGoalWithdrawals.equals(_existingGoalWithdrawals)) {
          return new RoomOpenHelper.ValidationResult(false, "goal_withdrawals(com.diego.budgetmensile.data.entity.GoalWithdrawal).\n"
                  + " Expected:\n" + _infoGoalWithdrawals + "\n"
                  + " Found:\n" + _existingGoalWithdrawals);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "6969d8d1c76a4dd1222e52679628d631", "0b97f498dc9e4b5437436fd6cf470605");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "expenses","incomes","savings","debts","debt_goals","budget_goals","recurring_expenses","saving_goals","goal_withdrawals");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `expenses`");
      _db.execSQL("DELETE FROM `incomes`");
      _db.execSQL("DELETE FROM `savings`");
      _db.execSQL("DELETE FROM `debts`");
      _db.execSQL("DELETE FROM `debt_goals`");
      _db.execSQL("DELETE FROM `budget_goals`");
      _db.execSQL("DELETE FROM `recurring_expenses`");
      _db.execSQL("DELETE FROM `saving_goals`");
      _db.execSQL("DELETE FROM `goal_withdrawals`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ExpenseDao.class, ExpenseDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(IncomeDao.class, IncomeDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SavingDao.class, SavingDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DebtDao.class, DebtDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DebtGoalDao.class, DebtGoalDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BudgetGoalDao.class, BudgetGoalDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(RecurringExpenseDao.class, RecurringExpenseDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SavingGoalDao.class, SavingGoalDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(GoalWithdrawalDao.class, GoalWithdrawalDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ExpenseDao expenseDao() {
    if (_expenseDao != null) {
      return _expenseDao;
    } else {
      synchronized(this) {
        if(_expenseDao == null) {
          _expenseDao = new ExpenseDao_Impl(this);
        }
        return _expenseDao;
      }
    }
  }

  @Override
  public IncomeDao incomeDao() {
    if (_incomeDao != null) {
      return _incomeDao;
    } else {
      synchronized(this) {
        if(_incomeDao == null) {
          _incomeDao = new IncomeDao_Impl(this);
        }
        return _incomeDao;
      }
    }
  }

  @Override
  public SavingDao savingDao() {
    if (_savingDao != null) {
      return _savingDao;
    } else {
      synchronized(this) {
        if(_savingDao == null) {
          _savingDao = new SavingDao_Impl(this);
        }
        return _savingDao;
      }
    }
  }

  @Override
  public DebtDao debtDao() {
    if (_debtDao != null) {
      return _debtDao;
    } else {
      synchronized(this) {
        if(_debtDao == null) {
          _debtDao = new DebtDao_Impl(this);
        }
        return _debtDao;
      }
    }
  }

  @Override
  public DebtGoalDao debtGoalDao() {
    if (_debtGoalDao != null) {
      return _debtGoalDao;
    } else {
      synchronized(this) {
        if(_debtGoalDao == null) {
          _debtGoalDao = new DebtGoalDao_Impl(this);
        }
        return _debtGoalDao;
      }
    }
  }

  @Override
  public BudgetGoalDao budgetGoalDao() {
    if (_budgetGoalDao != null) {
      return _budgetGoalDao;
    } else {
      synchronized(this) {
        if(_budgetGoalDao == null) {
          _budgetGoalDao = new BudgetGoalDao_Impl(this);
        }
        return _budgetGoalDao;
      }
    }
  }

  @Override
  public RecurringExpenseDao recurringExpenseDao() {
    if (_recurringExpenseDao != null) {
      return _recurringExpenseDao;
    } else {
      synchronized(this) {
        if(_recurringExpenseDao == null) {
          _recurringExpenseDao = new RecurringExpenseDao_Impl(this);
        }
        return _recurringExpenseDao;
      }
    }
  }

  @Override
  public SavingGoalDao savingGoalDao() {
    if (_savingGoalDao != null) {
      return _savingGoalDao;
    } else {
      synchronized(this) {
        if(_savingGoalDao == null) {
          _savingGoalDao = new SavingGoalDao_Impl(this);
        }
        return _savingGoalDao;
      }
    }
  }

  @Override
  public GoalWithdrawalDao goalWithdrawalDao() {
    if (_goalWithdrawalDao != null) {
      return _goalWithdrawalDao;
    } else {
      synchronized(this) {
        if(_goalWithdrawalDao == null) {
          _goalWithdrawalDao = new GoalWithdrawalDao_Impl(this);
        }
        return _goalWithdrawalDao;
      }
    }
  }
}
