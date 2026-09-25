package com.example.todo

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class Category(val id: String, val name: String)

data class Task(
    val id: String,
    val title: String = "",
    val description: String = "",
    val completed: Boolean = false,
    val dueDateTime: Instant? = null,
    val createdAt: Instant = Clock.System.now(),
    val categoryId: String? = null,
    val categoryName: String? = null
)

enum class StatusFilter { ALL, PENDING, COMPLETED }
