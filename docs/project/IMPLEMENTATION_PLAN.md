# План реализации

> _Последнее обновление: 2026-03-07 (Фаза E завершена)_

Поэтапный план завершения Этапа 1 дорожной карты. Задачи сгруппированы по фазам, упорядочены по зависимостям.

**Размеры:** S = 1–2 часа | M = 3–5 часов | L = 1–2 дня

---

## Фаза A: Технический долг (подготовка) — ✅ ЗАВЕРШЕНА

> Цель: создать фундамент для безопасного добавления новых фич.

| ID | Задача | Слой | Размер | Статус |
|----|--------|------|--------|--------|
| A1 | Рефакторинг BFS-каскада в `ProjectPlan` | Backend | S | ✅ |
| A2 | Убрать `println`-дебаг из `Routing.kt` (7 шт.) | Backend | S | ✅ |
| A3 | Unit-тесты доменной модели (`ProjectPlan`, `TimelineCalendar`, `TaskSchedule`) | Backend | L | ✅ |
| A4 | Интеграционные тесты API (основные эндпоинты) | Backend | M | ✅ |

### Результаты

**A1** — Выделен приватный метод `cascadeBfs(seedTaskId, calendar)` в `ProjectPlan.kt`. Три метода (`changeTaskStartDate`, `changeTaskEndDate`, `addDependency`) используют его вместо inline BFS-кода.

**A2** — Удалены 7 строк `println("[Routing]...")` из `Routing.kt`. Не заменены на логгер.

**A3** — 25 unit-тестов:
- `TimelineCalendarTest` (12): `addWorkingDays`, `subtractWorkingDays`, `workingDaysBetween`, `currentOrNextWorkingDay` — включая выходные, праздники, пограничные случаи
- `ProjectPlanTest` (11): `changeTaskStartDate` (одиночная, каскад, цепочка), `changeTaskEndDate` (пересчёт duration, каскад), `addDependency` (FS, цикл, множественные predecessors)
- `TaskScheduleTest` (2): создание с `ProjectDate.Set` и `ProjectDate.NotSet`

**A4** — 7 интеграционных тестов Ktor-эндпоинтов с in-memory repo. `AppConfig` рефакторнут — добавлен конструктор `AppConfig(repo)` для тестов.

---

## Фаза B: Завершение 1.1 — Task Pool — ✅ ЗАВЕРШЕНА

> Цель: полный CRUD для задач.

| ID | Задача | Слой | Размер | Статус |
|----|--------|------|--------|--------|
| B1 | DELETE-эндпоинт для задачи | Backend | M | ✅ |
| B2 | UI кнопка удаления задачи | Frontend | M | ✅ |
| B3 | Диалог подтверждения удаления | Frontend | S | ✅ |

### Результаты

**B1** — Добавлен `deleteTask(id)` в `IAtlasProjectTaskRepo` и обе реализации. Postgres: каскадное удаление в одной транзакции (`TaskSchedulesTable` → `TaskDependenciesTable` → `ProjectTasksTable`). Эндпоинт `DELETE /project-tasks/{id}` возвращает 204; повторный запрос → 404.

**B2** — `DeleteTaskCommand` в системе команд (`TaskCommandType`, `TaskCommand`, `DeleteTaskCommand.type.ts`). Кнопка `Trash2` в `GanttTaskRow` — видна только при hover (`group`/`group-hover`). `useDeleteProjectTask` hook с оптимистичным обновлением кеша (фильтрует задачу и её зависимости).

**B3** — `ConfirmDeleteModal` на Tailwind: показывает название задачи и количество затронутых зависимостей. Кнопки «Отмена» и «Удалить». После подтверждения — задача и зависимости исчезают из стейта и Gantt-диаграммы.

---

## Фаза C: Завершение 1.2 — Gantt-планирование — ✅ ЗАВЕРШЕНА

> Цель: полноценное интерактивное планирование через Gantt-диаграмму.

| ID | Задача | Слой | Размер | Статус |
|----|--------|------|--------|--------|
| C1 | Drag бара (перемещение задачи) | Frontend | L | ✅ |
| C2 | Resize бара (изменение длительности) | Frontend | L | ✅ |
| C3 | Backend: эндпоинт для назначения расписания | Backend | M | ✅ |
| C4 | Назначение задачи из пула на таймлайн | Both | L | ✅ |
| C5 | Планирование назад (end → start) | Both | M | ✅ |
| C6 | Оптимистичные обновления для drag/resize | Frontend | M | ✅ |

### Результаты

**C1** — Mouse-driven drag в `GanttTaskLayer.tsx` (onMouseDown/Move/Up). При отпускании — новая дата по offset от `rangeStart`, `MoveTask`-команда. `prevTasksRef.current` + `onError`-откат.

**C2** — Resize-handle на правом краю `GanttBar`. При отпускании — `ResizeTask`-команда. Минимум 1 рабочий день. Аналогичный оптимистичный паттерн.

**C3** — `POST /project-tasks/{id}/schedule` принимает `{ start, duration }`, вычисляет `end = addWorkingDays(start, duration)`, сохраняет через `updateSchedule()`.

**C4** — `@dnd-kit/react` v0.3.2: `DragDropProvider` в `GanttChart`, `useDroppable` в `GanttCalendarGrid`, `useDraggable` в `GanttTaskRow` (только пул). `handleDragEnd` → дата по X-позиции → `AssignSchedule`.

**C5** — `ProjectPlan.planFromEnd(taskId, newEnd, calendar)` → `subtractWorkingDays(newEnd, duration)` → `cascadeBfs()`. `POST /plan-from-end`. Фронт: end-дата пул-задачи → `PlanFromEnd`-команда.

