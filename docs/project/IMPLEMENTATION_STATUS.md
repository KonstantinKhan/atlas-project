# Статус реализации

> _Последнее обновление: 2026-03-15 (Этап 4 в процессе — мультипроектность)_

Документ сопоставляет дорожную карту ([ROAD_MAP.md](../ROAD_MAP.md)) с фактическим состоянием кода.

**Условные обозначения:** ✅ Реализовано | 🔧 Частично | ❌ Не реализовано

---

## Этап 0. Архитектурный фундамент (~80%)

### Доменные модели

| Модель | Статус | Расположение |
|--------|--------|--------------|
| `ProjectTask` | ✅ | `backend/.../common/models/task/ProjectTask.kt` |
| `TaskSchedule` | ✅ | `backend/.../common/models/taskSchedule/TaskSchedule.kt` |
| `ProjectPlan` | ✅ | `backend/.../common/models/projectPlan/ProjectPlan.kt` |
| `TaskDependency` | ✅ | `backend/.../common/models/TaskDependency.kt` |
| `TimelineCalendar` | ✅ | `backend/.../common/models/timelineCalendar/TimelineCalendar.kt` |

> Примечание: в дорожной карте модель называется `WorkCalendar`, в коде — `TimelineCalendar`.

### UX-команды

| Команда | Статус | Комментарий |
|---------|--------|-------------|
| Create (CreateTaskInPool) | ✅ | POST `/project-tasks/create-in-pool` |
| Rename (UpdateTitle) | ✅ | PATCH `/project-tasks/{id}` |
| ChangeStartDate | ✅ | POST `/change-start` с BFS-каскадом |
| ChangeEndDate | ✅ | POST `/change-end` с BFS-каскадом |
| Move (drag бара) | ✅ | `MoveTask` → `changeStartMutation` с оптимистичным обновлением |
| Resize (drag правого края) | ✅ | `ResizeTask` → `changeEndMutation` с оптимистичным обновлением |
| Resize слева (drag левого края) | ✅ | `ResizeFromStart` → start меняется, end сохраняется |

### Pipeline

```
UI → UX Command → API → Domain → DB → DTO → UI
```

✅ Pipeline полностью реализован для Create, Rename, ChangeStartDate, ChangeEndDate.

---

## Этап 1.1. Task Pool (~100%)

| Функция | Статус | Комментарий |
|---------|--------|-------------|
| Создание задач | ✅ | `CreateTaskInPoolCommand` → API → DB |
| Переименование | ✅ | `UpdateTitleCommand` → PATCH API |
| Задачи без дат | ✅ | Задачи в пуле существуют без расписания |
| Удаление | ✅ | `DeleteTaskCommand` → `DELETE /project-tasks/{id}` → каскад в БД, modal подтверждения |

---

## Этап 1.2. Gantt-планирование (~100%)

| Функция | Статус | Комментарий |
|---------|--------|-------------|
| Назначение на таймлайн | ✅ | Drag из пула (C4) + date-пикеры |
| Drag (перемещение бара) | ✅ | `GanttTaskLayer` mouse events → `MoveTask` → `ChangeStartDate` |
| Resize (изменение длительности) | ✅ | Resize-handle на баре → `ResizeTask` → `ChangeEndDate` |
| Планирование вперёд | ✅ | `changeTaskStartDate` пересчитывает end через календарь |
| Планирование назад | ✅ | `planFromEnd()` + `POST /plan-from-end` (C5) |
| Реакция на нерабочие дни | ✅ | `TimelineCalendar.addWorkingDays` пропускает выходные |
| Пересчёт через календарь | ✅ | Все изменения дат проходят через `TimelineCalendar` |
| UI не считает даты | ✅ | Все расчёты на бэкенде |

---

## Этап 1.3. Зависимости (~100%)

