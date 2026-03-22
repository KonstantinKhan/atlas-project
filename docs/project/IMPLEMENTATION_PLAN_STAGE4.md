# Этап 4: Мультипроектность (Portfolio) — План реализации

> **Статус: 🔧 В процессе.** Фазы M, N и O завершены (бэкенд + фронтенд). Фаза P ожидает реализации.

## Context

Этапы 0–3 завершены (100%). Система умеет: CRUD задач, Gantt-планирование, зависимости (FS/SS/FF/SF), критический путь, аналитика, ресурсы с назначениями, выравнивание. Однако всё работает в рамках **одного проекта** — `ProjectPlansTable.selectAll().single()`.

Этап 4 из дорожной карты:
- 4.1 Портфель (несколько проектов, общие ресурсы, приоритеты)
- 4.2 Межпроектные конфликты (один ресурс в двух проектах, конкуренция за сроки)
- 4.3 Сценарное планирование — **отложено**, реализуется после стабилизации 4.1 + 4.2

---

## Архитектурные решения (согласовано с пользователем)

| Решение | Выбор |
|---------|-------|
| Привязка ресурсов | **Глобальные** — ресурсы не принадлежат ни проекту, ни портфелю |
| Иерархия | **Портфель → Проекты** — явная сущность Portfolio, группирующая проекты |
| Сценарное планирование (4.3) | **Отложено** — реализовать после стабилизации 4.1 + 4.2 |
| Обратная совместимость API | **Нет** — старые эндпоинты заменены на `/projects/{projectId}/` сразу |

---

## Фаза M: Доменные модели + Миграция БД ✅ (завершена)

**Цель:** Создать Portfolio, расширить ProjectPlan, сделать ресурсы глобальными. Без изменений API.

### M1: Доменные модели

| Файл | Статус | Описание |
|------|--------|----------|
| `common/models/portfolio/PortfolioId.kt` | ✅ | `@JvmInline value class PortfolioId(val value: String)` |
| `common/models/portfolio/Portfolio.kt` | ✅ | `data class Portfolio(id, name, description)` |
| `common/models/projectPlan/ProjectPlan.kt` | ✅ | Добавлены поля `name`, `portfolioId`, `priority`; обновлён `snapshot()` |

### M2: Миграция V9

| Файл | Статус | Описание |
|------|--------|----------|
| `V9__create_portfolios_and_global_resources.sql` | ✅ | Таблица `portfolios`; `project_plans` + name/portfolio_id/priority; дефолтный портфель; `resources` — удалён `project_plan_id` |

### M3: Репозиторий

| Файл | Статус | Описание |
|------|--------|----------|
| `IPortfolioRepo.kt` | ✅ | Интерфейс: CRUD портфелей + CRUD проектов в портфеле |
| `PortfolioRepoPostgres.kt` | ✅ | Postgres-реализация |
| `PortfolioRepoInMemory.kt` | ✅ | In-memory реализация |
| `PortfoliosTable.kt` | ✅ | Exposed table object |
| `ProjectPlansTable.kt` | ✅ | Добавлены `name`, `portfolioId`, `priority` |
| `ResourcesTable.kt` | ✅ | Удалён `projectPlanId` |

### M4: Рефакторинг интерфейсов

| Изменение | Статус | Описание |
|-----------|--------|----------|
| `IAtlasProjectTaskRepo.projectPlan(planId)` | ✅ | Принимает planId вместо `.single()` |
| `IAtlasProjectTaskRepo.createTask(planId, task)` | ✅ | Принимает planId |
| `IAtlasProjectTaskRepo.createTaskWithoutSchedule(planId, task)` | ✅ | Принимает planId |
| `IAtlasProjectTaskRepo.addDependency(planId, ...)` | ✅ | Принимает planId |
| `IResourceRepo.listResources()` | ✅ | Без planId (глобальные) |
| `IResourceRepo.createResource(resource)` | ✅ | Без planId (глобальные) |
| `AtlasProjectTaskRepoPostgres.kt` | ✅ | Убраны все 4 вызова `.selectAll().single()` |
| `ResourceRepoPostgres.kt` | ✅ | Убраны ссылки на `projectPlanId` |
| `AtlasProjectTaskRepoInMemory.kt` | ✅ | Обновлён под новый интерфейс |
| `ResourceRepoInMemory.kt` | ✅ | Обновлён под новый интерфейс |
| `AppConfig.kt` | ✅ | Добавлен `portfolioRepo: IPortfolioRepo` |
| `Databases.kt` | ✅ | Добавлен `PortfolioRepoPostgres` в `DatabaseRepos` |

