# Этап 06: Реализация

## Обзор

На данном этапе реализован бэкенд приложения **Calculator** — конфигурируемого калькулятора для автоматизации ценообразования. Реализация выполнена на Java 25 + Spring Boot 4.0.6, строго следуя архитектуре PCMEF, спроектированной на этапе 03.

## Что реализовано

| Область | Статус | Описание |
|---|---|---|
| Слой Control (REST API) | ✅ | Контроллеры для всех сущностей: материалы, формулы, расчёты, пользователи, аутентификация |
| Пагинация | ✅ | `GET /materials` и `GET /formulas` поддерживают `?page`, `?size`, `?sort`; ответ — `Page<Dto>` |
| Слой Mediator (сервисы) | ✅ | CRUD-сервисы для всех сущностей с валидацией, паттернами Proxy и Decorator |
| Слой Entity (JPA) | ✅ | 10 JPA-сущностей: User, UserRole, Permission, Material, MaterialGroup, Formula, FormulaGroup, Calculation, CalculationItem, AuditLog |
| Слой Foundation (репозитории) | ✅ | Spring Data JPA репозитории для каждой сущности |
| Аутентификация | ✅ | JWT (access + refresh) через Spring Security OAuth2 Resource Server |
| OpenAPI / Swagger UI | ✅ | springdoc-openapi 2.8.8; доступен на `/api/v1/swagger-ui/index.html` |
| CORS | ✅ | Настроен в `SecurityConfig`; разрешены `localhost:5173`, `localhost:4173`, `localhost` (http + https) |
| HTTPS / TLS | ✅ | Nginx генерирует самоподписанный сертификат при сборке; HTTP (80) → редирект на HTTPS (443) |
| Rate limiting | ✅ | `RateLimitFilter` — token bucket 60 req/min per IP, возвращает HTTP 429 при превышении |
| Audit logging | ✅ | `AuditLoggingInterceptor` пишет в таблицу `audit_logs`: пользователь, действие, endpoint, IP, время, статус |
| Статический анализ | ✅ | Checkstyle 10.21.4: 69 предупреждений, 0 ошибок, BUILD SUCCESSFUL |
| Вычислитель формул | ✅ | `FormulaEvaluator` — парсинг и вычисление выражений с подстановкой `{const}` и `{material}` |
| Тесты | ✅ | 12 тестовых классов, 157 тестов, 0 failures; покрытие 49% (JaCoCo) |
| CI/CD | ✅ | GitHub Actions pipeline: тесты → JAR → Docker-образ (ghcr.io) + компиляция десктопа |

## Ключевые технические решения

**Стек:** Java 25, Spring Boot 4.0.6, Spring Data JPA, Spring Security + JWT (OAuth2 Resource Server), springdoc-openapi 2.8.8, PostgreSQL, Gradle 9, JUnit 5 + Mockito, JaCoCo.

**Версионирование API** реализовано через `server.servlet.context-path: /api/v1` — все эндпоинты доступны с префиксом `/api/v1/` без изменения кода контроллеров.

**CORS** настроен в `SecurityConfig.corsConfigurationSource()` — разрешены запросы с `localhost:5173` и `localhost:4173` (dev-сервер React).

**Паттерн Proxy** реализован для каждого сервиса (`MaterialServiceProxy`, `UserServiceProxy` и др.) — централизованная проверка прав доступа через `PermissionChecker` без дублирования в основных сервисах.

**Паттерн Decorator** — обёртки (`MaterialServiceDecorator`, `UserServiceDecorator` и др.) добавляют сквозную логику (логирование, аудит) поверх основных `*ServiceImpl` без изменения их кода.

**Стратегия удаления групп** — `DeleteGroupStrategy` инкапсулирует поведение при удалении группы (материалов или формул), не засоряя сервисный слой условиями.

**FormulaEvaluator** разбирает строку выражения, извлекает плейсхолдеры `{const}` и `{material}`, подставляет значения из `CalculationItem` и вычисляет результат. Возвращает `Optional<BigDecimal>` — пустой при любой ошибке подстановки или вычисления.

**DTO-слой** полностью изолирует внутренние JPA-сущности от API-контрактов: все контроллеры принимают `*Request` и возвращают `*Dto` (Java records).

## Артефакты

| Файл | Описание |
|---|---|
| [code-structure.md](code-structure.md) | Структура пакетов и маппинг на слои PCMEF |
| [tests.md](tests.md) | Описание тестов, охват, инструкция по запуску |
| [static-analysis.md](static-analysis.md) | Отчёт статического анализа кода |
| [cicd.md](cicd.md) | Описание CI/CD pipeline (GitHub Actions) |
| [images/](images/) | Скриншоты отчёта JaCoCo, результатов тестов и статанализа |

## Статический анализ

Результаты анализа описаны в [static-analysis.md](static-analysis.md).

Checkstyle 10.21.4 проверил 47 файлов: **69 предупреждений, 0 ошибок**. Основные категории: отступы табами в `CalculatorApplication.java` (15), отсутствие пустой строки между методами интерфейсов (28), wildcard-импорты в контроллерах (6). HTML-отчёт: `build/reports/checkstyle/main.html`.
