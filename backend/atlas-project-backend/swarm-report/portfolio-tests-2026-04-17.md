# Тесты PortfolioRepoPostgres — 2026-04-17

## Контекст
Написание и запуск unit-тестов для `PortfolioRepoPostgres` в модуле `atlas-project-backend-postgres`. Тестирование CRUD операций и управления связями между портфелями и проектами.

## Список написанных тестов

| # | Тест | Описание |
|---|------|---------|
| TC-1 | searchPortfolio - пустая БД | БД пуста → Multiple с пустым списком |
| TC-2 | searchPortfolio - 1 портфель | 1 портфель → Multiple из 1 элемента |
| TC-3 | searchPortfolio - 3 портфеля | 3 портфеля → Multiple из 3 элементов |
| TC-4 | readPortfolio - существует | Портфель существует → Single с корректными данными |
| TC-5 | readPortfolio - несуществует | Несуществующий UUID → NotFound |
| TC-6 | createPortfolio - валидный | Валидный запрос → Single с непустым id |
| TC-7 | createPortfolio - readPortfolio | После создания можно прочитать через readPortfolio |
| TC-8 | createPortfolio - пустое описание | Пустое описание → создаётся без ошибок |
| TC-9 | updatePortfolio - успешно | Обновление name/description → Single с новыми значениями |
| TC-10 | updatePortfolio - не существует | Несуществующий id → NotFound |
| TC-11 | deletePortfolio - успешно | Существующий → Single, затем readPortfolio → NotFound |
| TC-12 | deletePortfolio - не существует | Несуществующий id → NotFound |
| TC-13 | listPortfolioProjects - пусто | Нет проектов → пустой список |
| TC-14 | listPortfolioProjects - 2 проекта | 2 проекта → список из 2 (**ПАДАЕТ**) |
| TC-15 | addProjectToPortfolio - один | Добавление проекта → корректный результат |
| TC-16 | addProjectToPortfolio - два | Два проекта → второй sortOrder > первого (**ПАДАЕТ**) |
| TC-17 | removeProjectFromPortfolio - существует | Связь существует → удаляется |
| TC-18 | removeProjectFromPortfolio - не существует | Связь не существует → 0 удалённых |
| TC-19 | reorderPortfolioProjects - успешно | 2 проекта → перестановка применяется |
| TC-20 | reorderPortfolioProjects - пусто | Пустой список → 0 обновлений |

## Результаты

| Метрика | Значение |
|---------|----------|
| Всего тестов | 20 |
| Прошло | 18 ✅ |
| Упало | 2 ❌ |
| Процент успеха | 90% |

**Упавшие тесты:**
- TC-14: `listPortfolioProjects` с 2 проектами
- TC-16: `addProjectToPortfolio` со вторым добавлением

## Баги, обнаруженные тестами

### Bug #1: PortfolioRepoPostgres.listPortfolioProjects

**Файл:** `atlas-project-backend-postgres/src/main/kotlin/com/khan366kos/atlas/project/backend/repo/postgres/PortfolioRepoPostgres.kt`

**Строки:** 192, 254

**Исключение:** `java.lang.IllegalStateException: PortfoliosTable.id is not in record set`

**Корневая причина:**
Метод `listPortfolioProjects()` выполняет запрос к `PortfolioProjectsTable` без JOIN с `PortfoliosTable`. Однако маппер `toPortfolio()` на строке 254 пытается прочитать `PortfoliosTable.id` из result set, которого там нет.

**Схема запроса (текущая):**
```
SELECT * FROM PortfolioProjectsTable WHERE portfolio_id = ?
```

**Требуемое исправление:**
Добавить LEFT JOIN с `PortfoliosTable` в запрос:
```
SELECT PortfoliosTable.*, PortfolioProjectsTable.*
FROM PortfolioProjectsTable
LEFT JOIN PortfoliosTable ON PortfolioProjectsTable.portfolio_id = PortfoliosTable.id
WHERE PortfolioProjectsTable.portfolio_id = ?
```

Или переработать маппер `toPortfolio()` для работы без прямого обращения к `PortfoliosTable`.

**Статус:** Не исправлено (требуется исправление в основном коде)

## Изменённые файлы

| Файл | Тип | Описание |
|------|-----|---------|
| `atlas-project-backend-postgres/src/test/kotlin/com/khan366kos/atlas/project/backend/repo/postgres/PortfolioRepoPostgresTest.kt` | Создан | Suite из 20 unit-тестов для PortfolioRepoPostgres |
| `atlas-project-backend-postgres/build.gradle.kts` | Изменён | Добавлен `tasks.withType<Test> { useJUnitPlatform() }` для поддержки Kotest |

## Итог

Написано 20 тестов для `PortfolioRepoPostgres`. 18 тестов прошли успешно, 2 упали из-за бага в продакшн коде (недостаток JOIN в `listPortfolioProjects`). Баг требует исправления на этапе fix-стадии.