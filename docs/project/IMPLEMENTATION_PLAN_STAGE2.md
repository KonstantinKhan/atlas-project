# План реализации — Этап 2: Графы и алгоритмы планирования

> _Создан: 2026-03-09_

Поэтапный план реализации Этапа 2 дорожной карты. Задачи сгруппированы по фазам, упорядочены по зависимостям.

**Размеры:** S = 1–2 часа | M = 3–5 часов | L = 1–2 дня

---

## Предпосылки

Этап 1 полностью завершён (фазы A–F). В коде уже есть:

| Возможность | Где | Примечание |
|-------------|-----|------------|
| Обнаружение циклов | `ProjectPlan.wouldCreateCycle()` | BFS при добавлении зависимости |
| Каскад вперёд | `ProjectPlan.cascadeBfs()` | Проталкивает successors |
| Пересчёт всех задач | `ProjectPlan.recalculateAll()` | Ad-hoc BFS от корней, **без формальной топосортировки** |
| Constraint scheduling | `ProjectPlan.calculateConstrainedStart()` | FS/SS/FF/SF с лагом |
| Рабочий календарь | `TimelineCalendar` | `addWorkingDays()`, `subtractWorkingDays()`, `workingDaysBetween()` |

**Чего нет:** топологическая сортировка, CPM (ES/EF/LS/LF/slack), аналитические запросы.

---

## Фаза G: Граф задач (2.1)

> Цель: формальная DAG-инфраструктура, рефакторинг на топологическую сортировку.

| ID | Задача | Слой | Размер | Статус |
|----|--------|------|--------|--------|
| G1 | `topologicalSort()` — алгоритм Кана | Backend | S | Done |
| G2 | `validateNoCycles()` — валидация всего графа | Backend | S | Done |
| G3 | Рефакторинг `recalculateAll()` на topologicalSort | Backend | S | Done |
| G4 | `snapshot()` — глубокая копия ProjectPlan для what-if | Backend | S | Done |
| G5 | Unit-тесты графовых операций | Backend | M | Done |

### Детали

**G1** — Новый файл: `common/models/projectPlan/TopologicalSort.kt`

```kotlin
fun topologicalSort(
    taskIds: Set<TaskId>,
    dependencies: Collection<TaskDependency>
): List<TaskId>
```

Алгоритм Кана (BFS-based). Возвращает упорядоченный список от корней к листьям. Выбрасывает `IllegalStateException` при обнаружении цикла.

Почему Кана, а не DFS: натурально даёт порядок «по уровням», удобный для forward/backward pass CPM, и обнаруживает циклы как побочный эффект.

**G2** — В `ProjectPlan.kt`: метод `validateNoCycles(): Boolean` — обёртка над `topologicalSort()` с try/catch. Дополняет существующий `wouldCreateCycle()`, который проверяет только одно ребро.

**G3** — Заменить ad-hoc BFS в `recalculateAll()` на:

```kotlin
val scheduledIds = schedules.keys
    .filter { schedules[it]?.start is ProjectDate.Set }
    .toSet()
val sortedIds = topologicalSort(scheduledIds, dependencies)
for (taskId in sortedIds) { ... }
```

**G4** — В `ProjectPlan.kt`: `fun snapshot(): ProjectPlan` — глубокая копия (mutable maps и set копируются явно, т.к. `copy()` делает shallow copy).

**G5** — Тестовые сценарии: линейная цепочка (A→B→C), ромб (A→B, A→C, B→D, C→D), множественные корни, пустой граф, один узел, цикл (ожидаемое исключение).

### Файлы

- **Новый:** `.../common/models/projectPlan/TopologicalSort.kt`
- **Изменить:** `.../common/models/projectPlan/ProjectPlan.kt` (recalculateAll, snapshot, validateNoCycles)
- **Новый:** `.../test/.../TopologicalSortTest.kt`

---

## Фаза H: Критический путь — CPM (2.2)

> Цель: ES/EF/LS/LF/slack, выделение критических задач.

| ID | Задача | Слой | Размер | Статус |
|----|--------|------|--------|--------|
| H1 | `CriticalPathAnalysis` — forward pass (ES/EF) | Backend | M | Done |
| H2 | `CriticalPathAnalysis` — backward pass (LS/LF) + slack | Backend | M | Done |
| H3 | DTO: `CpmTaskDto`, `CriticalPathDto` | Backend | S | Done |
| H4 | Маппер: `CriticalPathResult.toDto()` | Backend | S | Done |
| H5 | Эндпоинт: `GET /critical-path` | Backend | S | Done |
| H6 | Unit-тесты CPM (forward, backward, slack, critical chain) | Backend | L | Done |
| H7 | Frontend: `useCriticalPath()` hook + API | Frontend | S | Done |
| H8 | Frontend: подсветка критических баров (ring-red) | Frontend | M | Done |
| H9 | Frontend: красные стрелки на критическом пути | Frontend | S | Done |
| H10 | Frontend: tooltip со slack на hover бара | Frontend | S | Done |

### Детали

