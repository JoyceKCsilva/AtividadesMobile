package com.example.todo

import app.cash.sqldelight.db.SqlDriver

expect fun createDatabaseDriver(): SqlDriver

interface NotificationScheduler {
    fun schedule(task: Task)
    fun cancel(taskId: String)
}

expect fun createNotificationScheduler(): NotificationScheduler
