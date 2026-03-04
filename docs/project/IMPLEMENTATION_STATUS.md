# Статус реализации

> _Последнее обновление: 2026-03-03 (Фаза A завершена)_

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

## Этап 1.1. Task Pool (~75%)

| Функция | Статус | Комментарий |
|---------|--------|-------------|
| Создание задач | ✅ | `CreateTaskInPoolCommand` → API → DB |
| Переименование | ✅ | `UpdateTitleCommand` → PATCH API |
| Задачи без дат | ✅ | Задачи в пуле существуют без расписания |
| Удаление | ❌ | Нет DELETE-эндпоинта, нет метода в репозитории, нет UI |

---

## Этап 1.2. Gantt-планирование (~50%)

| Функция | Статус | Комментарий |
|---------|--------|-------------|
| Назначение на таймлайн | 🔧 | Через date-пикеры, нет drag-to-assign из пула |
| Drag (перемещение бара) | ❌ | Нет обработчика `onMouseDown` для drag на баре |
| Resize (изменение длительности) | ❌ | Нет resize-handle на баре |
| Планирование вперёд | ✅ | `changeTaskStartDate` пересчитывает end через календарь |
| Планирование назад | ❌ | `subtractWorkingDays` есть в календаре, но не используется как фича |
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
| Удаление зависимости (API) | ❌ | Нет DELETE-эндпоинта для зависимостей |
| Пересчёт при удалении | ❌ | Без API удаления — пересчёт невозможен |

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
