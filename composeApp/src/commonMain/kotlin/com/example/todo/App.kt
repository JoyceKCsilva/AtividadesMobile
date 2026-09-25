package com.example.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private enum class Screen { LIST, EDITOR, CATEGORIES }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TodoApp() {
    val viewModel = remember { TodoViewModel(TodoRepository(
        com.example.todo.db.TodoDatabase(createDatabaseDriver()), createNotificationScheduler()
    )) }
    val state by viewModel.state.collectAsState()
    var screen by remember { mutableStateOf(Screen.LIST) }
    var editingId by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(
                    when (screen) {
                        Screen.LIST -> "Minhas tarefas"
                        Screen.EDITOR -> if (editingId == null) "Nova tarefa" else "Editar tarefa"
                        Screen.CATEGORIES -> "Categorias"
                    }
                ) })
            },
            snackbarHost = { SnackbarHost(snackbar) }
        ) { padding ->
            when (screen) {
                Screen.LIST -> TaskListScreen(
                    state, Modifier.padding(padding),
                    onNew = { editingId = null; screen = Screen.EDITOR },
                    onEdit = { editingId = it.id; screen = Screen.EDITOR },
                    onToggle = viewModel::toggleTask,
                    onCategories = { screen = Screen.CATEGORIES },
                    onStatus = viewModel::setStatus,
                    onCategory = viewModel::setCategory
                )
                Screen.EDITOR -> TaskEditorScreen(
                    task = editingId?.let(viewModel::findTask) ?: viewModel.newTask(),
                    categories = state.categories,
                    modifier = Modifier.padding(padding),
                    onCancel = { screen = Screen.LIST },
                    onSave = { if (viewModel.saveTask(it)) screen = Screen.LIST },
                    onDelete = { viewModel.deleteTask(it); screen = Screen.LIST }
                )
                Screen.CATEGORIES -> CategoryScreen(
                    state.categories, Modifier.padding(padding),
                    onBack = { screen = Screen.LIST },
                    onAdd = viewModel::addCategory,
                    onRename = viewModel::renameCategory,
                    onDelete = viewModel::deleteCategory
                )
            }
        }
    }
}

@Composable
private fun TaskListScreen(
    state: TodoState,
    modifier: Modifier,
    onNew: () -> Unit,
    onEdit: (Task) -> Unit,
    onToggle: (Task) -> Unit,
    onCategories: () -> Unit,
    onStatus: (StatusFilter) -> Unit,
    onCategory: (String?) -> Unit
) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusFilter.values().forEach { filter ->
                if (state.status == filter) Button(onClick = { onStatus(filter) }) { Text(filter.label()) }
                else OutlinedButton(onClick = { onStatus(filter) }) { Text(filter.label()) }
            }
        }
        Text("Use os filtros de status e categoria para encontrar tarefas.", style = MaterialTheme.typography.bodySmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                CategoryChip("Todas", state.categoryId == null, null, onCategory)
            }
            items(state.categories, key = { it.id }) { category ->
                CategoryChip(category.name, state.categoryId == category.id, category.id, onCategory)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onNew) { Text("Nova tarefa") }
            OutlinedButton(onClick = onCategories) { Text("Categorias") }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.visibleTasks, key = { it.id }) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (task.completed) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Checkbox(task.completed, { onToggle(task) })
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                task.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (task.completed) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (task.description.isNotBlank()) {
                                Text(
                                    task.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusChip(task.completed)
                                task.categoryName?.let {
                                    CategoryChip(it, false, task.categoryId, {})
                                }
                            }
                            task.dueDateTime?.let {
                                Text(
                                    "Prazo: ${it.formatForDisplay()}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (!task.completed) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                        TextButton(onClick = { onEdit(task) }) { Text("Editar") }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    name: String,
    selected: Boolean,
    categoryId: String?,
    onClick: (String?) -> Unit
) {
    val color = categoryColor(categoryId ?: name)
    AssistChip(
        onClick = { onClick(categoryId) },
        label = { Text(name, fontSize = 12.sp) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) color.copy(alpha = 0.25f) else color.copy(alpha = 0.10f),
            labelColor = color
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.55f))
    )
}

@Composable
private fun StatusChip(completed: Boolean) {
    val color = if (completed) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(if (completed) "Concluída" else "Pendente", fontSize = 12.sp) },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = color.copy(alpha = 0.12f),
            disabledLabelColor = color
        )
    )
}

private fun categoryColor(value: String): Color {
    val palette = listOf(
        Color(0xFF6750A4),
        Color(0xFF006A6A),
        Color(0xFF9C4146),
        Color(0xFF7A5900),
        Color(0xFF386A20),
        Color(0xFF315E91)
    )
    return palette[(value.hashCode() and Int.MAX_VALUE) % palette.size]
}

private fun Instant.formatForDisplay(): String {
    val local = toLocalDateTime(TimeZone.currentSystemDefault())
    val date = "${local.date.dayOfMonth.toString().padStart(2, '0')}/" +
        "${local.date.monthNumber.toString().padStart(2, '0')}/${local.date.year}"
    val time = "${local.time.hour.toString().padStart(2, '0')}:" +
        local.time.minute.toString().padStart(2, '0')
    return "$date às $time"
}