---

## Фаза N: Backend API — Проектно-скопированные эндпоинты ✅ (завершена)

**Цель:** Все REST-эндпоинты получают префикс `/projects/{projectId}/`. Новые эндпоинты для портфелей. Ресурсы остаются глобальными.

### N1: Маршрутизация

| Файл | Статус | Описание |
|------|--------|----------|
| `Routing.kt` (old) | ✅ | Все маршруты под `route("/projects/{projectId}")`, кроме `/work-calendar` |
| `Routing.kt` (plugins/) | ✅ | Проектные маршруты под `route("/projects/{projectId}")`; глобальные `portfolios()`, `resources()` |
| `ProjectPlan.kt` | ✅ | `Route.projectPlan()`, путь `/plan`, извлекает `projectId` |
| `CriticalPath.kt` | ✅ | `Route.criticalPath()`, извлекает `projectId` |
| `Analysis.kt` | ✅ | `Route.analysis()`, извлекает `projectId` |
| `ReorderTasks.kt` | ✅ | `Route.reorderTasks()`, извлекает `projectId` |
| `Assignments.kt` | ✅ | `Route.assignments()`, извлекает `projectId` |
| `Leveling.kt` | ✅ | `Route.leveling()`, извлекает `projectId` |
| `Resources.kt` | ✅ | Глобальный `Routing.resources()` без `taskRepo` |

### N2: Новые маршруты

| Файл | Статус | Описание |
|------|--------|----------|
| `Portfolios.kt` | ✅ | CRUD портфелей + проектов внутри портфеля |

### N3: Карта эндпоинтов

**Проектные эндпоинты** (все под `/projects/{projectId}/`):

| Эндпоинт | Метод | Описание |
|----------|-------|----------|
| `/projects/{projectId}/plan` | GET | План проекта (Gantt) |
| `/projects/{projectId}/change-start` | POST | Сменить дату начала задачи |
| `/projects/{projectId}/change-end` | POST | Сменить дату окончания |
| `/projects/{projectId}/resize-from-start` | POST | Ресайз слева |
| `/projects/{projectId}/plan-from-end` | POST | Планирование назад |
| `/projects/{projectId}/project-tasks` | POST | Создать задачу |
| `/projects/{projectId}/project-tasks/create-in-pool` | POST | Создать задачу в пуле |
| `/projects/{projectId}/project-tasks/{id}` | PATCH/DELETE | Обновить/удалить задачу |
| `/projects/{projectId}/project-tasks/{id}/schedule` | POST | Назначить расписание |
| `/projects/{projectId}/dependencies` | POST/PATCH/DELETE | Зависимости |
| `/projects/{projectId}/dependencies/recalculate` | POST | Пересчёт |
| `/projects/{projectId}/reorder-tasks` | POST | Перестановка задач |
| `/projects/{projectId}/critical-path` | GET | Критический путь |
| `/projects/{projectId}/analysis/*` | GET | Аналитика |
| `/projects/{projectId}/assignments` | GET/POST | Назначения |
| `/projects/{projectId}/assignments/{id}` | PATCH/DELETE | Обновить/удалить |
| `/projects/{projectId}/assignments/{id}/day-overrides` | GET/POST/DELETE | Day overrides |
| `/projects/{projectId}/resource-load` | GET | Нагрузка ресурсов |
| `/projects/{projectId}/resource-load/{resourceId}` | GET | Нагрузка ресурса |
| `/projects/{projectId}/leveling/preview` | POST | Превью выравнивания |
| `/projects/{projectId}/leveling/apply` | POST | Применить выравнивание |

