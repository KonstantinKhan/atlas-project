# Этап 3: Ресурсное планирование (single-project) — План реализации

> **Статус: ✅ Завершён (2026-03-15).** Весь функционал реализован. Тесты (J7, K6, L4) не написаны.

## Context

Этапы 0–2 завершены (100%). Система умеет: CRUD задач, Gantt-планирование с drag/resize, зависимости (FS/SS/FF/SF) с каскадом, граф задач с топосортировкой, критический путь (CPM), аналитика (блокеры, what-if). Следующий шаг — добавить **реализм**: ресурсы не бесконечны.

Этап 3 из дорожной карты:
- 3.1 Ресурсы (человек/роль, календарь доступности, capacity)
- 3.2 Назначения (task ↔ resource, перегрузки, конфликты)
- 3.3 Выравнивание (автосдвиг задач при перегрузке)

---

## Архитектурное решение: дни vs часы

Текущая система работает в **рабочих днях** (Duration = Int). Ресурсы требуют часов.

**Решение:** оставить Duration задач в рабочих днях, добавить параллельное измерение effort-hours.

- Длительность задачи = рабочие дни (без изменений)
- Capacity ресурса = часы/день (8.0 = полный день, 4.0 = полдня)
- Назначение = часы/день, которые ресурс тратит на задачу
- Перегрузка = сумма назначений за день > capacity ресурса
- Leveling сдвигает задачи в рабочих днях (как текущий каскад)

Это **не ломает** существующую логику Duration, TimelineCalendar, каскад.

## Решения по UX (согласовано с пользователем)

- **UI ресурсов** → **отдельная страница** `/resources` (не панель внутри Gantt)
- **Resource Load Chart** → **отдельная страница** `/resource-load` (полноэкранный вид)
- **Назначения** → **несколько ресурсов на задачу** (task_assignments: UNIQUE(task_id, resource_id))
- **Leveling** → реализуем **сразу** в рамках Этапа 3 (Фазы J → K → L)

---

## Фаза J: Ресурсы (3.1)

**Цель:** CRUD ресурсов + календарь доступности + capacity.

### J1: Доменные модели

Новые файлы в `.../common/models/resource/`:

```kotlin
// Resource.kt
data class Resource(
    val id: ResourceId,
    val name: ResourceName,
    val type: ResourceType,         // PERSON | ROLE
    val capacityHoursPerDay: BigDecimal, // 8.0 по умолчанию
    val calendarId: Int?,           // null = проектный календарь
)

// ResourceCalendarOverride.kt
data class ResourceCalendarOverride(
    val resourceId: ResourceId,
    val date: LocalDate,
    val availableHours: BigDecimal, // 0 = выходной, иное = частичный день
)
```

Value classes: `ResourceId(val value: String)`, `ResourceName(val value: String)`, `enum ResourceType { PERSON, ROLE }`

Ресурс **наследует** проектный `TimelineCalendar`, но может иметь per-date overrides (отпуск, сокращённый день).

### J2: Миграция — V6__create_resources.sql

```sql
CREATE TABLE resources (
    id UUID PRIMARY KEY,
    project_plan_id UUID NOT NULL REFERENCES project_plans(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'PERSON',
    capacity_hours_per_day NUMERIC(4,1) NOT NULL DEFAULT 8.0,
    calendar_id INT REFERENCES timeline_calendar(id),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE resource_calendar_overrides (
    id SERIAL PRIMARY KEY,
    resource_id UUID NOT NULL REFERENCES resources(id) ON DELETE CASCADE,
    override_date DATE NOT NULL,
    available_hours NUMERIC(4,1) NOT NULL DEFAULT 0,
    UNIQUE (resource_id, override_date)
);
```

### J3: Репозиторий

Новый интерфейс `IResourceRepo` (отдельный от `IAtlasProjectTaskRepo` — separation of concerns):

