# Тесты для User mappers и репозитория

**Дата:** 2026-04-19

## Описание задачи

Написание unit-тестов для маpперов (User.toDto(), CreatableUserDto.toDomain(), User.applyUpdate()) и PostgreSQL репозитория User (CRUD + search операции).

## Написанные тесты

### UserMappersTest (5 тестов)

1. **User.toDto() — map all fields correctly** ✅
   - Проверка маpпирования всех полей: id, name, email, role

2. **User.toDto() — map NONE user to empty id and default role** ✅
   - Маpпирование NONE пользователя с пустым id и ролью по умолчанию

3. **CreatableUserDto.toDomain() — map all fields and set id to NONE** ✅
   - Маpпирование DTO в domain модель с установкой id = NONE

4. **CreatableUserDto.toDomain() — throw IllegalArgumentException on unknown role** ✅
   - Проверка выброса исключения при неизвестной роли

5. **User.applyUpdate() — update all fields when all provided** ✅
   - Обновление всех полей при предоставлении всех данных

6. **User.applyUpdate() — keep original fields when dto fields are null** ✅
   - Сохранение исходных полей при null значениях в DTO

7. **User.applyUpdate() — update only name when only name provided** ✅
   - Частичное обновление только name поля

### UserRepoPostgresTest (12 тестов)

#### CRUD операции

1. **createUser — return Single with generated id** ✅
   - Создание пользователя и получение сгенерированного id

2. **createUser — persist user so it can be read back** ✅
   - Проверка персистентности созданного пользователя

3. **readUser — return Single when user exists** ✅
   - Чтение существующего пользователя

4. **readUser — return NotFound for unknown id** ✅
   - Возврат NotFound при запросе несуществующего пользователя

5. **updateUser — update fields and return Single** ✅
   - Обновление полей пользователя и возврат обновлённых данных

6. **updateUser — return NotFound when user does not exist** ✅
   - Возврат NotFound при обновлении несуществующего пользователя

7. **deleteUser — delete user and return its data** ✅
   - Удаление пользователя и возврат его данных

8. **deleteUser — return NotFound for unknown id** ✅
   - Возврат NotFound при удалении несуществующего пользователя

#### Search операции

9. **searchUsers — return all users when no filters** ✅
   - Возврат всех пользователей при отсутствии фильтров

10. **searchUsers — filter by name substring** ✅
    - Фильтрация по подстроке имени

11. **searchUsers — filter by role** ✅
    - Фильтрация по роли

12. **searchUsers — return empty list when no match** ✅
    - Возврат пустого списка при отсутствии совпадений

## Результаты тестирования

| Компонент | Количество тестов | Статус | Результат |
|-----------|-------------------|--------|-----------|
| UserMappersTest | 7 | ✅ Passed | Все тесты успешны |
| UserRepoPostgresTest | 12 | ✅ Passed | Все тесты успешны |
| **Итого** | **19** | ✅ Passed | **Все тесты успешны** |

## Инфраструктурные изменения

### Зависимости

- **`gradle/libs.versions.toml`** — добавлены kotest 5.9.1 зависимости
- **`atlas-project-backend-mappers/build.gradle.kts`** — подключены kotest для unit-тестирования маpперов
- **`atlas-project-backend-postgres/build.gradle.kts`** — подключены kotest + H2 in-memory база для интеграционного тестирования

### Конфигурация

- H2 database используется для тестирования PostgreSQL репозитория без необходимости реального подключения
- Kotest Should Spec синтаксис для читаемого описания тестов

## Итоги

Статус: **Done**

Все 19 тестов успешно реализованы и пройдены:
- 7 тестов маpперов (toDto, toDomain, applyUpdate)
- 12 тестов репозитория (создание, чтение, обновление, удаление, поиск)

Покрытие включает happy path сценарии и edge cases (null значения, несуществующие записи, фильтрация). Инфраструктура настроена для выполнения unit и интеграционных тестов в CI/CD пайплайне.
