package com.diego.budgetmensile.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.diego.budgetmensile.data.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import java.util.Calendar

class RecurringNotificationWorker(
    private val ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    companion object {
        const val CHANNEL_ID = "flusso_recurring"
        const val WORK_NAME  = "RecurringNotificationWork"

        fun schedule(context: Context) {
            val now    = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val delay = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<RecurringNotificationWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            createChannel()

            // Usa BudgetRepository invece di AppDatabase direttamente
            val repo      = BudgetRepository(ctx)
            val tomorrow  = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            val tMonth    = tomorrow.get(Calendar.MONTH) + 1

            val recurring = repo.getActiveRecurringSync()
            val due = recurring.filter { r ->
                when (r.frequency) {
                    "MONTHLY"   -> true
                    "ANNUAL"    -> {
                        val startMonth = r.startDate.substring(5, 7).toIntOrNull() ?: 0
                        startMonth == tMonth
                    }
                    "QUARTERLY" -> {
                        val startMonth = r.startDate.substring(5, 7).toIntOrNull() ?: 0
                        (tMonth - startMonth) % 3 == 0
                    }
                    else -> false
                }
            }

            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            due.forEachIndexed { i, r ->
                val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("💳 Scadenza domani")
                    .setContentText("${r.name}: -${"%.2f".format(r.amount)} €")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()
                nm.notify(1000 + i, notif)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun createChannel() {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(NotificationChannel(
                CHANNEL_ID, "Spese Ricorrenti",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Promemoria scadenze ricorrenti" })
        }
    }
}