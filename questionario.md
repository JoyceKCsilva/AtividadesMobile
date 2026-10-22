# Mobile App Reverse-Engineering Questions

Answer the following questions based on the code generated during the exercise.

You may use the coding agent to help investigate the project, but you must inspect the source code and verify the answers yourself.

Whenever possible, mention the relevant files, classes, functions, or components.

---

## 1. Project Structure

What are the main parts of the project, and where can you find:
A interface principal fica em composeApp/src/commonMain/kotlin/com/example/todo/App.kt e a função principal da interface é "TodoApp()".

- UI/screens;
  - As telas são controladas pelo enum privado: private enum class Screen { LIST, EDITOR, CATEGORIES }
- data models;
  - Os models estão em composeApp/src/commonMain/kotlin/com/example/todo/Model.kt
  - Os princiapis modelos são:
    - Task: representa uma tarefa;
    - Category: representa uma categoria;
    - StatusFilter: representa os filtros ALL, PENDING e COMPLETED
- SQLite/database code;
  - EStá localoizado em: composeApp/src/commonMain/sqldelight/com/example/todo/db/Todo.sq
  - É esse arquivo que define as tabelas de task e categorias e também s consultas SQL de criação, leitura, atualização e exclusão.
- navigation;
  - A navegação é controlada diretamente em TodoApp(), usando o enum Screen e estados locais do Compose.
- notification code?
  - A abstração comum fica em composeApp/src/commonMain/kotlin/com/example/todo/Platform.kt e as implemetações especificas para cada OS em AndroidPlatform.kt, para Android e IosPlatform.kt, para iOS.

Briefly describe how the project is organized.

composeApp/
├── src/
│   ├── commonMain/
│   │   ├── kotlin/
│   │   │   └── com/example/todo/
│   │   │       ├── App.kt
│   │   │       ├── Model.kt
│   │   │       ├── Platform.kt
│   │   │       ├── TodoRepository.kt
│   │   │       └── TodoViewModel.kt
│   │   └── sqldelight/
│   │       └── .../Todo.sq
│   ├── androidMain/
│   │   └── .../AndroidPlatform.kt
│   └── iosMain/
│       └── .../IosPlatform.kt

---

## 2. Architecture and State

How is application state managed?
O projeto utiliza uma abordagem semelhante a MVVM

Explain how the UI is updated after an operation such as:

- creating a task;
- editing a task;
- marking a task as completed.

Does the project use any recognizable architectural pattern or state-management approach?

---

## 3. SQLite Persistence

How is SQLite used in the application?

Identify:

- where the database is created;
- how tasks and categories are stored;
- where create, read, update, and delete operations are implemented.

---

## 4. Follow One Operation

Trace what happens when the user creates a new task.

Start from pressing **Save** and follow the execution until:

1. the task is stored in SQLite;
2. the task appears in the task list;
3. a notification is scheduled, if a due date exists.

Describe the main functions/components involved.

---

## 5. Navigation

How does navigation between screens work?

In particular:

- how does the app navigate from the task list to the task editor?
- when editing a task, what information is passed between screens?

For example: task ID, full object, shared state, or another approach.

---

## 6. Notifications

How are task reminders implemented?

Explain:

- how a notification is scheduled;
- how it is associated with a task;
- what happens when the due date changes;
- what happens when the task is completed or deleted.

---

## 7. Agent Decisions

Identify at least **two important decisions made by the coding agent that were not explicitly specified in the assignment**.

Examples:

- architecture;
- libraries;
- state-management strategy;
- navigation approach;
- SQLite abstraction;
- project structure.

For each one, explain what the agent chose.

---

## 8. BUILD_LOG Analysis

Using `BUILD_LOG.md`, identify:

- one problem or bug encountered during development;
- how the agent attempted to solve it;
- whether the first solution worked;
- what was eventually done.

Then answer:

**What did the build log help you understand that would have been harder to discover by looking only at the final code?**