| Функция | Статус | Комментарий |
|---------|--------|-------------|
| FS (Finish–Start) | ✅ | Полный цикл: UI → API → Domain → DB → UI |
| SS / FF / SF | ✅ | Все типы: создание через gesture (fromSide × toSide), смена через попап, визуализация SVG |
| Визуальные связи (SVG) | ✅ | SVG-стрелки с разными точками привязки по типу (FS/SS/FF/SF) |
| Пересчёт цепочек (BFS-каскад) | ✅ | `ProjectPlan.changeTaskStartDate/changeTaskEndDate` с каскадом |
| Детекция циклов | ✅ | Реализовано в `ProjectPlan.addDependency` |
| Создание зависимости | ✅ | POST `/dependencies` с автоопределением типа из gesture |
| Удаление зависимости | ✅ | `DELETE /dependencies` + `deleteDependencyMutation` с оптимистичным обновлением |
| Смена типа зависимости | ✅ | `PATCH /dependencies` + `DependencyActionPopover` с кнопками FS/SS/FF/SF |
| Пересчёт при удалении | ✅ | `recalculateAll()` при удалении — successor может сдвинуться назад |
| UX стрелок | ✅ | Hit-area (14px), hover-glow, попап по клику, Escape/click-outside dismiss |

---

## Фаза B: Task Pool ✅ (завершена 2026-03-04)

| Задача | Статус | Комментарий |
|--------|--------|-------------|
| B1: DELETE-эндпоинт | ✅ | `IAtlasProjectTaskRepo.deleteTask()` + Postgres (каскад) + InMemory + `DELETE /project-tasks/{id}` |
| B2: UI кнопка удаления | ✅ | `DeleteTaskCommand`, `Trash2`-кнопка в `GanttTaskRow`, `useDeleteProjectTask` hook |
| B3: Диалог подтверждения | ✅ | `ConfirmDeleteModal` — название задачи + счётчик зависимостей + подтверждение |

### Изменения в коде (Фаза B)
- `IAtlasProjectTaskRepo.kt` — добавлен `deleteTask(id: String): Int`
- `AtlasProjectTaskRepoPostgres.kt` — каскадное удаление: schedules → dependencies → task
- `AtlasProjectTaskRepoInMemory.kt` — `deleteWhere` по id
- `Routing.kt` — `DELETE /project-tasks/{id}` с 400/404 проверками → 204 NoContent
- `TaskCommandType.ts` / `TaskCommand.type.ts` — добавлен `DeleteTask`
- Новые файлы: `DeleteTaskCommand.type.ts`, `ConfirmDeleteModal.tsx`
- `projectTasksApi.ts` — `deleteProjectTask(id)`
- `useProjectTasks.ts` — `useDeleteProjectTask` с оптимистичным обновлением кеша
- `GanttTaskRow.tsx` — `group`-hover + кнопка удаления
- `GanttChart.tsx` — `pendingDeleteTaskId` стейт, `handleConfirmDelete`, рендер модала

---

## Фаза C: Gantt-планирование ✅ (завершена 2026-03-05)

| Задача | Статус | Комментарий |
|--------|--------|-------------|
| C1: Drag бара (перемещение) | ✅ | Mouse events в `GanttTaskLayer` → `MoveTask` → `changeStartMutation` с оптимистичным обновлением |
| C2: Resize бара (длительность) | ✅ | Resize-handle в `GanttBar` → `ResizeTask` → `changeEndMutation` с оптимистичным обновлением |
| C3: POST /project-tasks/{id}/schedule | ✅ | Назначает расписание задаче из пула; принимает `{ start, duration }`, вычисляет end через календарь |
| C4: Drag задачи из пула на таймлайн | ✅ | `@dnd-kit/react` DragDropProvider; `GanttCalendarGrid` как drop-zone; `AssignSchedule` команда |
| C5: Планирование назад (end → start) | ✅ | `ProjectPlan.planFromEnd()` + `POST /plan-from-end`; end → start через `subtractWorkingDays` |
| C6: Оптимистичные обновления + rollback | ✅ | `prevTasksRef.current` паттерн; `onError` откат для MoveTask/ResizeTask/AssignSchedule |

