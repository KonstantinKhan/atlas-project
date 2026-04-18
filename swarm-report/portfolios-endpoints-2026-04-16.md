# Исследование эндпоинтов портфеля проектов

**Дата**: 2026-04-16  
**Контекст**: Анализ API для работы с портфелями проектов в системе PPM

## Список исследованных модулей и файлов

### Маршруты (Routes)
- `backend/atlas-project-backend/atlas-project-backend-ktor-app/src/main/kotlin/com/khan366kos/atlas/project/backend/ktor/app/routes/Portfolios.kt`

### DTO и команды (Transport)
- `backend/atlas-project-backend/atlas-project-backend-transport/src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/commands/CreatePortfolioCommandDto.kt`
- `backend/atlas-project-backend/atlas-project-backend-transport/src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/commands/UpdatePortfolioCommandDto.kt`
- `backend/atlas-project-backend/atlas-project-backend-transport/src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/commands/ReadPortfolioCommandDto.kt`
- `backend/atlas-project-backend/atlas-project-backend-transport/src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/commands/DeletePortfolioCommandDto.kt`
- `backend/atlas-project-backend/atlas-project-backend-transport/src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/portfolio/CreatablePortfolioDto.kt`
- `backend/atlas-project-backend/atlas-project-backend-transport/src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/portfolio/UpdatablePortfolioDto.kt`
- `backend/atlas-project-backend/atlas-project-backend-transport/src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/portfolio/PortfolioProjectDto.kt`
- `backend/atlas-project-backend/atlas-project-backend-transport/src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/portfolio/AddProjectToPortfolioRequest.kt`
- `backend/atlas-project-backend/atlas-project-backend-transport/src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/portfolio/UpdateProjectPriorityRequest.kt`
- `backend/atlas-project-backend/atlas-project-backend-transport/src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/portfolio/ReorderPortfolioProjectsRequest.kt`

### Ответы (Responses)
- `backend/atlas-project-backend/atlas-project-backend-transport/src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/responses/CreatePortfolioResponseDto.kt`
- `backend/atlas-project-backend/atlas-project-backend-transport/src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/responses/ReadPortfolioResponseDto.kt`
- `backend/atlas-project-backend/atlas-project-backend-transport/src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/responses/UpdatePortfolioResponseDto.kt`
- `backend/atlas-project-backend/atlas-project-backend-transport/src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/responses/DeletePortfolioResponseDto.kt`
- `backend/atlas-project-backend/atlas-project-backend-transport/src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/responses/SearchPortfolioResponseDto.kt`
- `backend/atlas-project-backend/atlas-project-backend-transport/src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/responses/PortfolioResponseDto.kt`

### Сервисы и репозитории
- `backend/atlas-project-backend/atlas-project-backend-common/src/commonMain/kotlin/com/khan366kos/atlas/project/backend/common/repo/portfolio/IPortfolioRepo.kt`
- `backend/atlas-project-backend/atlas-project-backend-ktor-app/src/main/kotlin/com/khan366kos/atlas/project/backend/ktor/app/routes/Portfolios.kt`

## Обобщённый ответ для пользователя

В системе PPM реализованы 10 эндпоинтов для работы с портфелями проектов:

**Основные CRUD-операции для портфелей:**
1. `GET /portfolios` — получение списка всех портфелей
2. `POST /portfolios` — создание нового портфеля
3. `GET /portfolios/{id}` — получение информации о конкретном портфеле
4. `PATCH /portfolios/{id}` — обновление портфеля
5. `DELETE /portfolios/{id}` — удаление портфеля

**Управление проектами в портфеле:**
6. `POST /portfolios/{id}/projects` — добавление проекта в портфель с указанием приоритета
7. `DELETE /portfolios/{portfolioId}/projects/{projectId}` — удаление проекта из портфеля
8. `PATCH /portfolios/{portfolioId}/projects/{projectId}/priority` — изменение приоритета проекта в портфеле
9. `PATCH /portfolios/{id}/projects/reorder` — переупорядочивание проектов в портфеле
10. `GET /portfolios/{id}/resource-load` — расчёт нагрузки ресурсов на все проекты портфеля за указанный период

Все эндпоинты используют CQRS-паттерн с отдельными DTO для команд (создание, обновление, удаление) и запросов (чтение, поиск), а также сериализуются через Kotlinx Serialization с аннотациями `@Serializable` и `@SerialName`.

## Статус: Done
