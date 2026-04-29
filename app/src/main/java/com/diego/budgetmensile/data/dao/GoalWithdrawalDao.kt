package com.diego.budgetmensile.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.diego.budgetmensile.data.entity.GoalWithdrawal

@Dao interface GoalWithdrawalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(w: GoalWithdrawal)
    @Delete suspend fun delete(w: GoalWithdrawal)

    @Query("SELECT * FROM goal_withdrawals WHERE goalId = :goalId ORDER BY date DESC")
    fun getByGoal(goalId: Int): LiveData<List<GoalWithdrawal>>

    @Query("SELECT SUM(amount) FROM goal_withdrawals WHERE goalId = :goalId")
    fun getTotalWithdrawnForGoal(goalId: Int): LiveData<Double?>

    @Query("SELECT * FROM goal_withdrawals ORDER BY date DESC")
    fun getAll(): LiveData<List<GoalWithdrawal>>

    @Query("SELECT * FROM goal_withdrawals ORDER BY date DESC")
    suspend fun getAllSync(): List<GoalWithdrawal>

    @Query("SELECT * FROM goal_withdrawals WHERE substr(date,1,4) = :year ORDER BY date DESC")
    fun getByYear(year: String): LiveData<List<GoalWithdrawal>>
}