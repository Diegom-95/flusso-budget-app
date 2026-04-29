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
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.diego.budgetmensile.data.entity.RecurringExpense;
import java.lang.Class;
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
public final class RecurringExpenseDao_Impl implements RecurringExpenseDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RecurringExpense> __insertionAdapterOfRecurringExpense;

  private final EntityDeletionOrUpdateAdapter<RecurringExpense> __deletionAdapterOfRecurringExpense;

  private final EntityDeletionOrUpdateAdapter<RecurringExpense> __updateAdapterOfRecurringExpense;

  private final SharedSQLiteStatement __preparedStmtOfUpdateFutureByName;

  public RecurringExpenseDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRecurringExpense = new EntityInsertionAdapter<RecurringExpense>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `recurring_expenses` (`id`,`name`,`category`,`amount`,`frequency`,`startDate`,`notes`,`isActive`,`isNeed`,`durationMonths`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecurringExpense entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getCategory());
        statement.bindDouble(4, entity.getAmount());
        statement.bindString(5, entity.getFrequency());
        statement.bindString(6, entity.getStartDate());
        statement.bindString(7, entity.getNotes());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(8, _tmp);
        final int _tmp_1 = entity.isNeed() ? 1 : 0;
        statement.bindLong(9, _tmp_1);
        statement.bindLong(10, entity.getDurationMonths());
      }
    };
    this.__deletionAdapterOfRecurringExpense = new EntityDeletionOrUpdateAdapter<RecurringExpense>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `recurring_expenses` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecurringExpense entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfRecurringExpense = new EntityDeletionOrUpdateAdapter<RecurringExpense>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `recurring_expenses` SET `id` = ?,`name` = ?,`category` = ?,`amount` = ?,`frequency` = ?,`startDate` = ?,`notes` = ?,`isActive` = ?,`isNeed` = ?,`durationMonths` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecurringExpense entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getCategory());
        statement.bindDouble(4, entity.getAmount());
        statement.bindString(5, entity.getFrequency());
        statement.bindString(6, entity.getStartDate());
        statement.bindString(7, entity.getNotes());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(8, _tmp);
        final int _tmp_1 = entity.isNeed() ? 1 : 0;
        statement.bindLong(9, _tmp_1);
        statement.bindLong(10, entity.getDurationMonths());
        statement.bindLong(11, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateFutureByName = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE expenses SET amount=?, category=? WHERE description=? AND date >= ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final RecurringExpense r, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRecurringExpense.insert(r);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final RecurringExpense r, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfRecurringExpense.handle(r);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final RecurringExpense r, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfRecurringExpense.handle(r);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateFutureByName(final String name, final double amount, final String category,
      final String fromDate, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateFutureByName.acquire();
        int _argIndex = 1;
        _stmt.bindDouble(_argIndex, amount);
        _argIndex = 2;
        _stmt.bindString(_argIndex, category);
        _argIndex = 3;
        _stmt.bindString(_argIndex, name);
        _argIndex = 4;
        _stmt.bindString(_argIndex, fromDate);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateFutureByName.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<RecurringExpense>> getAll() {
    final String _sql = "SELECT * FROM recurring_expenses ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"recurring_expenses"}, false, new Callable<List<RecurringExpense>>() {
      @Override
      @Nullable
      public List<RecurringExpense> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsNeed = CursorUtil.getColumnIndexOrThrow(_cursor, "isNeed");
          final int _cursorIndexOfDurationMonths = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMonths");
          final List<RecurringExpense> _result = new ArrayList<RecurringExpense>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecurringExpense _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpFrequency;
            _tmpFrequency = _cursor.getString(_cursorIndexOfFrequency);
            final String _tmpStartDate;
            _tmpStartDate = _cursor.getString(_cursorIndexOfStartDate);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsNeed;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsNeed);
            _tmpIsNeed = _tmp_1 != 0;
            final int _tmpDurationMonths;
            _tmpDurationMonths = _cursor.getInt(_cursorIndexOfDurationMonths);
            _item = new RecurringExpense(_tmpId,_tmpName,_tmpCategory,_tmpAmount,_tmpFrequency,_tmpStartDate,_tmpNotes,_tmpIsActive,_tmpIsNeed,_tmpDurationMonths);
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
  public LiveData<List<RecurringExpense>> getActive() {
    final String _sql = "SELECT * FROM recurring_expenses WHERE isActive=1 ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"recurring_expenses"}, false, new Callable<List<RecurringExpense>>() {
      @Override
      @Nullable
      public List<RecurringExpense> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsNeed = CursorUtil.getColumnIndexOrThrow(_cursor, "isNeed");
          final int _cursorIndexOfDurationMonths = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMonths");
          final List<RecurringExpense> _result = new ArrayList<RecurringExpense>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecurringExpense _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpFrequency;
            _tmpFrequency = _cursor.getString(_cursorIndexOfFrequency);
            final String _tmpStartDate;
            _tmpStartDate = _cursor.getString(_cursorIndexOfStartDate);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsNeed;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsNeed);
            _tmpIsNeed = _tmp_1 != 0;
            final int _tmpDurationMonths;
            _tmpDurationMonths = _cursor.getInt(_cursorIndexOfDurationMonths);
            _item = new RecurringExpense(_tmpId,_tmpName,_tmpCategory,_tmpAmount,_tmpFrequency,_tmpStartDate,_tmpNotes,_tmpIsActive,_tmpIsNeed,_tmpDurationMonths);
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
  public Object getActiveSync(final Continuation<? super List<RecurringExpense>> $completion) {
    final String _sql = "SELECT * FROM recurring_expenses WHERE isActive=1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RecurringExpense>>() {
      @Override
      @NonNull
      public List<RecurringExpense> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsNeed = CursorUtil.getColumnIndexOrThrow(_cursor, "isNeed");
          final int _cursorIndexOfDurationMonths = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMonths");
          final List<RecurringExpense> _result = new ArrayList<RecurringExpense>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecurringExpense _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpFrequency;
            _tmpFrequency = _cursor.getString(_cursorIndexOfFrequency);
            final String _tmpStartDate;
            _tmpStartDate = _cursor.getString(_cursorIndexOfStartDate);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final boolean _tmpIsNeed;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsNeed);
            _tmpIsNeed = _tmp_1 != 0;
            final int _tmpDurationMonths;
            _tmpDurationMonths = _cursor.getInt(_cursorIndexOfDurationMonths);
            _item = new RecurringExpense(_tmpId,_tmpName,_tmpCategory,_tmpAmount,_tmpFrequency,_tmpStartDate,_tmpNotes,_tmpIsActive,_tmpIsNeed,_tmpDurationMonths);
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
