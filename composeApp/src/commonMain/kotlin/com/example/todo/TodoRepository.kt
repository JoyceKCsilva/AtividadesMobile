package com.example.todo

import com.example.todo.db.TodoDatabase
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.random.Random

class TodoRepository(
    private val database: TodoDatabase,
    private val notifications: NotificationScheduler
) {
    private val queries get() = database.todoQueries

    fun tasks(): List<Task> = queries.selectAllTasks().executeAsList().map {
        Task(it.id, it.title, it.description, it.completed, it.dueDateTime?.let(Instant::parse),
            Instant.parse(it.createdAt), it.categoryId, it.categoryName)
    }

    fun categories(): List<Category> = queries.selectAllCategories().executeAsList()
        .map { Category(it.id, it.name) }

    fun save(task: Task) {
        if (queries.selectTask(task.id).executeAsOneOrNull() == null) {
            queries.insertTask(task.id, task.title.trim(), task.description.trim(), task.completed,
                task.dueDateTime?.toString(), task.createdAt.toString(), task.categoryId)
        } else {
            queries.updateTask(task.title.trim(), task.description.trim(), task.completed,
                task.dueDateTime?.toString(), task.categoryId, task.id)
        }
        notifications.cancel(task.id)
        if (!task.completed && task.dueDateTime != null) notifications.schedule(task)
    }

    fun delete(task: Task) {
        queries.deleteTask(task.id)
        notifications.cancel(task.id)
    }

    fun toggleCompleted(task: Task) = save(task.copy(completed = !task.completed))

    fun addCategory(name: String) {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty())
        queries.insertCategory(randomId(), cleanName)
    }

    fun renameCategory(category: Category, name: String) {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty())
        queries.updateCategory(cleanName, category.id)
    }

    fun deleteCategory(category: Category) {
        queries.clearCategoryFromTasks(category.id)
        queries.deleteCategory(category.id)
    }

    private fun randomId(): String = "${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt()}"
}
