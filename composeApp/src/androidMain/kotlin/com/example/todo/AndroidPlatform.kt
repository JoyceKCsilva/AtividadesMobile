package com.example.todo

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import com.example.todo.db.TodoDatabase

lateinit var todoApplicationContext: Context

const val TASK_REMINDER_CHANNEL_ID = "task_reminders"

actual fun createDatabaseDriver(): SqlDriver =
    AndroidSqliteDriver(TodoDatabase.Schema, todoApplicationContext, "todo.db")

actual fun createNotificationScheduler(): NotificationScheduler = AndroidNotificationScheduler()

fun createTaskReminderChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            TASK_REMINDER_CHANNEL_ID,
            "Lembretes de tarefas",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificações dos prazos das tarefas"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
}

private class AndroidNotificationScheduler : NotificationScheduler {
    override fun schedule(task: Task) {
        val due = task.dueDateTime ?: return
        val trigger = due.toEpochMilliseconds()
        if (trigger <= System.currentTimeMillis()) return
        createTaskReminderChannel(todoApplicationContext)
        val intent = Intent(todoApplicationContext, ReminderReceiver::class.java)
            .putExtra("taskId", task.id).putExtra("title", task.title)
        val pending = PendingIntent.getBroadcast(
            todoApplicationContext, task.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = todoApplicationContext.getSystemService(AlarmManager::class.java)
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()
            ) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
        }
    }

    override fun cancel(taskId: String) {
        val intent = Intent(todoApplicationContext, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            todoApplicationContext, taskId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        todoApplicationContext.getSystemService(AlarmManager::class.java).cancel(pending)
        NotificationManagerCompat.from(todoApplicationContext)
            .cancel(taskId.hashCode())
    }
}

class ReminderReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        createTaskReminderChannel(context)
        val taskId = intent.getStringExtra("taskId") ?: return
        val notification = NotificationCompat.Builder(context, TASK_REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Lembrete de tarefa")
            .setContentText("Task reminder: ${intent.getStringExtra("title").orEmpty()}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        try {
            NotificationManagerCompat.from(context)
                .notify(taskId.hashCode(), notification)
        } catch (_: SecurityException) {
            // Notification permission denial is a supported, non-fatal outcome.
        }
    }
}