### Изменения в коде (Фаза C)
- `ProjectPlan.kt` — `planFromEnd()`, `recalculateAll()`, `calculateConstrainedStart()` с поддержкой negative lag, `actualDuration()`
- `Routing.kt` — `/project-tasks/{id}/schedule`, `/plan-from-end`, `/dependencies/recalculate` (BFS заменён на `recalculateAll()`)
- `ScheduleDelta.kt` — добавлено поле `updatedDependencies: List<TaskDependency>`
- `GanttChart.tsx` — MoveTask/ResizeTask/AssignSchedule/PlanFromEnd команды + DragDropProvider + handleDragEnd
- `GanttTaskLayer.tsx` — drag/resize mouse state machine + real-time SVG arrow offsets
- `GanttBar.tsx` — resize-handle + drag prop
- `GanttCalendarGrid.tsx` — useDroppable для drop-zone таймлайна
- `GanttTaskRow.tsx` — useDraggable для задач из пула + PlanFromEnd триггер
- `useProjectTasks.ts` — хуки `useChangeStartDate`, `useChangeEndDate`, `useAssignSchedule`, `usePlanFromEnd`
- `projectTasksApi.ts` — `assignTaskSchedule()`, `planTaskFromEnd()`
- Новые файлы: `AssignScheduleCommand.type.ts`, `MoveTaskCommand.type.ts`, `ResizeTaskCommand.type.ts`, `PlanFromEndCommand.type.ts`
- Новые DTOs: `AssignScheduleCommandDto.kt`, `PlanFromEndCommandDto.kt`

### Технический долг, устранённый в рамках фазы C

| Проблема | Фикс |
|----------|------|
| Лаг зависимости не сохранялся в БД при создании | `/dependencies` читает лаг из `plan.dependencies()` после `addDependency()` |
| `durationDays` не синхронизировался при изменении даты окончания | `/change-end` вызывает `config.repo.updateTask()` после `updateSchedule()` |
| `/dependencies/recalculate` использовал расходящуюся формулу BFS | Заменён на `plan.recalculateAll(calendar)` — единая формула с `calculateConstrainedStart()` |

---

## Фаза D: Зависимости ✅ (завершена 2026-03-06)