```kotlin
interface IResourceRepo {
    suspend fun listResources(planId: String): List<Resource>
    suspend fun getResource(id: String): Resource?
    suspend fun createResource(planId: String, resource: Resource): Resource
    suspend fun updateResource(resource: Resource): Resource
    suspend fun deleteResource(id: String): Int
    suspend fun getCalendarOverrides(resourceId: String): List<ResourceCalendarOverride>
    suspend fun setCalendarOverride(override: ResourceCalendarOverride): Int
    suspend fun deleteCalendarOverride(resourceId: String, date: LocalDate): Int
}
```

Реализации: `ResourceRepoPostgres.kt` + `ResourceRepoInMemory.kt`

### J4: DTOs

```kotlin
@Serializable data class ResourceDto(id: String, name: String, type: String, capacityHoursPerDay: Double)
@Serializable data class CreateResourceCommandDto(name: String, type: String, capacityHoursPerDay: Double)
@Serializable data class UpdateResourceCommandDto(name: String?, type: String?, capacityHoursPerDay: Double?)
@Serializable data class ResourceCalendarOverrideDto(date: String, availableHours: Double)
```

### J5: API-эндпоинты

| Endpoint | Method | Описание |
|----------|--------|----------|
| `/resources` | GET | Список ресурсов проекта |
| `/resources` | POST | Создать ресурс |
| `/resources/{id}` | PATCH | Обновить ресурс |
| `/resources/{id}` | DELETE | Удалить ресурс |
| `/resources/{id}/calendar-overrides` | GET | Overrides календаря ресурса |
| `/resources/{id}/calendar-overrides` | POST | Установить override |
| `/resources/{id}/calendar-overrides/{date}` | DELETE | Удалить override |

### J6: Frontend

- Новый роут: `src/app/resources/page.tsx` — отдельная страница `/resources`
- Zod-схема: `resource.schema.ts` (ResourceSchema, ResourceListSchema)
- API: `resourcesApi.ts` (7 функций)
- Hooks: `useResources.ts` (useResources, useCreateResource, useUpdateResource, useDeleteResource)
- Компоненты:
  - `ResourcesPage.tsx` — полноэкранная таблица ресурсов (имя, тип, capacity, actions)
  - `ResourceCalendarOverridesEditor.tsx` — управление calendar overrides конкретного ресурса
  - `CreateResourceDialog.tsx` — модалка создания ресурса
- Навигация: добавить ссылку на `/resources` в layout или хедер Gantt-страницы

### J7: Тесты

- `ResourceTest.kt` — модельные тесты
- Repo-тесты (InMemory): CRUD + overrides
- Интеграционный тест API (Ktor)

---

## Фаза K: Назначения (3.2)

**Зависит от:** Фаза J (ресурсы должны существовать).

**Цель:** Task ↔ Resource маппинг, расчёт нагрузки, обнаружение перегрузок.

### K1: Доменные модели

```kotlin
// TaskAssignment.kt
data class TaskAssignment(
    val id: AssignmentId,
    val taskId: TaskId,
    val resourceId: ResourceId,
    val hoursPerDay: BigDecimal,  // часов/день на эту задачу
)

// ResourceLoadCalculator.kt — ЯДРО фазы K
class ResourceLoadCalculator(
    plan: ProjectPlan,
    assignments: List<TaskAssignment>,
    resources: List<Resource>,
    calendar: TimelineCalendar,
    calendarOverrides: Map<ResourceId, List<ResourceCalendarOverride>>,
) {
    fun computeLoad(from: LocalDate, to: LocalDate): OverloadReport
    fun computeResourceLoad(resourceId: ResourceId, from: LocalDate, to: LocalDate): ResourceLoadResult
}

// Результат
data class ResourceDayLoad(date: LocalDate, assignedHours: BigDecimal, capacityHours: BigDecimal, isOverloaded: Boolean)
data class ResourceLoadResult(resourceId: ResourceId, resourceName: String, days: List<ResourceDayLoad>, overloadedDays: List<ResourceDayLoad>)
data class OverloadReport(resources: List<ResourceLoadResult>, totalOverloadedDays: Int)
```

