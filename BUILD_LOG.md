# Development Log

## 2026-09-24 — Initial project setup

### Prompt / Request
Leia o arquivo "instrucoes.md". nele há todas as instruções para que você desenvolva uma aplicação em kotlin multplatform

### Decision Summary
The existing directory was an IntelliJ Java starter project, so it was converted into a Kotlin Multiplatform project with Compose Multiplatform. Shared business logic and UI live in `composeApp`; platform source sets provide database drivers and local notification scheduling. SQLDelight was selected for SQLite persistence, and a small repository/ViewModel layer keeps state updates predictable.

### Actions Performed
- Created the Gradle Kotlin Multiplatform project structure.
- Added `BUILD_LOG.md` as required by the specification.
- Selected Compose Multiplatform, SQLDelight, and AndroidX lifecycle dependencies.

### Result
Project foundation created; feature implementation follows.

### Problems / Errors
No build tool or Gradle wrapper was present in the original project.

### Fixes Attempted
Created a self-contained Gradle configuration so the project can be opened and built from Android Studio/IntelliJ with Gradle support.

### Current Status
Partially completed

## 2026-09-24 — Core application implementation

### Prompt / Request
Implement the Kotlin Multiplatform mobile to-do application described in `instrucoes.md`.

### Decision Summary
The application uses a repository over SQLDelight-generated SQLite queries, a `TodoViewModel` backed by `StateFlow`, and a single Compose UI with explicit list, editor, and category-management screens. Existing task IDs are passed through shared state rather than serializing full task objects. Category deletion uses SQLite `ON DELETE SET NULL`, so tasks remain available as uncategorized.

### Actions Performed
- Added task and category models and SQLDelight schema/queries.
- Added create, edit, delete, complete/reopen, and filtering operations.
- Added task editor validation for required titles and ISO due dates.
- Added category creation, rename, deletion, and category filtering.
- Added Android and iOS SQLite drivers.
- Added Android AlarmManager/notification-channel scheduling and iOS UserNotifications scheduling.
- Added notification cancellation when tasks are completed, changed, or deleted.
- Added Android 13+ notification permission request and graceful scheduling no-op when permission is denied.
- Added Compose screens and navigation between list, editor, and category management.

### Result
The requested application structure and feature flows are implemented in the KMP source sets.

### Problems / Errors
The original directory did not contain Gradle or a Gradle wrapper, so a local compilation could not be run from this environment. Android/iOS SDK availability is also unknown.

### Fixes Attempted
Added explicit dependency versions and platform-specific source sets so Android Studio can resolve and build the project.

### Current Status
Needs testing

## 2026-09-25 — Architecture explanation

### Prompt / Request
me esplique toda a arquitetura deste projeto. onde está cada coisa, como funciona o banco de dados. onde cada coisa está implementada no codigo (as notificações por exemplo)

### Decision Summary
Provided a structured explanation of the Kotlin Multiplatform architecture, source sets, Compose UI, ViewModel/repository flow, SQLDelight schema and generated database API, navigation, filtering, and platform notification implementations.

### Actions Performed
- Documented the architecture and code locations for the user.

### Result
The project structure and runtime data flows are explained without changing application behavior.

### Problems / Errors
No new implementation error was reported in this interaction.

### Fixes Attempted
No source fix was necessary.

### Current Status
Needs testing

## 2026-09-25 — Android notification reliability fix

### Prompt / Request
as notificações não estão funcionando, corrija

### Decision Summary
Android notification delivery needed stronger platform handling. The channel is now created when the activity starts, exact alarms are used when permitted, and an inexact idle-compatible alarm is used as a safe fallback when Android 12+ exact-alarm access is unavailable. Cancellation also removes any already-posted notification.

### Actions Performed
- Added a high-importance notification channel initialized at app startup and before scheduling.
- Added `SCHEDULE_EXACT_ALARM` manifest permission.
- Added Android 12+ exact-alarm capability detection.
- Added fallback scheduling with `setAndAllowWhileIdle` when exact alarms are denied or unavailable.
- Added notification priority/category and defensive task-ID handling in the receiver.
- Added notification cancellation alongside alarm cancellation.

### Result
Notifications no longer depend exclusively on special exact-alarm access, and Android notification channels are guaranteed to exist before notifications are posted.

### Problems / Errors
The updated APK must be rebuilt and reinstalled. Existing installed versions will not contain these native changes.

