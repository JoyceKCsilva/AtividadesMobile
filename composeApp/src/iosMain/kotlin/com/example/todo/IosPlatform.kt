package com.example.todo

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.todo.db.TodoDatabase
import kotlinx.datetime.Instant
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

actual fun createDatabaseDriver(): SqlDriver = NativeSqliteDriver(TodoDatabase.Schema, "todo.db")

actual fun createNotificationScheduler(): NotificationScheduler = IosNotificationScheduler()

private class IosNotificationScheduler : NotificationScheduler {
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override fun schedule(task: Task) {
        val due = task.dueDateTime ?: return
        val seconds = due.toEpochMilliseconds() / 1000.0 -
            NSDate().timeIntervalSince1970
        if (seconds <= 0) return
        val content = UNMutableNotificationContent().apply {
            title = "Lembrete de tarefa"
            body = "Task reminder: ${task.title}"
            sound = UNNotificationSound.defaultSound
        }
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(seconds, false)
        center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(task.id, content, trigger), null)
    }

    override fun cancel(taskId: String) {
        center.removePendingNotificationRequestsWithIdentifiers(listOf(taskId))
    }
}
