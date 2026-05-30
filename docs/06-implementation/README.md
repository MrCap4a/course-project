# Этап 06: Реализация

## Обзор

На данном этапе реализован бэкенд приложения **Calculator** — конфигурируемого калькулятора для автоматизации ценообразования. Реализация выполнена на Java 25 + Spring Boot 4.0.6, строго следуя архитектуре PCMEF, спроектированной на этапе 03.

## Что реализовано

| Область | Статус | Описание |
|---|---|---|
| Слой Control (REST API) | ✅ | Контроллеры для всех сущностей: материалы, формулы, расчёты, пользователи, аутентификация |
| Слой Mediator (сервисы) | ✅ | CRUD-сервисы для всех сущностей с валидацией, паттернами Proxy и Decorator |
| Слой Entity (JPA) | ✅ | 9 JPA-сущностей: User, UserRole, Permission, Material, MaterialGroup, Formula, FormulaGroup, Calculation, CalculationItem |
| Слой Foundation (репозитории) | ✅ | Spring Data JPA репозитории для каждой сущности |
| Аутентификация | ✅ | JWT через Spring Security OAuth2 Resource Server |
| Вычислитель формул | ✅ | `FormulaEvaluator` — парсинг и вычисление выражений с подстановкой `{const}` и `{material}` |
| Тесты | ✅ | 12 тестовых классов, 157 тестов, 0 failures; покрытие 49% (JaCoCo) |

## Ключевые технические решения

**Стек:** Java 25, Spring Boot 4.0.6, Spring Data JPA, Spring Security + JWT (OAuth2 Resource Server), PostgreSQL, Gradle 9, JUnit 5 + Mockito, JaCoCo.

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
| [images/](images/) | Скриншоты отчёта JaCoCo и результатов запуска тестов |