**Глобальные эндпоинты:**

| Эндпоинт | Метод | Описание |
|----------|-------|----------|
| `/work-calendar` | GET | Рабочий календарь |
| `/resources` | GET/POST | Список/создание ресурсов |
| `/resources/{id}` | PATCH/DELETE | Обновление/удаление ресурса |
| `/resources/{id}/calendar-overrides` | GET/POST/DELETE | Overrides календаря |
| `/portfolios` | GET/POST | Список/создание портфелей |
| `/portfolios/{id}` | GET/PATCH/DELETE | CRUD портфеля |
| `/portfolios/{id}/projects` | GET/POST | Проекты в портфеле |
| `/portfolios/{id}/projects/reorder` | PATCH | Приоритеты проектов |

### N4: Тесты

| Изменение | Статус | Описание |
|-----------|--------|----------|
| `RoutingTest.kt` | ✅ | Обновлены URL-ы на `/projects/1/...`, методы тест-репо под новые сигнатуры |

---

## Фаза O: Frontend — Мультипроектная навигация ✅ (завершена)

**Цель:** Пользователь видит портфели, переключается между проектами, Gantt показывает данные конкретного проекта.

### O1: Zustand store

| Изменение | Статус | Описание |
|-----------|--------|----------|
| Zustand store | ⏭️ | Не понадобился: `projectId` передаётся через URL params → props |

### O2: API-сервисы

| Файл | Статус | Изменение |
|------|--------|-----------|
| `projectTasksApi.ts` | ✅ | Все ~20 функций получают `projectId`; URL → `/projects/${projectId}/...` |
| `assignmentsApi.ts` | ✅ | Все функции получают `projectId` |
| `resourcesApi.ts` | ✅ | Без изменений (глобальные `/resources`) |
| `portfoliosApi.ts` | ✅ | **Новый** — CRUD портфелей и проектов |

### O3: Хуки

| Файл | Статус | Изменение |
|------|--------|-----------|
| `useProjectTasks.ts` | ✅ | `projectId` во все query keys и API-вызовы |
| `useAssignments.ts` | ✅ | То же |
| `useResources.ts` | ✅ | Без изменений (глобальные) |
| `usePortfolios.ts` | ✅ | **Новый** — portfolios + projects CRUD |

### O4: Zod-схемы

| Файл | Статус | Описание |
|------|--------|----------|
| `portfolio.schema.ts` | ✅ | Portfolio + ProjectSummary (объединён в один файл) |

### O5: Маршрутизация (Next.js App Router)

| Маршрут | Статус | Описание |
|---------|--------|----------|
| `page.tsx` (root) | ✅ | Redirect → `/portfolios` |
| `portfolios/page.tsx` | ✅ | `PortfolioListPage` |
| `portfolios/[portfolioId]/page.tsx` | ✅ | `PortfolioDashboard` |
| `projects/[projectId]/page.tsx` | ✅ | `GanttChart` (project-scoped) |
| `projects/[projectId]/resources/page.tsx` | ✅ | `ResourcesPage` |
| `projects/[projectId]/resource-load/page.tsx` | ✅ | `ResourceLoadPage` (project-scoped) |
| `resources/page.tsx` | ✅ | Redirect → `/portfolios` |
| `resource-load/page.tsx` | ✅ | Redirect → `/portfolios` |

### O6: Новые компоненты

| Компонент | Статус | Описание |
|-----------|--------|----------|
| `PortfolioListPage.tsx` | ✅ | Создание/переименование/удаление портфелей |
| `PortfolioDashboard.tsx` | ✅ | Список проектов с приоритетами, создание проекта |
| `CreateProjectDialog.tsx` | ⏭️ | Inline-форма в `PortfolioDashboard`, отдельный диалог не нужен |
| `ProjectSwitcher.tsx` | ⏭️ | Отложен — навигация через breadcrumbs портфель → проект |

