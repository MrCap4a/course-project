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
| REST API | Клиент → Сервер | `POST/GET/PUT/DELETE /api/materials`, `/api/users` | Запросы от Desktop и Web клиентов поверх HTTPS, данные в формате JSON |
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

| Метод | Эндпоинт | Описание |
|---|---|---|
| GET | `/api/materials` | Получить список всех материалов |
| GET | `/api/materials/{id}` | Получить материал по ID |
| POST | `/api/materials` | Создать новый материал |
| PUT | `/api/materials/{id}` | Обновить материал |
| DELETE | `/api/materials/{id}` | Удалить материал |
| GET | `/api/material-groups` | Получить список групп материалов |
| GET | `/api/material-groups/{id}` | Получить группу материалов по ID |
| POST | `/api/material-groups` | Создать группу материалов |
| PUT | `/api/material-groups/{id}` | Обновить группу материалов |
| DELETE | `/api/material-groups/{id}` | Удалить группу материалов |
| GET | `/api/formulas` | Получить список формул |
| GET | `/api/formulas/{id}` | Получить формулу по ID |
| POST | `/api/formulas` | Создать формулу |
| PUT | `/api/formulas/{id}` | Обновить формулу |
| DELETE | `/api/formulas/{id}` | Удалить формулу |
| GET | `/api/calculations` | Получить список расчётов |
| GET | `/api/calculations/{id}` | Получить расчёт по ID |
| POST | `/api/calculations` | Создать расчёт на основе формулы |
| POST | `/api/calculations/{id}/recalculate` | Пересчитать расчёт по актуальным ценам |
| DELETE | `/api/calculations/{id}` | Удалить расчёт |
| GET | `/api/users` | Получить список пользователей |
| POST | `/api/users` | Создать пользователя |
| PUT | `/api/users/{id}` | Обновить пользователя |
| DELETE | `/api/users/{id}` | Удалить пользователя |

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
