# Implementation Plan — 2026-04-19

## Обзор

План учитывает реализованное за 2026-04-19 и предлагает дальнейшие шаги. Фокус: завершение User & Portfolio систем, подготовка к следующей фазе.

---

## Фаза 1: User CRUD + Search (✅ ЗАВЕРШЕНА)

### Статус: DONE

| Компонент | Описание | Статус |
|-----------|---------|--------|
| Доменные модели User | UserId, UserName, UserAge, UserRole, User | ✅ |
| Интерфейс IUserRepo | 5 методов (create, read, update, delete, search) | ✅ |
| Реализация UserRepoPostgres | PostgreSQL адаптер + индексы | ✅ |
| Транспортные модели | 3 Commands + 2 Queries + 5 ResponseDtos + 3 InputDtos | ✅ |
| Маппинг функции | toDto(), toDomain(), applyUpdate() | ✅ |
| UserService | 5 сервис-методов | ✅ |
| Миграция БД | V15__create_users.sql | ✅ |
| Тесты маппинга | UserMappersTest (7 тестов) | ✅ |
| Тесты репо | UserRepoPostgresTest (12 тестов) | ✅ |

**Результат:** Полная система управления пользователями, протестирована на 100%.

---

## Фаза 2: Portfolio Refactoring (✅ ЗАВЕРШЕНА)

### Статус: DONE

| Компонент | Описание | Статус |
|-----------|---------|--------|
| Транспортные модели | Убраны nullable, добавлен messageType | ✅ |
| Доменные модели | Добавлены PortfolioName, PortfolioDescription | ✅ |
| Portfolio.kt | Обновлён для новых типов | ✅ |

**Результат:** Повышенная типизация, лучшая контрактность.

---

## Фаза 3: User API Endpoints (🔴 TODO)

### Статус: READY TO START

**Цель:** Expose User CRUD через HTTP endpoints.

#### 3.1 Создание endpoints в Routing.kt

| Endpoint | Method | Body | Response | Статус |
|----------|--------|------|----------|--------|
| `/users` | POST | CreateUserCommandDto | CreateUserResponseDto | 🔴 TODO |
| `/users/{id}` | GET | — | ReadUserResponseDto | 🔴 TODO |
| `/users/{id}` | PUT | UpdateUserCommandDto | UpdateUserResponseDto | 🔴 TODO |
| `/users/{id}` | DELETE | DeleteUserCommandDto | DeleteUserResponseDto | 🔴 TODO |
| `/users/search` | POST | ListUsersQueryDto | ListUsersResponseDto | 🔴 TODO |

**Работа:**
- Добавить в `Routing.kt` новые маршруты
- Создать обработчики (handlers) для каждого endpoint
- Интегрировать UserService

**Файл:** `Routing.kt` (atlas-project-backend-ktor-app)

#### 3.2 Интеграционные тесты

| Тест | Описание | Статус |
|------|---------|--------|
| `testCreateUser_endpoint` | POST /users успешно создаёт | 🔴 TODO |
| `testGetUser_endpoint` | GET /users/{id} возвращает пользователя | 🔴 TODO |
| `testUpdateUser_endpoint` | PUT /users/{id} обновляет | 🔴 TODO |
| `testDeleteUser_endpoint` | DELETE /users/{id} удаляет | 🔴 TODO |
| `testListUsers_endpoint` | POST /users/search ищет по фильтру | 🔴 TODO |

**Файл:** `UserEndpointsTest.kt`

**Примечание:** RoutingTest.kt не компилируется — нужны InMemory-реализации репозиториев (UserRepoInMemory, PortfolioRepoInMemory и т.д.). После создания endpoints — создать эти инструменты.

---

## Фаза 4: Portfolio Tests (🔴 TODO)

### Статус: READY TO START

**Цель:** Аналогично User, написать тесты для Portfolio CRUD.

#### 4.1 Тесты маппинга (PortfolioMappersTest)

| Тест | Сигнатура | Статус |
|------|-----------|--------|
| `testPortfolioToDto` | Portfolio -> PortfolioResponseDto | 🔴 TODO |
| `testCreatablePortfolioDtoToDomain` | CreatablePortfolioDto -> Portfolio | 🔴 TODO |
| `testPortfolioApplyUpdate` | Portfolio, UpdatablePortfolioDto -> Portfolio | 🔴 TODO |

