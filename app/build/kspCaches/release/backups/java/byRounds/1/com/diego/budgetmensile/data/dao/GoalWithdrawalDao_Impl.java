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
import com.diego.budgetmensile.data.entity.GoalWithdrawal;
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
public final class GoalWithdrawalDao_Impl implements GoalWithdrawalDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<GoalWithdrawal> __insertionAdapterOfGoalWithdrawal;

  private final EntityDeletionOrUpdateAdapter<GoalWithdrawal> __deletionAdapterOfGoalWithdrawal;

  public GoalWithdrawalDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfGoalWithdrawal = new EntityInsertionAdapter<GoalWithdrawal>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `goal_withdrawals` (`id`,`goalId`,`goalName`,`amount`,`description`,`date`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final GoalWithdrawal entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getGoalId());
        statement.bindString(3, entity.getGoalName());
        statement.bindDouble(4, entity.getAmount());
        statement.bindString(5, entity.getDescription());
        statement.bindString(6, entity.getDate());
        statement.bindString(7, entity.getNotes());
      }
    };
    this.__deletionAdapterOfGoalWithdrawal = new EntityDeletionOrUpdateAdapter<GoalWithdrawal>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `goal_withdrawals` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final GoalWithdrawal entity) {
        statement.bindLong(1, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final GoalWithdrawal w, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfGoalWithdrawal.insert(w);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final GoalWithdrawal w, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfGoalWithdrawal.handle(w);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<GoalWithdrawal>> getByGoal(final int goalId) {
    final String _sql = "SELECT * FROM goal_withdrawals WHERE goalId = ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, goalId);
    return __db.getInvalidationTracker().createLiveData(new String[] {"goal_withdrawals"}, false, new Callable<List<GoalWithdrawal>>() {
      @Override
      @Nullable
      public List<GoalWithdrawal> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfGoalId = CursorUtil.getColumnIndexOrThrow(_cursor, "goalId");
          final int _cursorIndexOfGoalName = CursorUtil.getColumnIndexOrThrow(_cursor, "goalName");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<GoalWithdrawal> _result = new ArrayList<GoalWithdrawal>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GoalWithdrawal _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpGoalId;
            _tmpGoalId = _cursor.getInt(_cursorIndexOfGoalId);
            final String _tmpGoalName;
            _tmpGoalName = _cursor.getString(_cursorIndexOfGoalName);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new GoalWithdrawal(_tmpId,_tmpGoalId,_tmpGoalName,_tmpAmount,_tmpDescription,_tmpDate,_tmpNotes);
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
  public LiveData<Double> getTotalWithdrawnForGoal(final int goalId) {
    final String _sql = "SELECT SUM(amount) FROM goal_withdrawals WHERE goalId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, goalId);
    return __db.getInvalidationTracker().createLiveData(new String[] {"goal_withdrawals"}, false, new Callable<Double>() {
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
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<GoalWithdrawal>> getAll() {
    final String _sql = "SELECT * FROM goal_withdrawals ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"goal_withdrawals"}, false, new Callable<List<GoalWithdrawal>>() {
      @Override
      @Nullable
      public List<GoalWithdrawal> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfGoalId = CursorUtil.getColumnIndexOrThrow(_cursor, "goalId");
          final int _cursorIndexOfGoalName = CursorUtil.getColumnIndexOrThrow(_cursor, "goalName");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<GoalWithdrawal> _result = new ArrayList<GoalWithdrawal>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GoalWithdrawal _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpGoalId;
            _tmpGoalId = _cursor.getInt(_cursorIndexOfGoalId);
            final String _tmpGoalName;
            _tmpGoalName = _cursor.getString(_cursorIndexOfGoalName);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new GoalWithdrawal(_tmpId,_tmpGoalId,_tmpGoalName,_tmpAmount,_tmpDescription,_tmpDate,_tmpNotes);
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
  public Object getAllSync(final Continuation<? super List<GoalWithdrawal>> $completion) {
    final String _sql = "SELECT * FROM goal_withdrawals ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<GoalWithdrawal>>() {
      @Override
      @NonNull
      public List<GoalWithdrawal> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfGoalId = CursorUtil.getColumnIndexOrThrow(_cursor, "goalId");
          final int _cursorIndexOfGoalName = CursorUtil.getColumnIndexOrThrow(_cursor, "goalName");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<GoalWithdrawal> _result = new ArrayList<GoalWithdrawal>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GoalWithdrawal _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpGoalId;
            _tmpGoalId = _cursor.getInt(_cursorIndexOfGoalId);
            final String _tmpGoalName;
            _tmpGoalName = _cursor.getString(_cursorIndexOfGoalName);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new GoalWithdrawal(_tmpId,_tmpGoalId,_tmpGoalName,_tmpAmount,_tmpDescription,_tmpDate,_tmpNotes);
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

  @Override
  public LiveData<List<GoalWithdrawal>> getByYear(final String year) {
    final String _sql = "SELECT * FROM goal_withdrawals WHERE substr(date,1,4) = ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, year);
    return __db.getInvalidationTracker().createLiveData(new String[] {"goal_withdrawals"}, false, new Callable<List<GoalWithdrawal>>() {
      @Override
      @Nullable
      public List<GoalWithdrawal> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfGoalId = CursorUtil.getColumnIndexOrThrow(_cursor, "goalId");
          final int _cursorIndexOfGoalName = CursorUtil.getColumnIndexOrThrow(_cursor, "goalName");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<GoalWithdrawal> _result = new ArrayList<GoalWithdrawal>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GoalWithdrawal _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpGoalId;
            _tmpGoalId = _cursor.getInt(_cursorIndexOfGoalId);
            final String _tmpGoalName;
            _tmpGoalName = _cursor.getString(_cursorIndexOfGoalName);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new GoalWithdrawal(_tmpId,_tmpGoalId,_tmpGoalName,_tmpAmount,_tmpDescription,_tmpDate,_tmpNotes);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