private fun StatusFilter.label() = when (this) {
    StatusFilter.ALL -> "Todas"
    StatusFilter.PENDING -> "Pendentes"
    StatusFilter.COMPLETED -> "Concluídas"
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TaskEditorScreen(
    task: Task,
    categories: List<Category>,
    modifier: Modifier,
    onCancel: () -> Unit,
    onSave: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    var title by remember(task.id) { mutableStateOf(task.title) }
    var description by remember(task.id) { mutableStateOf(task.description) }
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val initialDateTime = task.dueDateTime?.toLocalDateTime(timeZone)
    var selectedDate by remember(task.id) { mutableStateOf(initialDateTime?.date) }
    var selectedTime by remember(task.id) { mutableStateOf(initialDateTime?.time) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var completed by remember(task.id) { mutableStateOf(task.completed) }
    var categoryId by remember(task.id) { mutableStateOf(task.categoryId) }
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Título *") })
        OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Descrição") })
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Data do prazo", style = MaterialTheme.typography.labelLarge)
            OutlinedButton(onClick = { showDatePicker = true }) {
                Text(selectedDate?.toString() ?: "Selecionar data")
            }
            Text("Hora do prazo", style = MaterialTheme.typography.labelLarge)
            OutlinedButton(onClick = { showTimePicker = true }) {
                Text(selectedTime?.let { "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}" }
                    ?: "Selecionar hora")
            }
        }
        if (selectedDate != null || selectedTime != null) {
            TextButton(onClick = { selectedDate = null; selectedTime = null }) {
                Text("Remover prazo")
            }
        }
        Row {
            Checkbox(completed, { completed = it })
            Text("Concluída", Modifier.padding(top = 12.dp))
        }
        BoxWithCategory(categories, categoryId, { categoryId = it }, expanded, { expanded = it })
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val parsed = when {
                    selectedDate == null && selectedTime == null -> null
                    selectedDate == null || selectedTime == null -> null
                    else -> LocalDateTime(selectedDate!!, selectedTime!!).toInstant(timeZone)
                }
                if ((selectedDate == null) != (selectedTime == null)) {
                    error = "Selecione a data e a hora, ou remova o prazo."
                } else {
                    onSave(task.copy(title = title, description = description, completed = completed,
                        dueDateTime = parsed, categoryId = categoryId))
                }
            }) { Text("Salvar") }
            OutlinedButton(onClick = onCancel) { Text("Cancelar") }
            if (task.title.isNotBlank()) TextButton(onClick = { onDelete(task) }) { Text("Excluir") }
        }
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.toUtcPickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = datePickerState.selectedDateMillis?.let { it.toPickerDate() }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    if (showTimePicker) {
        val currentTime = selectedTime ?: LocalTime(9, 0)
        val timePickerState = rememberTimePickerState(
            initialHour = currentTime.hour,
            initialMinute = currentTime.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Selecionar hora") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = LocalTime(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            }
        )
    }
}

private fun LocalDate.toUtcPickerMillis(): Long =
    LocalDateTime(this, LocalTime(0, 0)).toInstant(TimeZone.UTC).toEpochMilliseconds()

private fun Long.toPickerDate(): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date

@Composable
private fun BoxWithCategory(
    categories: List<Category>, selected: String?, onSelect: (String?) -> Unit,
    expanded: Boolean, onExpanded: (Boolean) -> Unit
) {
    val selectedName = categories.firstOrNull { it.id == selected }?.name ?: "Sem categoria"
    Column {
        OutlinedButton(onClick = { onExpanded(true) }) { Text("Categoria: $selectedName") }
        DropdownMenu(expanded, { onExpanded(false) }) {
            DropdownMenuItem({ Text("Sem categoria") }, { onSelect(null); onExpanded(false) })
            categories.forEach { category ->
                DropdownMenuItem({ Text(category.name) }, { onSelect(category.id); onExpanded(false) })
            }
        }
    }
}

@Composable
private fun CategoryScreen(
    categories: List<Category>, modifier: Modifier, onBack: () -> Unit,
    onAdd: (String) -> Boolean, onRename: (Category, String) -> Unit, onDelete: (Category) -> Unit
) {
    var newName by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Category?>(null) }
    var editName by remember { mutableStateOf("") }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row {
            OutlinedTextField(newName, { newName = it }, Modifier.weight(1f), label = { Text("Nova categoria") })
            Button(onClick = { if (onAdd(newName)) newName = "" }) { Text("Adicionar") }
        }
        categories.forEach { category ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(category.name, Modifier.padding(12.dp))
                Row {
                    TextButton(onClick = { editing = category; editName = category.name }) { Text("Renomear") }
                    TextButton(onClick = { onDelete(category) }) { Text("Excluir") }
                }
            }
        }
        TextButton(onClick = onBack) { Text("Voltar") }
    }
    editing?.let { category ->
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Renomear categoria") },
            text = { OutlinedTextField(editName, { editName = it }, label = { Text("Nome") }) },
            confirmButton = { TextButton(onClick = { onRename(category, editName); editing = null }) { Text("Salvar") } },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancelar") } }
        )
    }
}