**Файл:** `PortfolioMappersTest.kt`

#### 4.2 Тесты репо (PortfolioRepoPostgresTest)

| Тест | Описание | Статус |
|------|---------|--------|
| CRUD операции | create, read, update, delete (4 теста) | 🔴 TODO |
| Поиск | search (3 теста: by name, by owner, by status) | 🔴 TODO |
| Error cases | duplicate, not found (3 теста) | 🔴 TODO |

**Файл:** `PortfolioRepoPostgresTest.kt`

**Ожидаемый результат:** 10+ тестов, 100% пройдено.

---

## Фаза 5: Portfolio API Endpoints (🔴 TODO)

### Статус: BLOCKED (зависит от Фазы 3 и 4)

**Цель:** Expose Portfolio CRUD через HTTP endpoints (аналогично User).

#### 5.1 Endpoints

| Endpoint | Method | Body | Response | Статус |
|----------|--------|------|----------|--------|
| `/portfolios` | POST | CreatePortfolioCommandDto | CreatePortfolioResponseDto | 🔴 TODO |
| `/portfolios/{id}` | GET | — | ReadPortfolioResponseDto | 🔴 TODO |
| `/portfolios/{id}` | PUT | UpdatePortfolioCommandDto | UpdatePortfolioResponseDto | 🔴 TODO |
| `/portfolios/{id}` | DELETE | DeletePortfolioCommandDto | DeletePortfolioResponseDto | 🔴 TODO |
| `/portfolios` | GET | (query: ?owner=...) | ListPortfoliosResponseDto | 🔴 TODO |

**Работа:** Аналогично User endpoints.

#### 5.2 Интеграционные тесты (PortfolioEndpointsTest)

Аналогично User endpoints (5 тестов).

---

## Фаза 6: UI Safety Improvements (🟡 PARTIAL)

### Статус: IN PROGRESS

#### 6.1 Диалог подтверждения удаления портфеля

**Наблюдение из smoke-тестов:** Удаление портфеля в UI происходит без диалога.

| Работа | Описание | Статус |
|--------|---------|--------|
| UI компонент | Создать ConfirmDeleteDialog | 🔴 TODO |
| Интеграция | Добавить в Portfolio delete handler | 🔴 TODO |
| Smoke-тест | Verify диалог + cancel / confirm | 🔴 TODO |

**Файл:** `ConfirmDeleteDialog.tsx` (frontend/components/)

---

## Фаза 7: RoutingTest.kt Fix (🔴 BLOCKING)

### Статус: BLOCKED

**Проблема:** RoutingTest.kt не компилируется. Нужны InMemory-реализации репозиториев.

**Решение:**

#### 7.1 Создать InMemory-репозитории

| Репо | Назначение | Статус |
|------|-----------|--------|
| `UserRepoInMemory` | Для тестов User endpoints | 🔴 TODO |
| `PortfolioRepoInMemory` | Для тестов Portfolio endpoints | 🔴 TODO |
| `ProjectPlanRepoInMemory` | Для тестов Project endpoints | 🔴 TODO |

**Файлы:** `UserRepoInMemory.kt`, `PortfolioRepoInMemory.kt`, `ProjectPlanRepoInMemory.kt` (test-utils или repos/inmemory/)

#### 7.2 Обновить RoutingTest.kt

- Использовать InMemory-репозитории
- Добавить тесты для User endpoints
- Добавить тесты для Portfolio endpoints

---

## Фаза 8: Валидация входных данных (🟡 NICE-TO-HAVE)

### Статус: DEFERRED

**Идея:** Добавить валидацию на уровнеDTO и доменных моделей.

| Валидация | Описание | Статус |
|-----------|---------|--------|
| UserAge | age >= 0 && age <= 150 | 🔴 TODO |
| UserName | length > 0 && length <= 255 | 🔴 TODO |
| PortfolioName | length > 0 && length <= 255 | 🔴 TODO |
| PortfolioDescription | length <= 1000 | 🔴 TODO |