**H1–H2** — Новый файл: `common/models/projectPlan/CriticalPathAnalysis.kt`

```kotlin
data class CpmTaskResult(
    val taskId: TaskId,
    val es: LocalDate,       // Earliest Start
    val ef: LocalDate,       // Earliest Finish
    val ls: LocalDate,       // Latest Start
    val lf: LocalDate,       // Latest Finish
    val slack: Int,           // workingDaysBetween(es, ls)
    val isCritical: Boolean,  // slack == 0
)

data class CriticalPathResult(
    val tasks: Map<TaskId, CpmTaskResult>,
    val criticalTaskIds: Set<TaskId>,
    val projectEnd: LocalDate,
)

class CriticalPathAnalysis(
    private val plan: ProjectPlan,
    private val calendar: TimelineCalendar,
) {
    fun compute(): CriticalPathResult
}
```

**Алгоритм:**

1. Получить топологический порядок запланированных задач через `topologicalSort()`.
2. **Forward pass** (в топологическом порядке):
   - Корневые задачи: ES = их текущий start.
   - Остальные: ES = max(constraint dates от всех predecessors) — аналогично `calculateConstrainedStart()`.
   - EF = `calendar.addWorkingDays(ES, duration)`.
3. **Project end** = max(EF) по всем задачам.
4. **Backward pass** (в обратном топологическом порядке):
   - Листовые задачи: LF = project end. LS = `calendar.subtractWorkingDays(LF, duration)`.
   - Остальные: LF = min(constraint dates от всех successors). LS = `calendar.subtractWorkingDays(LF, duration)`.
5. **Slack** = `calendar.workingDaysBetween(ES, LS)`. Критические = slack == 0.

**Обратные ограничения (backward pass)** — для каждой исходящей зависимости задачи T → successor S:

| Тип | Ограничение на LF задачи T |
|-----|---------------------------|
| FS | LF ≤ `subtractWorkingDays(LS_succ, lag)` |
| SS | LS ≤ LS_succ - lag → LF не ограничивается напрямую |
| FF | LF ≤ LF_succ - lag |
| SF | LS ≤ LF_succ - lag → LF не ограничивается напрямую |

**Незапланированные задачи** (без дат) исключаются из расчёта. CPM вычисляется **on-demand** (не хранится в БД).

**H3** — Новый файл: `.../transport/cpm/CriticalPathDto.kt`

```kotlin
@Serializable
data class CpmTaskDto(
    val taskId: String,
    val es: String,  // ISO date
    val ef: String,
    val ls: String,
    val lf: String,
    val slack: Int,
    val isCritical: Boolean,
)

@Serializable
data class CriticalPathDto(
    val tasks: List<CpmTaskDto>,
    val criticalTaskIds: List<String>,
    val projectEnd: String,
)
```

**H5** — В `Routing.kt`:

```kotlin
get("/critical-path") {
    val plan = config.repo.projectPlan()
    val calendar = config.calendarService.current()
    val result = CriticalPathAnalysis(plan, calendar).compute()
    call.respond(result.toDto())
}
```

**H7** — `projectTasksApi.ts`: `getCriticalPath()`. `useProjectTasks.ts`: `useCriticalPath()` с invalidation при изменении плана.

**H8** — `GanttBar.tsx`: prop `isCritical?: boolean` → `ring-2 ring-red-500`. `GanttChart.tsx`: передаёт `criticalTaskIds` из `useCriticalPath()`.

**H9** — В `GanttTaskLayer.tsx`: если оба конца стрелки критические → цвет `#ef4444` вместо `#6366f1`.

**H10** — На `GanttBar`: hover tooltip «Запас: N дн.» или «Критическая задача».

### Файлы

- **Новый:** `.../common/models/projectPlan/CriticalPathAnalysis.kt`
- **Новый:** `.../transport/cpm/CriticalPathDto.kt`
- **Изменить:** `.../mappers/` (добавить CpmMapper)
- **Изменить:** `Routing.kt` (1 endpoint)
- **Новый:** `.../test/.../CriticalPathAnalysisTest.kt`
- **Изменить:** `projectTasksApi.ts`, `useProjectTasks.ts`
- **Изменить:** `GanttBar.tsx`, `GanttChart.tsx`, `GanttTaskLayer.tsx`

---

## Фаза I: Аналитические виды (2.3)

> Цель: блокеры, доступные задачи, what-if анализ.

| ID | Задача | Слой | Размер | Статус |
|----|--------|------|--------|--------|
| I1 | «Что блокирует задачу X» — BFS по predecessors | Backend | S | |
| I2 | «Какие задачи можно начать сейчас» — фильтр | Backend | S | |
| I3 | «Что будет если сдвинуть задачу» — what-if через snapshot | Backend | M | |
| I4 | DTOs для аналитики | Backend | S | |
| I5 | Эндпоинты: 3 GET-маршрута | Backend | S | |
| I6 | Unit-тесты аналитики | Backend | M | |
| I7 | Frontend: hooks + API для 3 запросов | Frontend | S | |
| I8 | Frontend: панель аналитики (AnalysisPanel) | Frontend | L | |

