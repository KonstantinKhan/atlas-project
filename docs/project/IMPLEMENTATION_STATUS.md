# Статус реализации

> _Последнее обновление: 2026-03-05 (Фаза C завершена)_

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
| Move (drag бара) | ❌ | Нет ни команды, ни обработчика drag |
| Resize (drag правого края) | ❌ | Нет ни команды, ни обработчика resize |

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

## Этап 1.3. Зависимости (~70%)

| Функция | Статус | Комментарий |
|---------|--------|-------------|
| FS (Finish–Start) | ✅ | Полный цикл: UI → API → Domain → DB → UI |
| SS / FF / SF | 🔧 | В домене (`ProjectPlan.calculateConstrainedStart`) и DTO — да. В UI — только FS (hardcoded) |
| Визуальные связи (SVG) | ✅ | SVG-стрелки в `GanttArrows` |
| Пересчёт цепочек (BFS-каскад) | ✅ | `ProjectPlan.changeTaskStartDate/changeTaskEndDate` с каскадом |
| Детекция циклов | ✅ | Реализовано в `ProjectPlan.addDependency` |
| Создание зависимости | ✅ | POST `/dependencies` |
| Удаление зависимости (UI) | 🔧 | Клик по стрелке убирает из React-стейта, **но не вызывает API** |
| Удаление зависимости (API) | ❌ | Нет DELETE-эндпоинта для зависимостей (каскадное удаление при удалении задачи — ✅) |
| Пересчёт при удалении | ❌ | Без API удаления зависимости — пересчёт невозможен |

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
