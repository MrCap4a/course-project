# Интерфейсы

## Описание

Все зависимости между слоями PCMEF реализованы через явные интерфейсы-контракты, что обеспечивает слабую связанность компонентов. Конкретный слой зависит от абстракции (интерфейса), а не от реализации - это позволяет независимо подменять реализации и тестировать слои изолированно.

## Интерфейсы между слоями

| Интерфейс | Откуда → Куда | Контракты | Описание |
|---|---|---|---|
| IMaterialService | Control → Mediator | `create()`, `getById()`, `update()`, `delete()` | Контроллеры вызывают методы сервиса материалов, не зная их реализации |
| IUserService | Control → Mediator | `create()`, `getById()`, `update()`, `delete()` | Контроллеры вызывают методы сервиса пользователей, не зная их реализации |
| IMaterialRepository | Mediator → Foundation | `save()`, `findById()`, `delete()`, `findAll()` | Сервисы работают с сущностями через репозиторий, без прямого доступа к БД |
| IUserRepository | Mediator → Foundation | `save()`, `findById()`, `delete()`, `findAll()` | Сервисы работают с сущностями пользователей через репозиторий |
| REST API | Клиент → Сервер | `POST/GET/PUT/DELETE /api/v1/materials`, `/api/v1/users` | Запросы от Desktop и Web клиентов поверх HTTPS, данные в формате JSON |
| JDBC/JPA | Foundation → БД | SQL-запросы через Hibernate | Репозитории транслируют объектные операции в SQL-запросы к PostgreSQL |

## Спецификация интерфейсов

### 1. Control → Mediator

**Интерфейсы:** `IMaterialService`, `IUserService`

Контроллеры (`@RestController`) на сервере и Desktop-контроллеры на клиенте вызывают методы сервисов через интерфейс, не зная конкретной реализации (`MaterialService`, `UserService`). Это позволяет подменять реализацию (например, для тестирования) без изменения контроллеров.

### 2. Mediator → Foundation

**Интерфейсы:** `IMaterialRepository`, `IUserRepository`

Сервисы (`@Service`) взаимодействуют с базой данных исключительно через методы репозиториев. Spring Data JPA предоставляет реализацию по умолчанию; кастомные запросы добавляются через аннотацию `@Query`. Прямой доступ к БД из слоя Mediator запрещён.

### 3. Клиент → Сервер

**Протокол:** REST API поверх HTTPS, данные в формате JSON

Версионирование реализовано через `server.servlet.context-path: /api/v1` в `application.yaml` — все эндпоинты доступны с префиксом `/api/v1/` без изменения кода контроллеров.

| Метод | Эндпоинт | Описание |
|---|---|---|
| POST | `/api/v1/auth/login` | Аутентификация, получение access + refresh токенов |
| POST | `/api/v1/auth/refresh` | Обновление access-токена по refresh-токену |
| POST | `/api/v1/auth/logout` | Завершение сессии |
| GET | `/api/v1/users/me` | Получить текущего пользователя |
| GET | `/api/v1/users` | Получить список пользователей |
| POST | `/api/v1/users` | Создать пользователя |
| PUT | `/api/v1/users/{id}` | Обновить пользователя |
| DELETE | `/api/v1/users/{id}` | Удалить пользователя |
| GET | `/api/v1/roles` | Получить список ролей |
| POST | `/api/v1/roles` | Создать роль |
| PUT | `/api/v1/roles/{id}` | Обновить роль |
| DELETE | `/api/v1/roles/{id}` | Удалить роль |
| GET | `/api/v1/permissions` | Получить список разрешений |
| GET | `/api/v1/materials` | Получить список материалов (фильтр: `groupId`, `search`) |
| POST | `/api/v1/materials` | Создать материал |
| PUT | `/api/v1/materials/{id}` | Обновить материал |
| DELETE | `/api/v1/materials/{id}` | Удалить материал |
| GET | `/api/v1/material-groups` | Получить список групп материалов |
| POST | `/api/v1/material-groups` | Создать группу материалов |
| PUT | `/api/v1/material-groups/{id}` | Обновить группу материалов |
| DELETE | `/api/v1/material-groups/{id}` | Удалить группу (стратегия: `CASCADE`, `DEFAULT`, `MOVE`) |
| GET | `/api/v1/formulas` | Получить список формул |
| POST | `/api/v1/formulas` | Создать формулу |
| PUT | `/api/v1/formulas/{id}` | Обновить формулу |
| DELETE | `/api/v1/formulas/{id}` | Удалить формулу |
| GET | `/api/v1/formula-groups` | Получить список групп формул |
| POST | `/api/v1/formula-groups` | Создать группу формул |
| PUT | `/api/v1/formula-groups/{id}` | Обновить группу формул |
| DELETE | `/api/v1/formula-groups/{id}` | Удалить группу формул |
| GET | `/api/v1/calculations` | Получить список расчётов |
| GET | `/api/v1/calculations/{id}` | Получить расчёт по ID |
| POST | `/api/v1/calculations` | Создать расчёт на основе формулы |
| PUT | `/api/v1/calculations/{id}` | Обновить расчёт |
| DELETE | `/api/v1/calculations/{id}` | Удалить расчёт |

### 4. Foundation → БД

**Технология:** JDBC через Hibernate/JPA

Репозитории (`@Repository`) транслируют объектные операции (`save()`, `findById()`, `delete()`) в SQL-запросы к PostgreSQL. Маппинг объектов на таблицы описывается аннотациями `@Entity`, `@Table`, `@Column` на классах слоя Entity.

## Дополнительные слои

| Слой | Назначение | Расположение |
|---|---|---|
| DTO (Data Transfer Objects) | Сериализация данных для API — отделяет внутренние JPA-сущности от JSON-структур запросов и ответов. Классы: `MaterialRequest`, `MaterialResponse`, `UserRequest`, `UserResponse` | Сервер |
| Contracts (Интерфейсы) | Контракты для слабой связанности — `IMaterialService`, `IUserService`, `IMaterialRepository`, `IUserRepository`. Контроллер зависит от интерфейса, а не от реализации | Сервер |
| REST API | Единая точка входа для обоих типов клиентов (Desktop и Web). Обрабатывается слоем Control (`@RestController`) | Сервер |

## Принцип направленности зависимостей

Все зависимости направлены строго вниз по слоям PCMEF, циклические зависимости отсутствуют:

```
Presentation → Control → Mediator → Entity → Foundation → Database
```

Слой DTO находится между Control и Mediator и используется для передачи данных через REST API без утечки внутренних сущностей наружу.