Алгоритм `computeLoad`:
1. Для каждого ресурса — итерация по рабочим дням в диапазоне
2. Для каждого дня — сумма `hoursPerDay` всех назначений, чья задача запланирована на этот день
3. Capacity ресурса на день = base capacity − calendar overrides
4. `assignedHours > capacityHours` → overloaded

### K2: Миграция — V7__create_assignments.sql

```sql
CREATE TABLE task_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_plan_id UUID NOT NULL REFERENCES project_plans(id) ON DELETE CASCADE,
    task_id UUID NOT NULL REFERENCES project_tasks(id) ON DELETE CASCADE,
    resource_id UUID NOT NULL REFERENCES resources(id) ON DELETE CASCADE,
    hours_per_day NUMERIC(4,1) NOT NULL DEFAULT 8.0,
    UNIQUE (task_id, resource_id)
);
```

### K3: Репозиторий

Расширение `IResourceRepo`:

```kotlin
suspend fun listAssignments(planId: String): List<TaskAssignment>
suspend fun getAssignmentsByTask(taskId: String): List<TaskAssignment>
suspend fun getAssignmentsByResource(resourceId: String): List<TaskAssignment>
suspend fun createAssignment(planId: String, assignment: TaskAssignment): TaskAssignment
suspend fun updateAssignment(id: String, hoursPerDay: BigDecimal): TaskAssignment
suspend fun deleteAssignment(id: String): Int
```

### K4: API-эндпоинты

| Endpoint | Method | Описание |
|----------|--------|----------|
| `/assignments` | GET | Все назначения проекта |
| `/assignments` | POST | Создать назначение (task + resource + hours) |
| `/assignments/{id}` | PATCH | Обновить hours/day |
| `/assignments/{id}` | DELETE | Удалить назначение |
| `/resource-load` | GET | Отчёт о нагрузке (query: from, to) |
| `/resource-load/{resourceId}` | GET | Нагрузка одного ресурса |

### K5: Frontend

- Zod-схемы: `assignment.schema.ts` (TaskAssignmentSchema, OverloadReportSchema, ResourceLoadSchema)
- API: `assignmentsApi.ts` (CRUD + load queries)
- Hooks: `useAssignments.ts`
- Компоненты:
  - `AssignmentEditor.tsx` — попап при клике на бар задачи: список назначенных ресурсов (N штук), добавление/удаление, редактирование hours/day
  - `GanttBar.tsx` — визуальные индикаторы назначенных ресурсов (инициалы/точки)
- Отдельная страница: `src/app/resource-load/page.tsx` → `/resource-load`
  - `ResourceLoadPage.tsx` — полноэкранная горизонтальная гистограмма нагрузки ресурсов
  - Строки = ресурсы, столбцы = дни (синхронно с timeline Gantt)
  - Красная заливка для перегруженных дней
  - Навигация: ссылка с `/resources` и из Gantt-тулбара

### K6: Тесты

- `ResourceLoadCalculatorTest.kt`:
  - 1 ресурс, 1 задача — нет перегрузки
  - 1 ресурс, 2 пересекающиеся задачи — перегрузка
  - Ресурс с calendar override (отпуск, сокращённый день)
  - Несколько ресурсов, смешанная нагрузка
- Assignment repo tests (InMemory)

---

## Фаза L: Выравнивание (3.3)

**Зависит от:** Фаза K (обнаружение перегрузок должно работать).

**Цель:** Автоматический сдвиг задач для устранения перегрузок с сохранением зависимостей.

### L1: Движок выравнивания

```kotlin
// ResourceLevelingEngine.kt
class ResourceLevelingEngine(
    plan: ProjectPlan,
    assignments: List<TaskAssignment>,
    resources: List<Resource>,
    calendar: TimelineCalendar,
    calendarOverrides: Map<ResourceId, List<ResourceCalendarOverride>>,
) {
    fun level(): LevelingResult
}

data class LevelingResult(
    val scheduleDelta: ScheduleDelta,
    val resolvedOverloads: Int,
    val remainingOverloads: Int,
)
```

