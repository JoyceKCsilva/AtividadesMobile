package com.example.todo.db

import kotlin.Boolean
import kotlin.String

public data class SelectTask(
  public val id: String,
  public val title: String,
  public val description: String,
  public val completed: Boolean,
  public val dueDateTime: String?,
  public val createdAt: String,
  public val categoryId: String?,
  public val categoryName: String?,
)