### Fixes Attempted
Kept permission denial non-fatal and preserved the existing future-date and completed-task rules.

### Current Status
Needs testing

## 2026-09-25 — Date formatting compiler fix

### Prompt / Request
a compilação apresentouum erro: `Unresolved reference 'day'`

### Decision Summary
The `kotlinx.datetime.LocalDate` API exposes the day component as `dayOfMonth`; the formatter was using an invalid `day` property.

### Actions Performed
- Replaced `local.date.day` with `local.date.dayOfMonth` in the task deadline formatter.

### Result
The reported unresolved-reference error is corrected without changing deadline persistence or display format.

### Problems / Errors
The build must be rerun in the user's environment to validate the complete project.

### Fixes Attempted
Applied the minimal source correction.

### Current Status
Needs testing

## 2026-09-25 — Interface hierarchy and category colors

### Prompt / Request
melhore a interface do app, separe as categorias por cores e coloque elas dentro de botões igual os outros. a distribuição do interior dos cards com as tarefas também não tá legal, não tem hierarquia alem de que o praso está estranho.

### Decision Summary
The UI was improved with Material 3 chips and stronger information hierarchy while keeping the existing data model. Category colors are deterministic from the category identifier/name, so the same category keeps its color without requiring a schema migration. Stored instants are formatted in the device timezone as `dd/MM/yyyy às HH:mm`.

### Actions Performed
- Replaced plain category text filters with colored `AssistChip` buttons.
- Added colored category chips inside task cards.
- Added status chips for pending/completed tasks.
- Reorganized cards into title, optional description, metadata chips, and deadline sections.
- Added completed-card surface styling and clearer edit action text.
- Formatted due dates using the local device timezone instead of displaying raw ISO instants.

### Result
The task list has clearer visual hierarchy, categories are visually distinct and clickable, and deadlines are easier to read.

### Problems / Errors
The updated interface still needs to be compiled and tested on the Android device.

### Fixes Attempted
Removed an accidental duplicate import during static review.

### Current Status
Needs testing

## 2026-09-25 — Separate date and time pickers

### Prompt / Request
a parte de selecionar data e hora estão juntas no mesmo bloco. separe as duas e quero que abra um calendariozinho pra selecionar a data e um relogio pra selecionar a hora

### Decision Summary
The editor now presents independent date and time controls using Compose Material 3 `DatePicker` and `TimePicker`. The database continues storing one optional instant; saving requires both values when a deadline is being used, while clearing both removes the deadline and its notification.

### Actions Performed
- Replaced the ISO date-time text field with separate “Selecionar data” and “Selecionar hora” buttons.
- Added a calendar dialog for date selection.
- Added a clock-style time dialog using a 24-hour `TimePicker`.
- Added a “Remover prazo” action.
- Added conversion between local device date/time and the persisted UTC instant.
- Added validation preventing a deadline with only one of the two components.

### Result
Users can select the date and time independently through native-looking Material 3 picker controls.

### Problems / Errors
The updated source still needs to be compiled in the user's Gradle environment.

### Fixes Attempted
Kept notification scheduling unchanged by converting the selected local values back to `Instant` before saving.

### Current Status
Needs testing

## 2026-09-25 — D8 bytecode compatibility fix

### Prompt / Request
a compilação falhou outra vez, verifique mais uma vez o erro que apareceu

### Decision Summary
The failure occurs in D8, not in Kotlin source compilation. `Unsupported class file major version 69` means Kotlin emitted Java 25 bytecode, while the configured Android Gradle Plugin/R8 toolchain cannot process it. Android bytecode was pinned to JVM 17, the supported baseline for this project.

### Actions Performed
- Set the Android Kotlin compiler `jvmTarget` to `JvmTarget.JVM_17`.
- Set Android Java source and target compatibility to Java 17.

### Result
The generated Android classes, including notification and Compose resources classes, will be emitted as Java 17 bytecode instead of Java 25 bytecode.

### Problems / Errors
The build must be rerun after the configuration change. The warnings about deprecated `Unsafe` and native library stripping are unrelated to this failure.

### Fixes Attempted
Applied the compatibility fix in `composeApp/build.gradle.kts`.

### Current Status
Needs testing

## 2026-09-25 — Validation attempt after compiler fixes

### Prompt / Request
Validate the corrections against the Android debug build.

### Decision Summary
The same Gradle task supplied by the user was used so the result would cover the Android source set and generated SQLDelight code.

### Actions Performed
- Ran `.\gradlew.bat :composeApp:assembleDebug --stacktrace`.