**Алгоритм (serial resource leveling):**

1. Работа на `plan.snapshot()` (immutable копия)
2. Начальный `OverloadReport` через `ResourceLoadCalculator`
3. Приоритетная очередь задач: (a) топологический порядок, (b) earliest start, (c) slack по убыванию
4. Для каждого перегруженного дня (от раннего к позднему):
   - Найти задачи-источники перегрузки
   - Выбрать задачу с **наибольшим slack** (наименее критичную)
   - Сдвинуть её **вперёд** на ближайший день без перегрузки
   - Учесть dependency constraints (`clampStartByIncomingDeps`)
   - Каскад через `cascadeBfs` для successors
   - Пересчитать нагрузку затронутых ресурсов
5. Повторять до устранения перегрузок или лимита итераций
6. Сравнить итоговые расписания с исходными → `LevelingResult`

**Ограничения:**
- Задачи сдвигаются только **вперёд** (leveling не ускоряет)
- Зависимости не нарушаются
- Работа на snapshot → вызывающий код решает, сохранять ли

### L2: API-эндпоинты

| Endpoint | Method | Описание |
|----------|--------|----------|
| `/leveling/preview` | POST | Предпросмотр: что изменится (без сохранения) |
| `/leveling/apply` | POST | Применить выравнивание (сохранить изменения) |

Preview важен — следует паттерну what-if из Фазы I.

### L3: Frontend

- Кнопка «Выровнять ресурсы» на странице `/resource-load` (где видны перегрузки)
- `LevelingPreviewDialog.tsx` — модалка предпросмотра:
  - Таблица: задача, старый start → новый start, старый end → новый end
  - Счётчик: «Устранено N перегрузок из M»
  - Кнопки «Применить» / «Отмена»
  - После применения: invalidate project plan + resource-load queries
- Hooks: `useLevelingPreview()`, `useApplyLeveling()`

### L4: Тесты

- `ResourceLevelingEngineTest.kt`:
  - 2 задачи, 1 ресурс, пересечение → одна сдвигается
  - Leveling уважает FS-зависимость
  - Критическая задача (slack=0) не сдвигается, если есть альтернатива
  - Несколько ресурсов — сдвигаются только перегруженные
  - Нет перегрузки → нет изменений
  - Guard: максимум итераций

---

## Последовательность фаз

```
Фаза J (Ресурсы CRUD)         ← фундамент
    ↓
Фаза K (Назначения + нагрузка) ← зависит от J
    ↓
Фаза L (Выравнивание)          ← зависит от K
```

Каждая фаза — отдельный PR с самостоятельной ценностью:
- **J:** пользователь может создавать ресурсы и настраивать их календари
- **J+K:** можно назначать ресурсы на задачи и видеть перегрузки
- **J+K+L:** система автоматически устраняет перегрузки

---

## Архитектурные решения

### ProjectPlan — не расширять

`ProjectPlan` остаётся агрегатом для tasks/schedules/dependencies. `ResourceLevelingEngine` получает `ProjectPlan` + assignments + resources как отдельные параметры и вызывает `plan.changeTaskStartDate()` внутри. Это сохраняет чистую границу агрегата.

### Отдельные эндпоинты, не расширение /project-plan

Ресурсы и назначения — отдельные REST-эндпоинты. `GET /project-plan` не меняется. Frontend загружает ресурсы параллельно.

### InMemory-реализации для тестов

Для `IResourceRepo` создать InMemory-реализацию, следуя паттерну `AtlasProjectTaskRepoInMemory`.

---

## Ключевые файлы для модификации

