# Bug Fix: GET /portfolios/{id} → 500 Database error

**Дата**: 2026-04-15  
**Статус**: Fixed

---

## Описание проблемы

Эндпоинт `GET /portfolios/{id}` возвращал HTTP 500 с сообщением об ошибке базы данных при любом запросе, независимо от существования портфеля с указанным ID.

Ошибка блокировала получение данных портфеля по ID, что нарушало базовый функционал.

---

## Шаги воспроизведения

1. Запустить backend сервер
2. Выполнить HTTP запрос: `GET /portfolios/{any-id}`
3. **Ожидаемый результат**: HTTP 200 с данными портфеля или HTTP 404 если портфель не существует
4. **Фактический результат**: HTTP 500 с `{"error": "Database error"}`

---

## Диагноз (root cause)

**Файл**: `atlas-project-backend-postgres/src/main/kotlin/com/khan366kos/atlas/project/backend/repo/postgres/PortfolioRepoPostgres.kt`  
**Строка**: 65  
**Метод**: `readPortfolio`

**Причина**:

Метод `readPortfolio` возвращал неверный тип результата:
```kotlin
// Было (неверно):
return PortfolioRepoResult.Multiple(listOf(it))

// Должно быть:
return PortfolioRepoResult.Single(it)
```

**Цепочка ошибки**:

1. `readPortfolio` возвращал `PortfolioRepoResult.Multiple` вместо `PortfolioRepoResult.Single`
2. `PortfolioService.find()` ожидает `Single` результат
3. Получая `Multiple`, сервис попадал в else-ветку логики
4. Сервис бросал `PortfolioOperationFailedException`
5. Контроллер перехватывал исключение → HTTP 500

**Проверка других методов repo**: Аналогичных проблем в других методах репозитория не обнаружено.

---

## Что исправлено

**Файл**: `atlas-project-backend-postgres/src/main/kotlin/com/khan366kos/atlas/project/backend/repo/postgres/PortfolioRepoPostgres.kt`

**Изменение** (строка 65):
```kotlin
- return PortfolioRepoResult.Multiple(listOf(it))
+ return PortfolioRepoResult.Single(it)
```

**Объём**: 1 строка в 1 файле.

---

## Результаты Validation

| Сценарий | Результат | Статус |
|----------|-----------|--------|
| `GET /portfolios/{существующий-id}` | 200 OK с корректными данными портфеля | ✅ Pass |
| `GET /portfolios/{несуществующий-id}` | 404 Not Found | ✅ Pass |
| `GET /portfolios` (список портфелей) | 200 OK (список не затронут) | ✅ Pass |

**Регрессии**: Не обнаружены.

---

## Итоговый статус

✅ **Fixed** — баг полностью исправлен, все тесты валидации пройдены, регрессий нет.