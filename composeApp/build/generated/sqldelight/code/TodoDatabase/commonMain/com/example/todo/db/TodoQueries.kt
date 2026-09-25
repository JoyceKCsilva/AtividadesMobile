package com.example.todo.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Boolean
import kotlin.Long
import kotlin.String

public class TodoQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAllTasks(mapper: (
    id: String,
    title: String,
    description: String,
    completed: Boolean,
    dueDateTime: String?,
    createdAt: String,
    categoryId: String?,
    categoryName: String?,
  ) -> T): Query<T> = Query(-1_804_481_150, arrayOf("task", "category"), driver, "Todo.sq", "selectAllTasks", """
  |SELECT task.id, task.title, task.description, task.completed, task.dueDateTime, task.createdAt, task.categoryId, category.name AS categoryName
  |FROM task LEFT JOIN category ON task.categoryId = category.id
  |ORDER BY completed ASC, dueDateTime ASC, createdAt DESC
  """.trimMargin()) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getBoolean(3)!!,
      cursor.getString(4),
      cursor.getString(5)!!,
      cursor.getString(6),
      cursor.getString(7)
    )
  }

  public fun selectAllTasks(): Query<SelectAllTasks> = selectAllTasks(::SelectAllTasks)

  public fun <T : Any> selectTask(id: String, mapper: (
    id: String,
    title: String,
    description: String,
    completed: Boolean,
    dueDateTime: String?,
    createdAt: String,
    categoryId: String?,
    categoryName: String?,
  ) -> T): Query<T> = SelectTaskQuery(id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getBoolean(3)!!,
      cursor.getString(4),
      cursor.getString(5)!!,
      cursor.getString(6),
      cursor.getString(7)
    )
  }

  public fun selectTask(id: String): Query<SelectTask> = selectTask(id, ::SelectTask)

  public fun <T : Any> selectAllCategories(mapper: (id: String, name: String) -> T): Query<T> = Query(485_428_392, arrayOf("category"), driver, "Todo.sq", "selectAllCategories", "SELECT category.id, category.name FROM category ORDER BY name COLLATE NOCASE") { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!
    )
  }

  public fun selectAllCategories(): Query<Category> = selectAllCategories(::Category)

  /**
   * @return The number of rows updated.
   */
  public fun insertTask(
    id: String,
    title: String,
    description: String,
    completed: Boolean,
    dueDateTime: String?,
    createdAt: String,
    categoryId: String?,
  ): QueryResult<Long> {
    val result = driver.execute(-1_997_320_617, """
        |INSERT INTO task(id, title, description, completed, dueDateTime, createdAt, categoryId)
        |VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 7) {
          var parameterIndex = 0
          bindString(parameterIndex++, id)
          bindString(parameterIndex++, title)
          bindString(parameterIndex++, description)
          bindBoolean(parameterIndex++, completed)
          bindString(parameterIndex++, dueDateTime)
          bindString(parameterIndex++, createdAt)
          bindString(parameterIndex++, categoryId)
        }
    notifyQueries(-1_997_320_617) { emit ->
      emit("task")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun updateTask(
    title: String,
    description: String,
    completed: Boolean,
    dueDateTime: String?,
    categoryId: String?,
    id: String,
  ): QueryResult<Long> {
    val result = driver.execute(1_035_549_799, """
        |UPDATE task SET title = ?, description = ?, completed = ?, dueDateTime = ?, categoryId = ?
        |WHERE id = ?
        """.trimMargin(), 6) {
          var parameterIndex = 0
          bindString(parameterIndex++, title)
          bindString(parameterIndex++, description)
          bindBoolean(parameterIndex++, completed)
          bindString(parameterIndex++, dueDateTime)
          bindString(parameterIndex++, categoryId)
          bindString(parameterIndex++, id)
        }
    notifyQueries(1_035_549_799) { emit ->
      emit("task")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteTask(id: String): QueryResult<Long> {
    val result = driver.execute(-1_198_897_079, """DELETE FROM task WHERE id = ?""", 1) {
          var parameterIndex = 0
          bindString(parameterIndex++, id)
        }
    notifyQueries(-1_198_897_079) { emit ->
      emit("task")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertCategory(id: String, name: String): QueryResult<Long> {
    val result = driver.execute(1_124_277_456, """INSERT INTO category(id, name) VALUES (?, ?)""", 2) {
          var parameterIndex = 0
          bindString(parameterIndex++, id)
          bindString(parameterIndex++, name)
        }
    notifyQueries(1_124_277_456) { emit ->
      emit("category")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun updateCategory(name: String, id: String): QueryResult<Long> {
    val result = driver.execute(671_318_752, """UPDATE category SET name = ? WHERE id = ?""", 2) {
          var parameterIndex = 0
          bindString(parameterIndex++, name)
          bindString(parameterIndex++, id)
        }
    notifyQueries(671_318_752) { emit ->
      emit("category")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteCategory(id: String): QueryResult<Long> {
    val result = driver.execute(2_043_137_474, """DELETE FROM category WHERE id = ?""", 1) {
          var parameterIndex = 0
          bindString(parameterIndex++, id)
        }
    notifyQueries(2_043_137_474) { emit ->
      emit("category")
      emit("task")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun clearCategoryFromTasks(categoryId: String?): QueryResult<Long> {
    val result = driver.execute(null, """UPDATE task SET categoryId = NULL WHERE categoryId ${ if (categoryId == null) "IS" else "=" } ?""", 1) {
          var parameterIndex = 0
          bindString(parameterIndex++, categoryId)
        }
    notifyQueries(-1_101_982_510) { emit ->
      emit("task")
    }
    return result
  }

  private inner class SelectTaskQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("task", "category", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("task", "category", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(-324_475_110, """
    |SELECT task.id, task.title, task.description, task.completed, task.dueDateTime, task.createdAt, task.categoryId, category.name AS categoryName
    |FROM task LEFT JOIN category ON task.categoryId = category.id
    |WHERE task.id = ?
    """.trimMargin(), mapper, 1) {
      var parameterIndex = 0
      bindString(parameterIndex++, id)
    }

    override fun toString(): String = "Todo.sq:selectTask"
  }
}