### Result
The command did not reach compilation in this shell because Java is unavailable here.

### Problems / Errors
Gradle reported: `JAVA_HOME is not set and no 'java' command could be found in your PATH`.

### Fixes Attempted
The source fixes were already applied; the next validation must run in the user's configured Java environment.

### Current Status
Needs testing

## 2026-09-25 — Testing without Android Studio

### Prompt / Request
tem como rodar no celular sem precisar do android studio?

### Decision Summary
Yes. The application can be built and installed from the command line, but Android Studio itself is not required only if the JDK, Gradle, Android SDK command-line tools, platform SDK, build tools, and ADB are installed and configured.

### Actions Performed
- Explained the command-line APK build and USB installation workflow.

### Result
The project can be tested without opening Android Studio by generating a debug APK with Gradle and installing it using ADB.

### Problems / Errors
This project currently has no Gradle wrapper, and the current environment does not have Java, Gradle, Android SDK, or ADB available.

### Fixes Attempted
No source changes were required for this guidance request.

### Current Status
Needs testing

## 2026-09-25 — Android compilation fixes

### Prompt / Request
este erro está aparecendo na compilação corrija

### Decision Summary
The reported errors were source-level compatibility issues: SQLDelight 2.x exposes the Android driver under `app.cash.sqldelight.driver.android`, the repository needed its `Clock` import, new tasks needed an empty default title, and Material 3 `TopAppBar` requires an explicit experimental API opt-in.

### Actions Performed
- Corrected the SQLDelight Android driver import.
- Added the missing `kotlinx.datetime.Clock` import.
- Added a default empty title to `Task` so the new-task factory compiles.
- Opted `TodoApp` into `ExperimentalMaterial3Api`.

### Result
All four compiler errors shown in the provided Gradle output were addressed. The deprecated `Unsafe` and native-library stripping messages are warnings, not compilation failures.

### Problems / Errors
The build has not yet been rerun after these corrections in this environment.

### Fixes Attempted
Applied targeted source changes without altering persistence or notification behavior.

### Current Status
Needs testing

## 2026-09-24 — Device testing instructions

### Prompt / Request
como faço para testar a aplicação no meu celular?

### Decision Summary
The recommended validation path is Android Studio with a physical Android device or emulator. iOS testing requires macOS with Xcode because Apple device builds cannot be produced on Windows.

### Actions Performed
- Documented the Android Studio, SDK, USB debugging, Gradle sync, and run steps in the project handoff.

### Result
The application can be tested on Android after installing the required mobile development toolchain.

### Problems / Errors
The current Windows environment does not have Java, Gradle, Kotlin, or Android SDK tools available, so this shell cannot launch the application directly.

### Fixes Attempted
No source changes were needed for this guidance request.

### Current Status
Needs testing

## 2026-09-24 — Final static cleanup

### Prompt / Request
Complete the implementation and leave the project ready for toolchain validation.

### Decision Summary
Removed unused code and narrowed notification-permission handling to the expected `SecurityException`, avoiding accidental suppression of unrelated platform errors.

### Actions Performed
- Removed unused Compose coroutine and Gradle imports.
- Kept notification permission denial non-fatal without masking other failures.

### Result
The source tree is ready to be opened and built with the configured Kotlin/Android toolchain.

### Problems / Errors
No local compiler was available for a build or instrumented runtime test.

### Fixes Attempted
Static source inspection and placeholder searches were completed successfully.

### Current Status
Needs testing

## 2026-09-24 — Review correction

### Prompt / Request
Review the implemented application and verify the requested behavior.

### Decision Summary
Category deletion is made explicit instead of relying solely on SQLite foreign-key pragma behavior. Notification delivery now treats permission denial as a non-fatal platform condition.

### Actions Performed
- Added a SQLDelight query that clears category references before deleting a category.
- Updated the repository deletion flow to leave affected tasks uncategorized.
- Guarded Android notification delivery against `SecurityException` after permission denial.
- Performed static searches for placeholders and inspected the generated project tree.

### Result
The implementation has no remaining placeholder markers in the application source and the specified category-deletion behavior is explicit.

### Problems / Errors
Compilation and runtime validation remain unavailable because Java, Kotlin, Gradle, and the Android/iOS toolchains are not installed in this shell environment.

### Fixes Attempted
No further toolchain workaround was possible without installing a full mobile development toolchain.

### Current Status
Needs testing
