# Тесты

## Инструменты

| Инструмент | Назначение |
|---|---|
| JUnit 5 (Jupiter) | Фреймворк тестирования |
| Mockito | Моки и стабы зависимостей |
| Spring MockMvc (standalone) | Тестирование REST-контроллеров без поднятия сервера |
| AssertJ | Fluent-утверждения |
| JaCoCo | Отчёт о покрытии кода |
| Testcontainers | Реальная PostgreSQL 16.3 в Docker для интеграционных тестов |

Все зависимости поставляются через `spring-boot-starter-test` + `spring-security-test` + `spring-boot-starter-testcontainers`. JaCoCo сконфигурирован в `build.gradle.kts` и генерирует HTML-отчёт автоматически после каждого прогона тестов (`finalizedBy(tasks.jacocoTestReport)`).

## Результаты

**275 тестов, 0 failures.**

## Запуск

```bash
# из корня CalculatorBack
./gradlew test
```

- Результаты: `build/reports/tests/test/index.html`
- JaCoCo: `build/reports/jacoco/test/html/index.html`

> **Docker:** интеграционный тест (`PostgresSpringBootIntegrationTest`) требует работающего Docker. Если Docker недоступен, тест автоматически пропускается — остальные 272 теста выполняются в любом случае. На Windows запустите Docker Desktop перед прогоном тестов.

---

## Unit-тесты сервисов — 136 тестов

Расположены в `src/test/.../Mediator/`. Используют `@ExtendWith(MockitoExtension.class)` — все репозитории замоканы, Spring-контекст и база данных не задействованы.

### Тесты реализаций (`*ServiceImpl`) — 99 тестов

| Тест-класс | Покрываемый класс | Тестов |
|---|---|---|
| `FormulaEvaluatorTest` | `FormulaEvaluator` | 18 |
| `MaterialServiceImplTest` | `MaterialServiceImpl` | 15 |
| `MaterialGroupServiceImplTest` | `MaterialGroupServiceImpl` | 15 |
| `SqlServiceImplTest` | `SqlServiceImpl` | 15 |
| `UserServiceImplTest` | `UserServiceImpl` | 14 |
| `CalculationServiceImplTest` | `CalculationServiceImpl` | 14 |
| `FormulaServiceImplTest` | `FormulaServiceImpl` | 12 |
| `UserRoleServiceImplTest` | `UserRoleServiceImpl` | 11 |
| `FormulaGroupServiceImplTest` | `FormulaGroupServiceImpl` | 9 |

### Тесты паттернов Proxy (`*ServiceProxy`) — 45 тестов

| Тест-класс | Покрываемый класс | Тестов |
|---|---|---|
| `MaterialGroupServiceProxyTest` | `MaterialGroupServiceProxy` | 8 |
| `UserRoleServiceProxyTest` | `UserRoleServiceProxy` | 7 |
| `CalculationServiceProxyTest` | `CalculationServiceProxy` | 6 |
| `FormulaGroupServiceProxyTest` | `FormulaGroupServiceProxy` | 6 |
| `FormulaServiceProxyTest` | `FormulaServiceProxy` | 6 |
| `MaterialServiceProxyTest` | `MaterialServiceProxy` | 6 |
| `UserServiceProxyTest` | `UserServiceProxy` | 6 |

### Прочие unit-тесты — 8 тестов

| Тест-класс | Покрываемый класс | Тестов |
|---|---|---|
| `PermissionCheckerTest` | `PermissionChecker` | 8 |

---

## Тесты контроллеров — 82 теста

Расположены в `src/test/.../Control/`. Используют `MockMvcBuilders.standaloneSetup(controller)` — контроллер создаётся с замоканными сервисами, без полного Spring-контекста.

> `@WebMvcTest` удалён в Spring Boot 4.x, поэтому применяется standalone MockMvc. Spring Security фильтр-цепочка в standalone не активна — авторизация в этих тестах не проверяется. Тесты ошибочных сценариев используют `assertThatThrownBy(...).hasRootCauseMessage(...)`, так как standalone MockMvc пробрасывает `RuntimeException` как `ServletException`.

| Тест-класс | Тестов | Что проверяет |
|---|---|---|
| `MaterialControllerTest` | 16 | CRUD материалов и групп, фильтры `?groupId` и `?search` с пагинацией, стратегии удаления (CASCADE/MOVE) |
| `UserControllerTest` | 14 | CRUD пользователей и ролей, защита super admin, эндпоинт `/permissions` |
| `FormulaControllerTest` | 13 | CRUD формул и групп формул, пагинация |
| `CalculationControllerTest` | 11 | CRUD расчётов, парсинг вложенных `items` в теле запроса |
| `SqlControllerTest` | 8 | выполнение SELECT-запросов, получение схемы БД, проверка прав |
| `GlobalExceptionHandlerTest` | 6 | обработка глобальных исключений |
| `AuthControllerTest` | 4 | login (200 + токен), bad credentials, login без тела → 400, logout → 204 |

---

## Тесты конфигурации и фильтров — 24 теста

| Тест-класс | Тестов | Что проверяет |
|---|---|---|
| `AuditLoggingInterceptorTest` | 8 | логирование HTTP-запросов: сохранение в `audit_logs`, корректные поля |
| `DataInitializerTest` | 9 | инициализация данных при старте: права, роли, пользователь admin, группы по умолчанию |
| `RateLimitFilterTest` | 7 | ограничение частоты запросов (rate limiting) |

---

## Интеграционный тест — 3 теста

`PostgresSpringBootIntegrationTest` — единственный тест с реальным Spring-контекстом и реальной БД.

**Стек:** `@SpringBootTest(webEnvironment = RANDOM_PORT)` + Testcontainers (`postgres:16.3`). `@DynamicPropertySource` подставляет JDBC URL контейнера в конфигурацию. `@Transactional` — данные откатываются после каждого теста.

**Условный запуск:** кастомное расширение `DockerAvailableCondition` пропускает тесты если Docker недоступен — `./gradlew test` не падает на машинах без Docker.

| Тест | Что проверяет |
|---|---|
| `contextLoads` | Spring-контекст поднимается, репозитории инжектируются |
| `saveMaterialWithGroupAndQueryByName` | сохранение материала с группой; `findByGroup`, `findByNameContainingIgnoreCase` |
| `saveMultipleMaterialsAndSearchByGroupAndName` | `findByGroupAndNameContainingIgnoreCase` возвращает только совпадающие записи |

---

## Покрытие кода (JaCoCo)

Хорошо покрыты: `FormulaEvaluator` (все ветви), все `*ServiceImpl` (happy-path + error cases), все контроллеры (основные маршруты и коды ответов), все `*ServiceProxy`.

Не покрыты напрямую: `*ServiceDecorator`, `SecurityConfig`, `UserDetailsServiceImpl`, `JwtService` — требуют полного Spring-контекста с JWT-аутентификацией.
