# Reproduce: GET /portfolios/{id} → 500 Database error

Статус: **Воспроизведён**

## Входные данные
- Описание: GET /portfolios/{id} возвращает HTTP 500 с ошибкой `{"error": "Database error"}`
- Ошибка: `{"error": "Database error"}`
- HTTP статус: 500 Internal Server Error

## Найденный код

### Файлы, участвующие в проблеме:

1. **Routing** (`atlas-project-backend-ktor-app/src/main/kotlin/com/khan366kos/atlas/project/backend/ktor/app/routes/Portfolios.kt`, строки 92-97):
   ```kotlin
   get("/{id}") {
       val id = call.parameters["id"]!!
       val portfolio =
           portfolioService.find(PortfolioId(id))
       call.respond(ReadPortfolioResponseDto(portfolio.toResponsePortfolioDto()))
   }
   ```

2. **PortfolioService** (`atlas-project-backend-portfolio-service/src/main/kotlin/com/khan366kos/atlas/project/backend/portfolio/service/PortfolioService.kt`, строки 24-31):
   ```kotlin
   suspend fun find(id: PortfolioId): Portfolio {
       return when (val result = repo.readPortfolio(DbPortfolioIdRequest(id))) {
           is PortfolioRepoResult.Single -> result.portfolio
           is PortfolioRepoResult.NotFound -> throw PortfolioNotFoundException(id.asString())
           is PortfolioRepoResult.DbError -> throw PortfolioOperationFailedException(result.cause)
           else -> throw PortfolioOperationFailedException(RuntimeException("Unexpected result"))
       }
   }
   ```

3. **PortfolioRepoPostgres** (`atlas-project-backend-postgres/src/main/kotlin/com/khan366kos/atlas/project/backend/repo/postgres/PortfolioRepoPostgres.kt`, строки 53-72):
   ```kotlin
   override suspend fun readPortfolio(request: DbPortfolioIdRequest): PortfolioRepoResult =
       newSuspendedTransaction(db = database) {
           runCatching {
               PortfoliosTable.selectAll()
                   .where { PortfoliosTable.id eq request.id.asUUID() }
                   .singleOrNull()
                   ?.toPortfolio()
           }
               .onFailure { if (it is CancellationException) throw it }
               .fold(
                   onSuccess = { portfolio ->
                       portfolio
                           ?.let { PortfolioRepoResult.Multiple(listOf(it)) }  // ← ПРОБЛЕМА!
                           ?: PortfolioRepoResult.NotFound
                   },
                   onFailure = {
                       PortfolioRepoResult.DbError(it)
                   }
               )
       }
   ```

4. **StatusPages** (`atlas-project-backend-ktor-app/src/main/kotlin/StatusPages.kt`, строки 33-35):
   ```kotlin
   exception<PortfolioOperationFailedException> { call, cause ->
       call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Database error"))
   }
   ```

### Предполагаемая причина:

**Несовместимость типов результата между репозиторием и сервисом.**

- `readPortfolio()` в `PortfolioRepoPostgres` возвращает `PortfolioRepoResult.Multiple(listOf(portfolio))` когда портфолио найдено (строка 65)
- `find()` в `PortfolioService` ожидает `PortfolioRepoResult.Single` (строка 26)
- Когда результат приходит как `PortfolioRepoResult.Multiple`, он не совпадает ни с одним из обработанных случаев и падает в блок `else`
- Блок `else` бросает `PortfolioOperationFailedException(RuntimeException("Unexpected result"))`
- `StatusPages` перехватывает это исключение и отвечает с HTTP 500 и сообщением `{"error": "Database error"}`

## Шаги воспроизведения

1. Запустить сервер: `./gradlew run` (порт 8080)
2. Получить список портфолио: `curl http://localhost:8080/portfolios`
3. Выбрать ID из результата, например: `00000000-0000-0000-0000-000000000100`
4. Попытаться получить один портфолио: `curl http://localhost:8080/portfolios/00000000-0000-0000-0000-000000000100`
5. **Ожидаемый результат**: HTTP 200 с данными портфолио
6. **Фактический результат**: HTTP 500 с `{"error": "Database error"}`

## Логи

```
GET /portfolios → HTTP 200 ✓
GET /portfolios/00000000-0000-0000-0000-000000000100 → HTTP 500 ✗
```

Сервер успешно запускается и получает данные из БД (доказано тем, что GET /portfolios работает). Ошибка возникает только при попытке получить одно портфолио по ID.