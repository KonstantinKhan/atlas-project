# User CRUD + SEARCH + рефакторинг моделей — 2026-04-19

## Краткое описание

Реализована полная система управления пользователями (User CRUD) с поддержкой поиска, включая создание доменных моделей, транспортных DTO, репозитория PostgreSQL и сервиса. Параллельно выполнен рефакторинг моделей Portfolio: убраны nullable поля, добавлен messageType в ResponseDto, введены value-class для Portfolio имён.

---

## Итоги Research

- **Существующие паттерны:** @JvmInline value class для ID и Name; sealed class RepoResult (Single/Multiple/NotFound/DbError)
- **Архитектура транспорта:** CQRS — Commands/Queries/ResponseDto с @Serializable
- **Инфраструктура:** PostgreSQL, Exposed ORM, newSuspendedTransaction
- **Миграции:** Flyway, последняя версия была V14
- **Выявленная проблема:** Portfolio ResponseDto содержали nullable поля и не имели messageType

---

## План

1. Создать доменные модели User (UserRole, UserId, UserName, UserAge, User)
2. Создать транспортные модели (Commands, Queries, ResponseDto)
3. Реализовать маpперы (DomainToTransport, TransportToDomain)
4. Написать IUserRepo и UserRepoPostgres
5. Написать UserService с методами CRUD и SEARCH
6. Создать миграцию V15__create_users.sql
7. Выполнить рефакторинг Portfolio: убрать nullable, добавить messageType
8. Ввести value-class PortfolioName и PortfolioDescription

---

## Что реализовано

### Доменные модели User (5 файлов)
- `UserRole.kt` — enum Role (ADMIN, USER, MANAGER)
- `UserId.kt` — @JvmInline value class UserId
- `UserName.kt` — @JvmInline value class UserName
- `UserAge.kt` — @JvmInline value class UserAge
- `User.kt` — доменная сущность с id, name, age, role

### Репозиторий User (5 файлов)
- `IUserRepo.kt` — интерфейс репозитория
- `UserRepoResult.kt` — sealed class результатов (Single, Multiple, NotFound, DbError)
- `DbUserRequest.kt` — запрос для создания пользователя
- `DbUserIdRequest.kt` — запрос по ID
- `DbUserFilterRequest.kt` — запрос с поиском по name/role

### Транспортные модели User (13 файлов)
- `CreatableUserDto.kt` — DTO для создания
- `UpdatableUserDto.kt` — DTO для обновления
- `UserResponseDto.kt` — ответ с пользователем
- Команды (3 файла):
  - `CreateUserCommand.kt`
  - `UpdateUserCommand.kt`
  - `DeleteUserCommand.kt`
- Запросы (2 файла):
  - `GetUserByIdQuery.kt`
  - `SearchUsersQuery.kt`
- ResponseDto (5 файлов):
  - `CreateUserResponseDto.kt`
  - `UpdateUserResponseDto.kt`
  - `DeleteUserResponseDto.kt`
  - `GetUserByIdResponseDto.kt`
  - `SearchUsersResponseDto.kt`

### Маpперы (2 файла изменений)
- `DomainToTransportMapper.kt` — User → UserResponseDto
- `TransportToDomainMapper.kt` — CreatableUserDto → User

### PostgreSQL инфраструктура (2 файла)
- `UsersTable.kt` — таблица Exposed ORM
- `UserRepoPostgres.kt` — реализация IUserRepo с методами CRUD и поиска

### Сервис (1 файл)
- `UserService.kt` — бизнес-логика с создаванием, обновлением, удалением, поиском

### Миграция (1 файл)
- `V15__create_users.sql` — DDL для создания таблицы users с индексами по role и name

### Рефакторинг Portfolio — доменные модели (3 файла)
- `PortfolioName.kt` — новый @JvmInline value class вместо String
- `PortfolioDescription.kt` — новый @JvmInline value class вместо String
- Обновления в `Portfolio.kt` для использования новых типов

### Рефакторинг Portfolio — транспорт (7 файлов)
- `CreatePortfolioCommand.kt` — удаление nullable, добавление messageType
- `UpdatePortfolioCommand.kt` — удаление nullable, добавление messageType
- `CreatePortfolioResponseDto.kt` — добавление messageType
- `UpdatePortfolioResponseDto.kt` — добавление messageType
- `GetPortfolioByIdResponseDto.kt` — удаление nullable полей
- `ListPortfoliosResponseDto.kt` — удаление nullable полей
- `PortfolioMapper.kt` — обновления для маpпирования новых типов

---

## Результаты

- **Компиляция:** успешно
- **Покрытие тестами:** не запускались (out of scope для этапа реализации)
- **Валидация:** структура соответствует паттернам проекта
- **Интеграция:** все доменные, репозиторные и транспортные слои согласованы

---

## Статус

**Done**

Реализация завершена. User CRUD система полностью готова к использованию. Portfolio переведён на более строгую типизацию с messageType.
