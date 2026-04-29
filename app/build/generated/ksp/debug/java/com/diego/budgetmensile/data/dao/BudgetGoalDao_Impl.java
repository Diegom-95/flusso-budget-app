package com.diego.budgetmensile.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.diego.budgetmensile.data.entity.BudgetGoal;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BudgetGoalDao_Impl implements BudgetGoalDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BudgetGoal> __insertionAdapterOfBudgetGoal;

  private final EntityDeletionOrUpdateAdapter<BudgetGoal> __deletionAdapterOfBudgetGoal;

  private final EntityDeletionOrUpdateAdapter<BudgetGoal> __updateAdapterOfBudgetGoal;

  public BudgetGoalDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBudgetGoal = new EntityInsertionAdapter<BudgetGoal>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `budget_goals` (`category`,`type`,`goal`,`month`,`year`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BudgetGoal entity) {
        statement.bindString(1, entity.getCategory());
        statement.bindString(2, entity.getType());
        statement.bindDouble(3, entity.getGoal());
        statement.bindLong(4, entity.getMonth());
        statement.bindLong(5, entity.getYear());
      }
    };
    this.__deletionAdapterOfBudgetGoal = new EntityDeletionOrUpdateAdapter<BudgetGoal>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `budget_goals` WHERE `category` = ? AND `type` = ? AND `month` = ? AND `year` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BudgetGoal entity) {
        statement.bindString(1, entity.getCategory());
        statement.bindString(2, entity.getType());
        statement.bindLong(3, entity.getMonth());
        statement.bindLong(4, entity.getYear());
      }
    };
    this.__updateAdapterOfBudgetGoal = new EntityDeletionOrUpdateAdapter<BudgetGoal>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `budget_goals` SET `category` = ?,`type` = ?,`goal` = ?,`month` = ?,`year` = ? WHERE `category` = ? AND `type` = ? AND `month` = ? AND `year` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BudgetGoal entity) {
        statement.bindString(1, entity.getCategory());
        statement.bindString(2, entity.getType());
        statement.bindDouble(3, entity.getGoal());
        statement.bindLong(4, entity.getMonth());
        statement.bindLong(5, entity.getYear());
        statement.bindString(6, entity.getCategory());
        statement.bindString(7, entity.getType());
        statement.bindLong(8, entity.getMonth());
        statement.bindLong(9, entity.getYear());
      }
    };
  }

  @Override
  public Object insert(final BudgetGoal g, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBudgetGoal.insert(g);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final BudgetGoal g, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfBudgetGoal.handle(g);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final BudgetGoal g, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfBudgetGoal.handle(g);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<BudgetGoal>> getByTypeAndMonth(final String t, final int m, final int y) {
    final String _sql = "SELECT * FROM budget_goals WHERE type=? AND month=? AND year=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, t);
    _argIndex = 2;
    _statement.bindLong(_argIndex, m);
    _argIndex = 3;
    _statement.bindLong(_argIndex, y);
    return __db.getInvalidationTracker().createLiveData(new String[] {"budget_goals"}, false, new Callable<List<BudgetGoal>>() {
      @Override
      @Nullable
      public List<BudgetGoal> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfGoal = CursorUtil.getColumnIndexOrThrow(_cursor, "goal");
          final int _cursorIndexOfMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "month");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final List<BudgetGoal> _result = new ArrayList<BudgetGoal>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BudgetGoal _item;
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final double _tmpGoal;
            _tmpGoal = _cursor.getDouble(_cursorIndexOfGoal);
            final int _tmpMonth;
            _tmpMonth = _cursor.getInt(_cursorIndexOfMonth);
            final int _tmpYear;
            _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            _item = new BudgetGoal(_tmpCategory,_tmpType,_tmpGoal,_tmpMonth,_tmpYear);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getGoal(final String c, final String t, final int m, final int y,
      final Continuation<? super Double> $completion) {
    final String _sql = "SELECT goal FROM budget_goals WHERE category=? AND type=? AND month=? AND year=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    _statement.bindString(_argIndex, c);
    _argIndex = 2;
    _statement.bindString(_argIndex, t);
    _argIndex = 3;
    _statement.bindLong(_argIndex, m);
    _argIndex = 4;
    _statement.bindLong(_argIndex, y);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            if (_cursor.isNull(0)) {
              _result = null;
            } else {
              _result = _cursor.getDouble(0);
            }
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTotalGoal(final String t, final int m, final int y,
      final Continuation<? super Double> $completion) {
    final String _sql = "SELECT SUM(goal) FROM budget_goals WHERE type=? AND month=? AND year=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, t);
    _argIndex = 2;
    _statement.bindLong(_argIndex, m);
    _argIndex = 3;
    _statement.bindLong(_argIndex, y);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAll(final Continuation<? super List<BudgetGoal>> $completion) {
    final String _sql = "SELECT * FROM budget_goals";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<BudgetGoal>>() {
      @Override
      @NonNull
      public List<BudgetGoal> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfGoal = CursorUtil.getColumnIndexOrThrow(_cursor, "goal");
          final int _cursorIndexOfMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "month");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final List<BudgetGoal> _result = new ArrayList<BudgetGoal>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BudgetGoal _item;
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final double _tmpGoal;
            _tmpGoal = _cursor.getDouble(_cursorIndexOfGoal);
            final int _tmpMonth;
            _tmpMonth = _cursor.getInt(_cursorIndexOfMonth);
            final int _tmpYear;
            _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            _item = new BudgetGoal(_tmpCategory,_tmpType,_tmpGoal,_tmpMonth,_tmpYear);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
