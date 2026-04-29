package com.diego.budgetmensile.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Prelievo da un obiettivo di risparmio.
 * Scala il saldo disponibile dell'obiettivo senza toccare
 * i record di risparmio originali e senza comparire
 * come spesa ordinaria nel bilancio mensile.
 */
@Entity(tableName = "goal_withdrawals")
data class GoalWithdrawal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,
    val goalName: String,       // denormalizzato per visualizzazione rapida
    val amount: Double,
    val description: String,    // es. "Vacanza Sardegna luglio"
    val date: String,           // "yyyy-MM-dd"
    val notes: String = ""
)