| Задача | Статус | Комментарий |
|--------|--------|-------------|
| D1: DELETE-эндпоинт для зависимости | ✅ | `DELETE /dependencies?from={id}&to={id}` → пересчитанный `GanttProjectPlan` |
| D2: Подключить удаление к API | ✅ | `deleteDependencyMutation` с оптимистичным обновлением + откат |
| D3: UI выбора типа зависимости | ✅ | Двусторонние маркеры (start/end) на GanttBar + попап `[Start\|Finish]` на целевой задаче |
| D4: Визуализация SS/FF/SF стрелок | ✅ | SVG-стрелки с разными точками привязки по типу зависимости |
| D5: Пересчёт при удалении | ✅ | `recalculateAll()` — successor сдвигается назад при снятии ограничения |
| D6: Смена типа зависимости | ✅ | `PATCH /dependencies` + `changeDependencyType()` в `ProjectPlan.kt` + `DependencyActionPopover` |
| D7: Resize слева | ✅ | `POST /resize-from-start` + `resizeTaskFromStart()` — start меняется, end сохраняется |
| D8: UX стрелок | ✅ | Hit-area (14px), hover-glow (1.5→3, #818cf8), попап по клику |
| D9: Сохранение порядка задач | ✅ | Order-preserving merge вместо `setAllTasks(newPlan.tasks)` |

### Изменения в коде (Фаза D)
- `ProjectPlan.kt` — `changeDependencyType()`, `resizeTaskFromStart()` с clamping по FS/SS
- `Routing.kt` — `DELETE /dependencies`, `PATCH /dependencies`, `POST /resize-from-start`
- `IAtlasProjectTaskRepo.kt` — `updateDependency(predecessorId, successorId, type, lagDays)`
- `AtlasProjectTaskRepoPostgres.kt` — реализация `updateDependency`
- `GanttBar.tsx` — двусторонние link-маркеры (start/end) + left resize handle
- `GanttTaskLayer.tsx` — left resize state machine, target side selector, hit-area + hover для стрелок, `clickedDep` попап
- `GanttChart.tsx` — `handleChangeDependencyType`, `handleResizeFromStart`, order-preserving merge
- `DependencyActionPopover.tsx` — новый: попап с кнопками FS/SS/FF/SF + удаление
- `projectTasksApi.ts` — `changeDependencyType()`, `resizeTaskFromStart()`
- `useProjectTasks.ts` — `useChangeDependencyType()`, `useResizeTaskFromStart()`
- Новый DTO: `ChangeDependencyTypeCommandDto.kt`

### Технический долг, устранённый в рамках фазы D

| Проблема | Фикс |
|----------|------|
| SF лаг рассчитывался из текущих позиций при смене типа | Лаг по умолчанию: SF→1, остальные→0 (без `calculateLag`) |
| `setAllTasks(newPlan.tasks)` пересортировывал задачи | Order-preserving merge: `prev.map(t => updated ?? t)` |

---

## Фаза A: Технический долг ✅ (завершена 2026-03-03)

| Задача | Статус | Комментарий |
|--------|--------|-------------|
| A1: Рефакторинг BFS-каскада | ✅ | Выделен `cascadeBfs()` — один метод вместо 3 копий |
| A2: Убрать `println`-дебаг | ✅ | 7 строк удалены из `Routing.kt` |
| A3: Unit-тесты доменной модели | ✅ | 25 тестов: `TimelineCalendar` (12), `ProjectPlan` (11), `TaskSchedule` (2) |
| A4: Интеграционные тесты API | ✅ | 7 тестов Ktor-эндпоинтов с in-memory repo |

### Изменения в коде (Фаза A)
- `ProjectPlan.kt` — BFS-каскад вынесен в приватный `cascadeBfs(seedTaskId, calendar)`
- `Routing.kt` — удалён дебаг-вывод
- `AppConfig.kt` — добавлен конструктор `AppConfig(repo)` для тестов
- `common/build.gradle.kts` — добавлена зависимость `kotlin("test")` в `commonTest`
- Новые файлы: `TimelineCalendarTest.kt`, `ProjectPlanTest.kt`, `TaskScheduleTest.kt`, `RoutingTest.kt`

---

## Фаза G: Граф задач ✅ (завершена 2026-03-09)

| Задача | Статус | Комментарий |
|--------|--------|-------------|
| G1: `topologicalSort()` — алгоритм Кана | ✅ | `TopologicalSort.kt` — BFS-based, обнаруживает циклы |
| G2: `validateNoCycles()` | ✅ | Обёртка над `topologicalSort()` с try/catch |
| G3: Рефакторинг `recalculateAll()` | ✅ | Заменён ad-hoc BFS на `topologicalSort()` |
| G4: `snapshot()` — глубокая копия | ✅ | Глубокая копия mutable maps/sets для what-if |
| G5: Unit-тесты графовых операций | ✅ | Линейная цепочка, ромб, множественные корни, цикл |

### Изменения в коде (Фаза G)
- **Новый:** `TopologicalSort.kt` — алгоритм Кана
- **Изменить:** `ProjectPlan.kt` — `recalculateAll()` на топосортировке, `snapshot()`, `validateNoCycles()`
- **Новый:** `TopologicalSortTest.kt`

---

## Фаза H: Критический путь (CPM) ✅ (завершена 2026-03-09)

| Задача | Статус | Комментарий |
|--------|--------|-------------|
| H1: Forward pass (ES/EF) | ✅ | `CriticalPathAnalysis.kt` |
| H2: Backward pass (LS/LF) + slack | ✅ | slack = workingDaysBetween(ES, LS) |
| H3: DTOs | ✅ | `CpmTaskDto`, `CriticalPathDto` |
| H4: Маппер | ✅ | `CriticalPathResult.toDto()` |
| H5: `GET /critical-path` | ✅ | On-demand CPM вычисление |
| H6: Unit-тесты CPM | ✅ | Forward, backward, slack, critical chain |
| H7: `useCriticalPath()` hook | ✅ | С invalidation при изменении плана |
| H8: Подсветка критических баров | ✅ | `ring-2 ring-red-500` |
| H9: Красные стрелки | ✅ | Стрелки между критическими задачами |
| H10: Tooltip со slack | ✅ | «Запас: N дн.» / «Критическая задача» |

### Изменения в коде (Фаза H)
- **Новый:** `CriticalPathAnalysis.kt` — forward/backward pass
- **Новый:** `CriticalPathDto.kt` — DTO для API
- **Изменить:** `DomainToTransport.kt` — маппер `CriticalPathResult.toDto()`
- **Новый:** `CriticalPath.kt` (routes) — `GET /critical-path`
- **Изменить:** `Routing.kt` — регистрация `criticalPath()`
- **Новый:** `CriticalPathAnalysisTest.kt`
- **Изменить:** `projectTasksApi.ts` — `getCriticalPath()`
- **Изменить:** `useProjectTasks.ts` — `useCriticalPath()`
- **Изменить:** `GanttBar.tsx`, `GanttChart.tsx`, `GanttTaskLayer.tsx` — визуализация CPM

---

## Фаза I: Аналитические виды ✅ (завершена 2026-03-10)

| Задача | Статус | Комментарий |
|--------|--------|-------------|
| I1: Блокирующая цепочка | ✅ | BFS назад по predecessors, топологическая сортировка подграфа |
| I2: Доступные задачи | ✅ | Фильтр: запланирована, не DONE, start ≤ today, все preds DONE |
| I3: What-if (start + end) | ✅ | `snapshot()` + `changeTaskStartDate` / `changeTaskEndDate`, сравнение дельт |
| I4: DTOs для аналитики | ✅ | `BlockerChainDto`, `AvailableTasksDto`, `WhatIfDto` |
| I5: 4 GET-эндпоинта | ✅ | `/analysis/blocker-chain/{id}`, `/analysis/available-tasks`, `/analysis/what-if`, `/analysis/what-if-end` |
| I6: Unit-тесты (12 шт.) | ✅ | blockerChain (3), availableTasks (4), whatIf (3), whatIfEnd (2) |
| I7: Frontend hooks + API | ✅ | `useBlockerChain`, `useAvailableTasks`, `useWhatIf`, `useWhatIfEnd` |
| I8: AnalysisPanel | ✅ | Правый сайдбар (320px), 3 таба: Блокеры / Доступные / Что-если (start + end) |

### Изменения в коде (Фаза I)
- **Новый:** `AnalysisQueries.kt` — `blockerChain()`, `availableTasks()`, `whatIf()`, `whatIfEnd()`
- **Новый:** `BlockerChainDto.kt`, `AvailableTasksDto.kt`, `WhatIfDto.kt`
- **Изменить:** `DomainToTransport.kt` — 6 маппер-функций
- **Новый:** `Analysis.kt` (routes) — 4 GET-эндпоинта
- **Изменить:** `Routing.kt` — регистрация `analysis()`
- **Новый:** `AnalysisQueriesTest.kt`
- **Новый:** `analysis.schema.ts` — Zod-схемы
- **Изменить:** `types/index.ts`, `projectTasksApi.ts`, `useProjectTasks.ts`
- **Изменить:** `timelineCalendarStore.ts` — состояние панели аналитики
- **Новый:** `AnalysisPanel.tsx` — правый сайдбар
- **Изменить:** `GanttChart.tsx` — кнопка-тоггл, рендер панели

---

## UX-улучшения (2026-03-10)

| Функция | Статус | Комментарий |
|---------|--------|-------------|
| Scroll-to-today | ✅ | При загрузке графика сегодняшний день по центру |
| Drag-to-reorder задач | ✅ | Перетаскивание задач за grip-handle для смены порядка |

### Изменения в коде (UX-улучшения)

**Scroll-to-today:**
- `GanttChart.tsx` — `hasScrolledToToday` ref, useEffect с `getDayOffset()`, сброс при смене viewMode

**Drag-to-reorder:**
- **Backend:**
  - `ProjectTask.kt` — добавлено `val sortOrder: Int = 0`
  - `ProjectTasksTable.kt` — добавлен `sort_order` столбец
  - `IAtlasProjectTaskRepo.kt` — добавлен `reorderTasks(orderedIds)`
  - `AtlasProjectTaskRepoPostgres.kt` — `orderBy(sortOrder)`, `reorderTasks()`, `sortOrder` в inserts/mapper
  - **Новый:** `ReorderTasks.kt` (routes) — `POST /reorder-tasks`
  - `Routing.kt` — регистрация `reorderTasks()`
  - **Новый:** `V5__add_sort_order.sql` — миграция
- **Frontend:**
  - `projectTasksApi.ts` — `reorderTasks(orderedIds)`
  - `useProjectTasks.ts` — `useReorderTasks()` mutation hook
  - `GanttTaskList.tsx` — `SortableTaskRow` с `useSortable` из `@dnd-kit/react/sortable`
  - `GanttTaskRow.tsx` — `dragHandleRef` prop + `GripVertical` иконка (вместо `useDraggable`)
  - `GanttChart.tsx` — `handleReorder` с оптимистичным обновлением, `isSortableOperation` в `handleDragEnd`

---

## Оставшийся технический долг

| Проблема | Серьёзность | Комментарий |
|----------|-------------|-------------|
| `CreateTaskCommand` — мёртвый код | Низкая | Определён, но не включён в union type `TaskCommand` |

---

## Фаза J: Ресурсы (3.1) ✅ (завершена 2026-03-15)

| Задача | Статус | Комментарий |
|--------|--------|-------------|
| J1: Доменные модели | ✅ | `Resource`, `ResourceId`, `ResourceName`, `ResourceType`, `ResourceCalendarOverride` |
| J2: Миграция V6 | ✅ | `V6__create_resources.sql` — таблицы `resources` + `resource_calendar_overrides` |
| J3: Репозиторий | ✅ | `IResourceRepo` + `ResourceRepoPostgres` + `ResourceRepoInMemory` |
| J4: DTOs | ✅ | `ResourceDto`, `CreateResourceCommandDto`, `UpdateResourceCommandDto`, `ResourceCalendarOverrideDto` |
| J5: API-эндпоинты | ✅ | 7 эндпоинтов: CRUD `/resources` + `/resources/{id}/calendar-overrides` |
| J6: Frontend | ✅ | `/resources` страница, `ResourcesPage`, `CreateResourceDialog`, `ResourceCalendarOverridesEditor` |
| J7: Тесты | ❌ | Не написаны |

### Изменения в коде (Фаза J)
- **Новые:** `Resource.kt`, `ResourceId.kt`, `ResourceName.kt`, `ResourceType.kt`, `ResourceCalendarOverride.kt`
- **Новые:** `ResourcesTable.kt`, `ResourceCalendarOverridesTable.kt`
- **Новые:** `IResourceRepo.kt`, `ResourceRepoPostgres.kt`, `ResourceRepoInMemory.kt`
- **Новые:** `ResourceDto.kt`, `Resources.kt` (routes)
- **Изменить:** `Routing.kt` — регистрация `resources()`
- **Изменить:** `AppConfig.kt` — добавлен `IResourceRepo`
- **Новые (frontend):** `resource.schema.ts`, `resourcesApi.ts`, `useResources.ts`, `ResourcesPage.tsx`, `CreateResourceDialog.tsx`, `ResourceCalendarOverridesEditor.tsx`, `src/app/resources/page.tsx`

---

## Фаза K: Назначения (3.2) ✅ (завершена 2026-03-15)

| Задача | Статус | Комментарий |
|--------|--------|-------------|
| K1: Доменные модели | ✅ | `TaskAssignment` (hoursPerDay, plannedEffortHours), `AssignmentDayOverride`, `ResourceLoadCalculator`, `ResourceDayLoad`, `ResourceLoadResult`, `OverloadReport` |
| K2: Миграция V7+V8 | ✅ | `V7__create_assignments.sql`, `V8__assignment_enhancements.sql` (plannedEffortHours + day overrides) |
| K3: Репозиторий | ✅ | 6 методов назначений + 4 метода day overrides в `IResourceRepo` |
| K4: API-эндпоинты | ✅ | `/assignments` CRUD, `/assignments/{id}/day-overrides` CRUD, `/resource-load` (GET, GET/{resourceId}) |
| K5: Frontend | ✅ | `AssignmentEditor` (попап), `DayOverrideEditor` (клик по ячейке дня), `ResourceLoadPage` (гистограмма нагрузки), индикаторы на `GanttBar` + `GanttTaskRow` |
| K6: Тесты | ❌ | Не написаны |

### Изменения в коде (Фаза K)
- **Новые:** `TaskAssignment.kt`, `AssignmentId.kt`, `AssignmentDayOverride.kt`, `ResourceLoadCalculator.kt`
- **Новые:** `TaskAssignmentsTable.kt`, `AssignmentDayOverridesTable.kt`
- **Новые:** `AssignmentDto.kt` (DTOs: assignment, day override, load report), `Assignments.kt` (routes)
- **Изменить:** `DomainToTransport.kt` — маппинг назначений, нагрузки, day overrides
- **Изменить:** `Routing.kt` — регистрация `assignments()`
- **Изменить:** `GanttBar.tsx`, `GanttTaskLayer.tsx`, `GanttCalendarGrid.tsx`, `GanttTaskRow.tsx`, `GanttTaskList.tsx`, `GanttChart.tsx` — индикаторы назначений, попап AssignmentEditor
- **Новые (frontend):** `assignment.schema.ts`, `assignmentsApi.ts`, `useAssignments.ts`, `AssignmentEditor.tsx`, `DayOverrideEditor.tsx`, `ResourceLoadPage.tsx`, `src/app/resource-load/page.tsx`

### Модель назначений
- `hoursPerDay` — дефолтная ставка (ч/день) на всю длительность задачи
- `plannedEffortHours` — плановая трудоёмкость (опционально); если задана, рассчитывается `effortDeficit`
- `AssignmentDayOverride` — точечная корректировка часов на конкретный день (override > default)
- `ResourceLoadCalculator` использует: `dayOverride[date]?.hours ?? assignment.hoursPerDay`

---

## Фаза L: Выравнивание (3.3) ✅ (завершена 2026-03-15)

| Задача | Статус | Комментарий |
|--------|--------|-------------|
| L1: Движок выравнивания | ✅ | `ResourceLevelingEngine` — serial leveling: snapshot, CPM slack, сдвиг наименее критичных задач |
| L2: API-эндпоинты | ✅ | `POST /leveling/preview`, `POST /leveling/apply` |
| L3: Frontend | ✅ | Кнопка «Выровнять» на `/resource-load`, `LevelingPreviewDialog` (таблица сдвигов, resolved/remaining) |
| L4: Тесты | ❌ | Не написаны |

### Изменения в коде (Фаза L)
- **Новый:** `ResourceLevelingEngine.kt` — serial leveling алгоритм
- **Новый:** `LevelingResultDto.kt`
- **Новый:** `Leveling.kt` (routes) — preview + apply
- **Изменить:** `DomainToTransport.kt` — `LevelingResult.toDto()`
- **Изменить:** `Routing.kt` — регистрация `leveling()`
- **Новые (frontend):** `LevelingPreviewDialog.tsx`
- **Изменить:** `assignmentsApi.ts` — `previewLeveling()`, `applyLeveling()`
- **Изменить:** `useAssignments.ts` — `useLevelingPreview()`, `useApplyLeveling()`

---

## Фаза M: Доменные модели + Миграция БД (4.1) ✅ (завершена 2026-03-15)

| Задача | Статус | Комментарий |
|--------|--------|-------------|
| M1: Доменные модели | ✅ | `Portfolio`, `PortfolioId`; `ProjectPlan` расширен: `name`, `portfolioId`, `priority` |
| M2: Миграция V9 | ✅ | Таблица `portfolios`; `project_plans` + metadata; ресурсы глобальные (убран `project_plan_id`) |
| M3: Репозиторий портфелей | ✅ | `IPortfolioRepo` + `PortfolioRepoPostgres` + `PortfolioRepoInMemory` |
| M4: Рефакторинг `.single()` | ✅ | `projectPlan(planId)`, `createTask(planId, ...)`, `addDependency(planId, ...)` |
| M5: Глобальные ресурсы | ✅ | `listResources()` / `createResource(resource)` без `planId` |

### Изменения в коде (Фаза M)
- **Новые:** `Portfolio.kt`, `PortfolioId.kt`, `IPortfolioRepo.kt`, `PortfolioRepoPostgres.kt`, `PortfolioRepoInMemory.kt`, `PortfoliosTable.kt`
- **Изменить:** `ProjectPlan.kt` — name, portfolioId, priority + snapshot()
- **Изменить:** `IAtlasProjectTaskRepo.kt` — planId параметр
- **Изменить:** `IResourceRepo.kt` — убран planId из ресурсных методов
- **Изменить:** `AtlasProjectTaskRepoPostgres.kt` — убраны `.single()`, чтение новых колонок
- **Изменить:** `ResourceRepoPostgres.kt`, `ResourcesTable.kt` — убран projectPlanId
- **Изменить:** `ProjectPlansTable.kt` — name, portfolioId, priority
- **Изменить:** `AppConfig.kt`, `Databases.kt` — добавлен portfolioRepo
- **Новый:** `V9__create_portfolios_and_global_resources.sql`

---

## Фаза N: Backend API (4.1) ✅ (завершена 2026-03-15)

| Задача | Статус | Комментарий |
|--------|--------|-------------|
| N1: Проектные эндпоинты | ✅ | Все маршруты под `/projects/{projectId}/` |
| N2: Портфельные эндпоинты | ✅ | CRUD `/portfolios`, проекты `/portfolios/{id}/projects` |
| N3: Глобальные ресурсы | ✅ | `/resources` без project scope |
| N4: Тесты | ✅ | `RoutingTest.kt` обновлён |

### Изменения в коде (Фаза N)
- **Новый:** `Portfolios.kt` (routes) — CRUD портфелей + проектов
- **Изменить:** `Routing.kt` (old) — все маршруты в `route("/projects/{projectId}")`
- **Изменить:** `Routing.kt` (plugins/) — project-scoped + global разделение
- **Изменить:** `ProjectPlan.kt`, `CriticalPath.kt`, `Analysis.kt`, `ReorderTasks.kt`, `Assignments.kt`, `Leveling.kt` — `Routing` → `Route`, извлечение `projectId`
- **Изменить:** `Resources.kt` — убран `taskRepo`, глобальный scope
- **Изменить:** `RoutingTest.kt` — новые URL-ы, новые сигнатуры

---

## Фаза O: Frontend (4.1) ❌ (не начата)

Навигация по портфелям/проектам, миграция API-вызовов фронтенда.

## Фаза P: Межпроектные конфликты (4.2) ❌ (не начата)

Кросс-проектная нагрузка ресурсов, выравнивание по приоритетам проектов.

Подробный план: [IMPLEMENTATION_PLAN_STAGE4.md](IMPLEMENTATION_PLAN_STAGE4.md)

---

## Этапы 5–6

Не начаты.

---

## Связанные документы

- [Дорожная карта](../ROAD_MAP.md)
- [План реализации — Этап 1](IMPLEMENTATION_PLAN.md)
- [План реализации — Этап 2](IMPLEMENTATION_PLAN_STAGE2.md)
- [План реализации — Этап 3](IMPLEMENTATION_PLAN_STAGE3.md)
- [План реализации — Этап 4](IMPLEMENTATION_PLAN_STAGE4.md)
- [Обсуждение команды](TEAM_DISCUSSION.md)
