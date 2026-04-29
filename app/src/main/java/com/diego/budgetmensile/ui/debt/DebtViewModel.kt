package com.diego.budgetmensile.ui.debt

import android.app.Application
import androidx.lifecycle.*
import com.diego.budgetmensile.data.BudgetRepository
import com.diego.budgetmensile.data.entity.Debt
import com.diego.budgetmensile.data.entity.DebtGoal
import kotlinx.coroutines.launch

class DebtViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = BudgetRepository(app)

    val debtGoals: LiveData<List<DebtGoal>> = repo.getAllDebtGoals()

    // Mappa nome_debito → totale_pagato (aggiornata manualmente)
    val paidMap = mutableMapOf<String, Double>()

    fun refreshPaid() {
        debtGoals.value?.forEach { goal ->
            repo.getTotalPaidForGoal(goal.name).observeForever { paid ->
                paidMap[goal.name] = paid ?: 0.0
            }
        }
    }

    fun addGoal(g: DebtGoal)    = viewModelScope.launch { repo.insertDebtGoal(g); refreshPaid() }
    fun updateGoal(g: DebtGoal) = viewModelScope.launch { repo.updateDebtGoal(g) }
    fun deleteGoal(g: DebtGoal) = viewModelScope.launch { repo.deleteDebtGoal(g) }

    fun recordPayment(payment: Debt, goal: DebtGoal) = viewModelScope.launch {
        repo.insertDebt(payment)
        val paid = (paidMap[goal.name] ?: 0.0) + payment.amount
        paidMap[goal.name] = paid
        // Se ha finito di pagare, segna come inattivo
        if (paid >= goal.totalAmount) {
            repo.updateDebtGoal(goal.copy(isActive = false))
        }
        refreshPaid()
    }
}