### O7: Модификация существующих компонентов

| Компонент | Статус | Изменение |
|-----------|--------|-----------|
| `GanttChart.tsx` | ✅ | Принимает `projectId` prop, передаёт во все хуки |
| `AnalysisPanel.tsx` | ✅ | Принимает `projectId`, передаёт в sub-tabs |
| `AssignmentEditor.tsx` | ✅ | Принимает `projectId`, передаёт в assignment хуки |
| `ResourceLoadPage.tsx` | ✅ | Принимает `projectId`, project-scoped нагрузка |
| `DayOverrideEditor.tsx` | ✅ | Принимает `projectId`, передаёт в day override хуки |
| `ResourcesPage.tsx` | ✅ | Принимает `projectId` (для back-link), данные глобальные |

### Верификация

- `/portfolios` → список портфелей
- Создать портфель → создать проект внутри
- Клик по проекту → Gantt загружает правильные задачи
- Переключение проектов → данные меняются
- `/resources` → все ресурсы, независимо от проекта

---

## Фаза P: Межпроектные конфликты ресурсов (4.2) ❌ (не начата)

**Цель:** Обнаружение и визуализация перегрузки ресурсов через несколько проектов.

### P1: Backend

| Файл | Описание |
|------|----------|
| `CrossProjectLoadCalculator.kt` | Агрегация нагрузки ресурса по всем проектам портфеля |
| `CrossProjectLoadDto.kt` | DTO: нагрузка ресурса с разбивкой по проектам |
| `CrossProjectLoad.kt` (routes) | `GET /portfolios/{id}/cross-project-load` |
| | `POST /portfolios/{id}/cross-project-leveling/preview` |
| | `POST /portfolios/{id}/cross-project-leveling/apply` |

### P2: Frontend

| Файл | Описание |
|------|----------|
| `CrossProjectLoadChart.tsx` | Гистограмма нагрузки, stacked по проектам (цветовая кодировка) |
| `crossProjectApi.ts` | API для кросс-проектных данных |
| `useCrossProjectLoad.ts` | React Query хук |
| `portfolios/[portfolioId]/resource-load/page.tsx` | Страница кросс-проектной нагрузки |

### P3: Кросс-проектное выравнивание

- Объединение задач всех проектов портфеля в виртуальный план
- Сортировка по приоритету проекта: задачи высокоприоритетных проектов получают приоритет
- Использование существующего `ResourceLevelingEngine` на объединённом плане

### Верификация

- Назначить один ресурс на задачи в 2 разных проектах на пересекающиеся даты
- `GET /portfolios/{id}/cross-project-load` показывает суммарную перегрузку
- График нагрузки отображает вклад каждого проекта
- Превью кросс-проектного выравнивания сдвигает задачи менее приоритетного проекта

---

## Последовательность фаз

```
Фаза M  ✅  Доменные модели + миграция БД (только бэкенд)
Фаза N  ✅  Рефакторинг API (project-scoped эндпоинты + портфель CRUD)
Фаза O  ✅  Frontend: навигация, мультипроектность, миграция API
Фаза P  ❌  Кросс-проектные конфликты ресурсов + выравнивание
```

---

## Риски

| Риск | Митигация |
|------|-----------|
| Миграция V9 ломает данные | Создаёт дефолтный портфель, привязывает существующий план |
| ~20 frontend API-функций нужен `projectId` | Механическая замена: service → hook → component |
| Производительность кросс-проектного выравнивания | Ленивая загрузка — только проекты с общими ресурсами |
| Миграция маршрутов фронтенда | Next.js App Router поддерживает вложенные динамические маршруты нативно |

---

## Обновления документации

После каждой фазы обновить:
- `docs/backend/INDEX.md` и `details/*.md`
- `docs/frontend/INDEX.md` и `details/*.md`
- `docs/project/IMPLEMENTATION_STATUS.md`
- `docs/ROAD_MAP.md` — таблица прогресса
