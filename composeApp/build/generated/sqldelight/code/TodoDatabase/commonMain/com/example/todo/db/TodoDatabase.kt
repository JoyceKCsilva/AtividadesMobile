package com.example.todo.db

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.example.todo.db.composeApp.newInstance
import com.example.todo.db.composeApp.schema
import kotlin.Unit

public interface TodoDatabase : Transacter {
  public val todoQueries: TodoQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = TodoDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): TodoDatabase = TodoDatabase::class.newInstance(driver)
  }
}