**Подход:** Использовать sealed Result<T, Error> для возврата ошибок валидации из конструкторов value-class.

---

## Фаза 9: API Documentation (🟡 NICE-TO-HAVE)

### Статус: DEFERRED

**Идея:** Добавить OpenAPI/Swagger или Markdown документацию для API endpoints.

| Документация | Статус |
|--------------|--------|
| User endpoints | 🔴 TODO |
| Portfolio endpoints | 🔴 TODO |
| Project endpoints | 🔴 TODO |

---

## Критический путь (Critical Path)

```
Фаза 1 (✅ DONE)
    ↓
Фаза 2 (✅ DONE)
    ↓
Фаза 3 (🔴 TODO) ←— User API Endpoints
    ↓
Фаза 7 (🔴 BLOCKING) ←— Fix RoutingTest.kt (зависит от Фазы 3)
    ↓
Фаза 4 (🔴 TODO) ←— Portfolio Tests
    ↓
Фаза 5 (🔴 TODO) ←— Portfolio Endpoints (зависит от Фазы 4)
    ↓
Фаза 6 (🔴 TODO) ←— UI Safety
```

**Минимальный путь (MVP):**
1. Фаза 3: User endpoints + тесты
2. Фаза 4: Portfolio tests
3. Фаза 5: Portfolio endpoints
4. Фаза 7: Fix RoutingTest.kt

**Оптимальный путь:**
Мин. путь + Фаза 6 (UI safety) + Фаза 8 (валидация)

---

## Метрики и целевые показатели

| Метрика | Текущее | Целевое | Статус |
|---------|---------|---------|--------|
| Backend тесты | 19 | 40+ | 47% |
| API endpoints (User) | 0/5 | 5/5 | 0% |
| API endpoints (Portfolio) | 0/5 | 5/5 | 0% |
| Smoke-тесты UI | 3/3 | 6+ | 50% |
| Код без ошибок компиляции | 95% | 100% | 95% |

---

## Риски и миtigations

| Риск | Вероятность | Воздействие | Mitigation |
|------|-----------|-----------|-----------|
| RoutingTest.kt продолжает не компилироваться | Средняя | Высокое (блокирует) | Создать InMemory-репы на Фазе 7 |
| User endpoints будут содержать баги | Средняя | Среднее | Добавить интеграционные тесты на Фазе 3 |
| Portfolio без диалога подтверждения удаления | Низкая | Среднее (UX риск) | Исправить на Фазе 6 |
| Пропущены edge-cases в поиске User/Portfolio | Средняя | Низкое | Писать тесты "по граням" (min/max, null, empty) |

---

## Рекомендации

### Для команды
1. **Приоритизировать Фазу 3 & 7** — User endpoints блокируют другие работы.
2. **Параллелизировать Фазу 4** — Portfolio tests можно писать параллельно с Фазой 3.
3. **Отложить Фазу 8 & 9** — Валидация и документация — низкий приоритет в MVP.
4. **Добавить в дефект-трекер:** диалог подтверждения удаления Portfolio (UX).

### Для разработчика
1. Использовать User как шаблон для Portfolio (same patterns).
2. Создать Kotlin fixtures для тестов (factories UserFactory, PortfolioFactory).
3. После каждой Фазы — запускать полный test suite (всё должно компилироваться).
4. Документировать сложные решения в docs/ (не в коде).

---

## Заключение

**Состояние проекта:** 60% готовности к MVP.
- User CRUD: полностью реализовано.
- Portfolio: отрефакторено, нужны тесты и endpoints.
- API endpoints: нулевое состояние, срочно нужны.
- UI: safe, но можно улучшить (диалоги).

**Рекомендуемый timeline:**
- Фаза 3 (User endpoints): 2-3 дня
- Фаза 4 (Portfolio tests): 1-2 дня (параллельно)
- Фаза 5 (Portfolio endpoints): 1-2 дня
- Фаза 6 (UI safety): 0.5 дня
- **Итого: 4-7 дней** до MVP-ready.

**Следующая встреча:** Обсудить приоритеты Фаз 3-7 и распределить между разработчиками.