| Файл | Что меняется |
|------|-------------|
| `Routing.kt` | Регистрация новых route-групп: resources(), assignments(), leveling() |
| `AppConfig.kt` | Добавить `IResourceRepo` в конфигурацию |
| `DomainToTransport.kt` | Маппинг-функции для Resource, Assignment, Load, Leveling DTOs |
| `GanttBar.tsx` | Индикаторы назначенных ресурсов (инициалы/точки) + попап назначения |
| `src/app/layout.tsx` | Навигация между страницами (Gantt / Resources / Load) |

## Новые файлы (суммарно)

### Backend (~20 файлов)
- Домен: Resource.kt, ResourceId.kt, ResourceName.kt, ResourceType.kt, ResourceCalendarOverride.kt, TaskAssignment.kt, AssignmentId.kt, ResourceLoadCalculator.kt, ResourceLevelingEngine.kt
- Repo: IResourceRepo.kt, ResourceRepoPostgres.kt, ResourceRepoInMemory.kt
- Tables: ResourcesTable.kt, TaskAssignmentsTable.kt, ResourceCalendarOverridesTable.kt
- DTOs: ResourceDto.kt, AssignmentDto.kt, LevelingResultDto.kt + command DTOs
- Routes: Resources.kt, Assignments.kt, Leveling.kt
- Миграции: V6__create_resources.sql, V7__create_assignments.sql
- Тесты: ResourceLoadCalculatorTest.kt, ResourceLevelingEngineTest.kt

### Frontend (~15 файлов)
- Страницы: `src/app/resources/page.tsx`, `src/app/resource-load/page.tsx`
- Схемы: resource.schema.ts, assignment.schema.ts
- API: resourcesApi.ts, assignmentsApi.ts
- Hooks: useResources.ts, useAssignments.ts
- Компоненты: ResourcesPage.tsx, CreateResourceDialog.tsx, ResourceCalendarOverridesEditor.tsx, AssignmentEditor.tsx, ResourceLoadPage.tsx, LevelingPreviewDialog.tsx

---

## Верификация

### Фаза J
- Unit: CRUD ресурсов, calendar overrides
- Integration: API тесты (Ktor + InMemory repo)
- Manual: создать ресурс через UI, установить capacity, добавить override

### Фаза K
- Unit: ResourceLoadCalculator — перегрузки, пересечения, overrides
- Integration: API тесты назначений + load endpoint
- Manual: назначить ресурс на 2 пересекающиеся задачи, проверить красные индикаторы перегрузки

### Фаза L
- Unit: ResourceLevelingEngine — все сценарии из L4
- Integration: preview → apply → проверить изменённые расписания
- Manual: создать перегрузку → Preview leveling → Apply → визуально проверить сдвиг на Gantt

---

## Дополнения к исходному плану (реализованы)

Следующие возможности были добавлены сверх исходного плана в процессе реализации:

### Плановая трудоёмкость (`plannedEffortHours`)
- `TaskAssignment.plannedEffortHours: Double?` — общая плановая трудоёмкость назначения в часах
- Если задана, система вычисляет `effortDeficit = max(0, planned - allocated)` — разницу между планом и фактически назначенными часами
- UI: поле «План: __ ч» в AssignmentEditor, бейджи дефицита на странице `/resource-load`

### Per-day overrides (`AssignmentDayOverride`)
- Корректировка загрузки назначения на конкретный день (override вместо дефолтного hoursPerDay)
- Миграция: `V8__assignment_enhancements.sql` — таблица `assignment_day_overrides`
- API: `GET/POST /assignments/{id}/day-overrides`, `DELETE /assignments/{id}/day-overrides/{date}`
- UI: `DayOverrideEditor` — попап при клике на ячейку дня на странице нагрузки
- `ResourceLoadCalculator` использует: `dayOverride[date]?.hours ?? assignment.hoursPerDay`

### UX: назначение из списка задач
- Кнопка `UserPlus` в `GanttTaskRow` — открывает `AssignmentEditor` прямо из панели задач (не только через бар на Gantt)