**C6** — `setAllTasks((prev) => { prevTasksRef.current = prev; return newState })` — атомарный захват предыдущего стейта. `onError: () => setAllTasks(prevTasksRef.current)` для MoveTask, ResizeTask, AssignSchedule.

---

## Фаза D: Завершение 1.3 — Зависимости — ✅ ЗАВЕРШЕНА

> Цель: полная поддержка всех типов зависимостей с CRUD.

| ID | Задача | Слой | Размер | Статус |
|----|--------|------|--------|--------|
| D1 | DELETE-эндпоинт для зависимости | Backend | M | ✅ |
| D2 | Подключить удаление зависимости к API | Frontend | S | ✅ |
| D3 | UI выбора типа зависимости (FS/SS/FF/SF) | Frontend | M | ✅ |
| D4 | Визуализация SS/FF/SF стрелок | Frontend | M | ✅ |
| D5 | Пересчёт при удалении зависимости | Backend | M | ✅ |
| D6 | Смена типа зависимости (UI + API) | Both | M | ✅ |
| D7 | Resize слева (изменение начала, сохранение конца) | Both | M | ✅ |
| D8 | UX стрелок: hover-эффект + попап + hit-area | Frontend | M | ✅ |
| D9 | Сохранение порядка задач при операциях с зависимостями | Frontend | S | ✅ |

### Результаты

**D1** — `DELETE /dependencies?from={id}&to={id}`. `deleteDependency` в репозитории + Postgres-реализация. Возвращает пересчитанный `GanttProjectPlan`.

**D2** — `handleRemoveDependency` в `GanttChart.tsx` вызывает `deleteDependencyMutation` с оптимистичным удалением из стейта + откат через `refetchPlan()` при ошибке.

**D3** — Двусторонние маркеры на GanttBar (start/end) для создания зависимости. Тип определяется автоматически из комбинации fromSide × toSide (end→start=FS, start→start=SS, end→end=FF, start→end=SF). На целевой задаче — попап `[Start | Finish]` для выбора стороны.

**D4** — SVG-стрелки с разными точками привязки: FS (end→start), SS (start→start), FF (end→end), SF (start→end). Все типы визуализированы корректно.

**D5** — При удалении зависимости бэкенд пересчитывает расписание через `recalculateAll()`. Successor может сдвинуться назад, если ограничение снято.

**D6** — `PATCH /dependencies` + `changeDependencyType()` в `ProjectPlan.kt`. Попап `DependencyActionPopover` с кнопками FS/SS/FF/SF + кнопка удаления. Лаг по умолчанию: SF→1, остальные→0 (не рассчитывается из текущих позиций).

**D7** — `POST /resize-from-start` + `resizeTaskFromStart()` в `ProjectPlan.kt`. Resize-handle на левом краю GanttBar. Start меняется, end сохраняется, duration пересчитывается. Clamping только по FS/SS (FF/SF не ограничивают start).

**D8** — Невидимая hit-area (strokeWidth=14) под стрелками. Hover: утолщение (1.5→3) + подсветка (#818cf8). Клик: попап с типом связи + удаление. Escape / click-outside закрывает попап.

**D9** — `setAllTasks(newPlan.tasks)` заменён на order-preserving merge (`prev.map(t => updated ?? t)`) во всех обработчиках зависимостей, planFromEnd и assignSchedule.

---

## Фаза E: UX-улучшения для MVP — ✅ ЗАВЕРШЕНА

> Цель: довести пользовательский опыт до уровня MVP.

| ID | Задача | Слой | Размер | Статус |
|----|--------|------|--------|--------|
| E1 | Изменение статуса задачи из UI | Frontend | S | ✅ |
| E2 | Редактирование описания задачи | Both | M | ✅ |
| E3 | Обработка ошибок drag/resize | Frontend | S | ✅ |
| E4 | Индикатор длительности на баре | Frontend | S | ✅ |

### Результаты

**E1** — Dropdown-меню на статусной точке в `GanttTaskRow`. 5 статусов: Пусто, Бэклог, В работе, Готово, Заблокировано. Клик по точке → меню, выбор → `ChangeStatus`-команда → оптимистичное обновление + PATCH `/project-tasks/{id}`.

**E2** — `description` добавлен в `GanttTaskDto` (бэкенд) и `GanttTaskSchema` (фронт). Клик по названию задачи открывает два поля: название + описание. `UpdateDescription`-команда → оптимистичное обновление + PATCH. Описание отображается мелким текстом под названием.

**E3** — Компонент `Toast.tsx` (Tailwind, auto-dismiss через 4 сек). Все мутации drag/resize/move/assign/planFromEnd показывают toast при ошибке вместо `console.error`. Визуальный откат бара через `prevTasksRef.current` + toast-уведомление.

**E4** — На правом краю `GanttBar` отображается количество дней (`{durationDays}д`). Текст `text-white/70 text-[10px]`, не мешает названию задачи.

---

## Порядок реализации (рекомендуемый)

```
Фаза A (техдолг)
    ↓
Фаза B (Task Pool)      Фаза D (Зависимости)
    ↓                        ↓
Фаза C (Gantt-планирование)
    ↓
Фаза E (UX-улучшения)
```

Фазы B и D можно выполнять параллельно. Фаза C зависит от стабильного CRUD (B) и зависимостей (D). Фаза E — финальная полировка.

---

## Связанные документы

- [Дорожная карта](../ROAD_MAP.md)
- [Статус реализации](IMPLEMENTATION_STATUS.md)
- [Обсуждение команды](TEAM_DISCUSSION.md)
