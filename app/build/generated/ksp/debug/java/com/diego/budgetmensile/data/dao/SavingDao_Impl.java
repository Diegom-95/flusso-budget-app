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
import com.diego.budgetmensile.data.entity.Saving;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Integer;
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
public final class SavingDao_Impl implements SavingDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Saving> __insertionAdapterOfSaving;

  private final EntityDeletionOrUpdateAdapter<Saving> __deletionAdapterOfSaving;

  private final EntityDeletionOrUpdateAdapter<Saving> __updateAdapterOfSaving;

  public SavingDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSaving = new EntityInsertionAdapter<Saving>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `savings` (`id`,`date`,`category`,`amount`,`isLongTerm`,`notes`,`month`,`year`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Saving entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getDate());
        statement.bindString(3, entity.getCategory());
        statement.bindDouble(4, entity.getAmount());
        final int _tmp = entity.isLongTerm() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindString(6, entity.getNotes());
        statement.bindLong(7, entity.getMonth());
        statement.bindLong(8, entity.getYear());
      }
    };
    this.__deletionAdapterOfSaving = new EntityDeletionOrUpdateAdapter<Saving>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `savings` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Saving entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfSaving = new EntityDeletionOrUpdateAdapter<Saving>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `savings` SET `id` = ?,`date` = ?,`category` = ?,`amount` = ?,`isLongTerm` = ?,`notes` = ?,`month` = ?,`year` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Saving entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getDate());
        statement.bindString(3, entity.getCategory());
        statement.bindDouble(4, entity.getAmount());
        final int _tmp = entity.isLongTerm() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindString(6, entity.getNotes());
        statement.bindLong(7, entity.getMonth());
        statement.bindLong(8, entity.getYear());
        statement.bindLong(9, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final Saving s, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSaving.insert(s);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Saving s, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSaving.handle(s);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Saving s, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSaving.handle(s);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<Saving>> getByMonth(final int m, final int y) {
    final String _sql = "SELECT * FROM savings WHERE month=? AND year=? ORDER BY category";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, m);
    _argIndex = 2;
    _statement.bindLong(_argIndex, y);
    return __db.getInvalidationTracker().createLiveData(new String[] {"savings"}, false, new Callable<List<Saving>>() {
      @Override
      @Nullable
      public List<Saving> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfIsLongTerm = CursorUtil.getColumnIndexOrThrow(_cursor, "isLongTerm");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "month");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final List<Saving> _result = new ArrayList<Saving>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Saving _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final boolean _tmpIsLongTerm;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsLongTerm);
            _tmpIsLongTerm = _tmp != 0;
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final int _tmpMonth;
            _tmpMonth = _cursor.getInt(_cursorIndexOfMonth);
            final int _tmpYear;
            _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            _item = new Saving(_tmpId,_tmpDate,_tmpCategory,_tmpAmount,_tmpIsLongTerm,_tmpNotes,_tmpMonth,_tmpYear);
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
  public LiveData<List<Saving>> getByYear(final int y) {
    final String _sql = "SELECT * FROM savings WHERE year=? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, y);
    return __db.getInvalidationTracker().createLiveData(new String[] {"savings"}, false, new Callable<List<Saving>>() {
      @Override
      @Nullable
      public List<Saving> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfIsLongTerm = CursorUtil.getColumnIndexOrThrow(_cursor, "isLongTerm");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "month");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final List<Saving> _result = new ArrayList<Saving>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Saving _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final boolean _tmpIsLongTerm;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsLongTerm);
            _tmpIsLongTerm = _tmp != 0;
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final int _tmpMonth;
            _tmpMonth = _cursor.getInt(_cursorIndexOfMonth);
            final int _tmpYear;
            _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            _item = new Saving(_tmpId,_tmpDate,_tmpCategory,_tmpAmount,_tmpIsLongTerm,_tmpNotes,_tmpMonth,_tmpYear);
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
  public LiveData<Double> getTotalByMonth(final int m, final int y) {
    final String _sql = "SELECT SUM(amount) FROM savings WHERE month=? AND year=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, m);
    _argIndex = 2;
    _statement.bindLong(_argIndex, y);
    return __db.getInvalidationTracker().createLiveData(new String[] {"savings"}, false, new Callable<Double>() {
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
  public Object exists(final String date, final String cat, final double amt, final int m,
      final int y, final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM savings WHERE date=? AND category=? AND ABS(amount-?)<0.001 AND month=? AND year=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 5);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    _argIndex = 2;
    _statement.bindString(_argIndex, cat);
    _argIndex = 3;
    _statement.bindDouble(_argIndex, amt);
    _argIndex = 4;
    _statement.bindLong(_argIndex, m);
    _argIndex = 5;
    _statement.bindLong(_argIndex, y);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
  public Object getByYearSync(final int y, final Continuation<? super List<Saving>> $completion) {
    final String _sql = "SELECT * FROM savings WHERE year=? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, y);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Saving>>() {
      @Override
      @NonNull
      public List<Saving> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfIsLongTerm = CursorUtil.getColumnIndexOrThrow(_cursor, "isLongTerm");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "month");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final List<Saving> _result = new ArrayList<Saving>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Saving _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final boolean _tmpIsLongTerm;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsLongTerm);
            _tmpIsLongTerm = _tmp != 0;
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final int _tmpMonth;
            _tmpMonth = _cursor.getInt(_cursorIndexOfMonth);
            final int _tmpYear;
            _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            _item = new Saving(_tmpId,_tmpDate,_tmpCategory,_tmpAmount,_tmpIsLongTerm,_tmpNotes,_tmpMonth,_tmpYear);
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