### Детали

**I1** — В `AnalysisQueries.kt`:

```kotlin
fun ProjectPlan.getBlockerChain(taskId: TaskId): List<TaskId>
```

BFS назад по predecessor'ам. Возвращает упорядоченный список от корней до непосредственных предшественников. Включает только незавершённые задачи.

**I2** —

```kotlin
fun ProjectPlan.getAvailableTasks(
    calendar: TimelineCalendar,
    today: LocalDate
): List<TaskId>
```

Задача доступна если: запланирована, не DONE, все predecessors завершены (или нет блокирующих зависимостей), ES ≤ today.

**I3** — Использует `snapshot()` из G4:

```kotlin
data class ScheduleImpact(
    val taskId: TaskId,
    val currentStart: LocalDate,
    val currentEnd: LocalDate,
    val projectedStart: LocalDate,
    val projectedEnd: LocalDate,
    val deltaWorkingDays: Int,
)

fun ProjectPlan.simulateMove(
    taskId: TaskId,
    newStart: LocalDate,
    calendar: TimelineCalendar
): List<ScheduleImpact>
```

Алгоритм:
1. `val sim = this.snapshot()`
2. `sim.changeTaskStartDate(taskId, newStart, calendar)`
3. Сравнить расписания sim vs original → вернуть дельты.

Оригинальный план и БД не затрагиваются.

**I5** — Эндпоинты:

| Endpoint | Method | Параметры | Возвращает |
|----------|--------|-----------|------------|
| `/analysis/blockers/{taskId}` | GET | path: taskId | `BlockerChainDto` — список блокирующих задач |
| `/analysis/available-tasks` | GET | — | `AvailableTasksDto` — задачи, готовые к старту |
| `/analysis/impact/{taskId}` | GET | path: taskId, query: newStart | `ImpactSimulationDto` — затронутые задачи с дельтами |

**I8** — Новый компонент `AnalysisPanel.tsx` — сворачиваемая боковая панель с 3 секциями:
- **Блокеры**: выбрать задачу → список её блокирующей цепочки
- **Доступные**: задачи, которые можно начать сегодня
- **Что-если**: выбрать задачу + новую дату → увидеть затронутые задачи

Интеграция в `GanttChart.tsx` через кнопку-тоггл.

### Файлы

- **Новый:** `.../common/models/projectPlan/AnalysisQueries.kt`
- **Новый:** `.../transport/analysis/BlockerChainDto.kt`
- **Новый:** `.../transport/analysis/AvailableTasksDto.kt`
- **Новый:** `.../transport/analysis/ImpactSimulationDto.kt`
- **Изменить:** `Routing.kt` (3 endpoints)
- **Новый:** `.../test/.../AnalysisQueriesTest.kt`
- **Новый:** `frontend/.../components/GanttChart/AnalysisPanel.tsx`
- **Изменить:** `projectTasksApi.ts`, `useProjectTasks.ts`, `GanttChart.tsx`

---

## Порядок реализации

```
Фаза G (граф задач) ─── фундамент
    ↓
Фаза H (CPM) ────────── зависит от G (topologicalSort)
    ↓
Фаза I (аналитика) ──── зависит от G (snapshot) + H (ES для available tasks)
```

Каждая фаза — отдельный PR.

---

## Ключевые архитектурные решения

| Решение | Обоснование |
|---------|-------------|
| CPM вычисляется on-demand, не хранится в БД | Производный результат, всегда актуален. O(V+E) — быстро. |
| Отдельный класс `CriticalPathAnalysis` | `ProjectPlan.kt` уже ~500 строк с мутабельным стейтом. CPM — read-only анализ, чистое разделение. |
| `TopologicalSort.kt` — отдельный файл | Переиспользуется и в `recalculateAll()`, и в CPM. |
| What-if через `snapshot()` | Глубокая копия плана → мутация копии → сравнение. Оригинал не трогается. |
| Незапланированные задачи исключаются из CPM | Нет дат → нет ES/EF. Связи к/от них игнорируются. |

---

## Верификация

1. **Unit-тесты:** `./gradlew test` — все тесты (существующие + новые) проходят
2. **Топологическая сортировка:** тест на ромбовидный граф даёт корректный порядок; тест на цикл → исключение
3. **CPM ручная проверка:** проект с 5-6 задачами, 3-4 зависимости → `GET /critical-path` возвращает корректные ES/EF/LS/LF/slack
4. **Frontend CPM:** критические бары подсвечены красным, стрелки между ними красные, tooltip показывает slack
5. **What-if:** `GET /analysis/impact/{id}?newStart=...` возвращает дельты; повторный `GET /project-plan` подтверждает, что данные не изменились
6. **Интеграционный тест:** сдвинуть задачу → повторный `GET /critical-path` возвращает обновлённые значения

---

## Связанные документы

- [Дорожная карта](../ROAD_MAP.md)
- [План реализации — Этап 1](IMPLEMENTATION_PLAN.md)
- [Статус реализации](IMPLEMENTATION_STATUS.md)
