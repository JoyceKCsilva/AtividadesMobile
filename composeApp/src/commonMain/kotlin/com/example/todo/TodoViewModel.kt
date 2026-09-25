package com.example.todo

import androidx.lifecycle.ViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class TodoState(
    val tasks: List<Task> = emptyList(),
    val categories: List<Category> = emptyList(),
    val status: StatusFilter = StatusFilter.ALL,
    val categoryId: String? = null,
    val message: String? = null
) {
    val visibleTasks: List<Task>
        get() = tasks.filter { status == StatusFilter.ALL ||
            (status == StatusFilter.COMPLETED && it.completed) ||
            (status == StatusFilter.PENDING && !it.completed) }
            .filter { categoryId == null || it.categoryId == categoryId }
}

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {
    private val _state = MutableStateFlow(TodoState())
    val state: StateFlow<TodoState> = _state

    init { refresh() }

    fun refresh() {
        _state.update { it.copy(tasks = repository.tasks(), categories = repository.categories()) }
    }

    fun setStatus(status: StatusFilter) = _state.update { it.copy(status = status) }
    fun setCategory(id: String?) = _state.update { it.copy(categoryId = id) }

    fun saveTask(task: Task): Boolean = runCatching {
        require(task.title.isNotBlank()) { "O título é obrigatório." }
        repository.save(task.copy(title = task.title.trim()))
        refresh()
        true
    }.getOrElse {
        _state.update { state -> state.copy(message = it.message ?: "Não foi possível salvar a tarefa.") }
        false
    }

    fun deleteTask(task: Task) {
        repository.delete(task)
        refresh()
    }

    fun toggleTask(task: Task) {
        repository.toggleCompleted(task)
        refresh()
    }

    fun addCategory(name: String): Boolean = runCatching {
        repository.addCategory(name)
        refresh()
        true
    }.getOrElse {
        _state.update { state -> state.copy(message = "Informe um nome de categoria.") }
        false
    }

    fun renameCategory(category: Category, name: String) {
        runCatching { repository.renameCategory(category, name); refresh() }
            .onFailure { _state.update { state -> state.copy(message = "Nome de categoria inválido.") } }
    }

    fun deleteCategory(category: Category) {
        repository.deleteCategory(category)
        refresh()
    }

    fun newTask() = Task(id = "${Clock.System.now().toEpochMilliseconds()}", createdAt = Clock.System.now())
    fun findTask(id: String): Task? = _state.value.tasks.firstOrNull { it.id == id }
    fun clearMessage() = _state.update { it.copy(message = null) }
}
