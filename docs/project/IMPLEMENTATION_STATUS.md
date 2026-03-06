# Статус реализации

> _Последнее обновление: 2026-03-06 (Фаза D завершена)_

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

## Оставшийся технический долг

| Проблема | Серьёзность | Комментарий |
|----------|-------------|-------------|
| `CreateTaskCommand` — мёртвый код | Низкая | Определён, но не включён в union type `TaskCommand` |

---

## Этапы 2–6

Не начаты. Предпосылки для начала Этапа 2:
- Завершить Этап 1 (все три подэтапа)
- Покрыть доменную модель тестами

---

## Связанные документы

- [Дорожная карта](../ROAD_MAP.md)
- [План реализации](IMPLEMENTATION_PLAN.md)
- [Обсуждение команды](TEAM_DISCUSSION.md)
