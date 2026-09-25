package com.example.todo.db.composeApp

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.example.todo.db.TodoDatabase
import com.example.todo.db.TodoQueries
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<TodoDatabase>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = TodoDatabaseImpl.Schema

internal fun KClass<TodoDatabase>.newInstance(driver: SqlDriver): TodoDatabase = TodoDatabaseImpl(driver)

private class TodoDatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver),
    TodoDatabase {
  override val todoQueries: TodoQueries = TodoQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE category (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    name TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE task (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    title TEXT NOT NULL,
          |    description TEXT NOT NULL DEFAULT '',
          |    completed INTEGER NOT NULL DEFAULT 0,
          |    dueDateTime TEXT,
          |    createdAt TEXT NOT NULL,
          |    categoryId TEXT REFERENCES category(id) ON DELETE SET NULL
          |)
          """.trimMargin(), 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
