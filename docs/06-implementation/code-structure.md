# Структура кода

## Расположение исходников

Весь исходный код бэкенда находится в отдельном репозитории `CalculatorBack`:

```
CalculatorBack/
├── build.gradle.kts               # Сборка: Spring Boot 4.0.6, Java 25, JaCoCo
├── settings.gradle.kts
└── src/
    ├── main/java/ru/denis/Calculator/
    │   ├── CalculatorApplication.java
    │   ├── Config/
    │   │   └── SecurityConfig.java
    │   ├── Filter/                # Servlet-фильтры (rate limiting)
    │   │   └── RateLimitFilter.java
    │   ├── Aspect/                # AOP-аспекты (audit logging)
    │   │   └── AuditLoggingAspect.java
    │   ├── Control/               # PCMEF: Control
    │   ├── Mediator/              # PCMEF: Mediator
    │   ├── Entity/                # PCMEF: Entity
    │   ├── Foundation/            # PCMEF: Foundation
    │   └── Dto/                   # Вспомогательный слой DTO
    └── test/java/ru/denis/Calculator/
        ├── Control/               # Тесты контроллеров
        └── Mediator/              # Тесты сервисов и FormulaEvaluator
```

## Маппинг на PCMEF

### Control — `ru.denis.Calculator.Control`

REST-контроллеры и инфраструктура безопасности. Принимают HTTP-запросы, делегируют бизнес-логику в Mediator через интерфейсы, возвращают DTO.

| Класс | Назначение |
|---|---|
| `AuthController` | `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout` |
| `MaterialController` | CRUD материалов и групп материалов |
| `FormulaController` | CRUD формул и групп формул |
| `CalculationController` | CRUD расчётов |
| `UserController` | Управление пользователями и ролями |
| `JwtService` | Генерация и валидация JWT-токенов |
| `UserDetailsServiceImpl` | Загрузка пользователя для Spring Security |
| `SecurityConfig` | Конфигурация фильтров, белый список эндпоинтов |

Контроллеры зависят **только** от интерфейсов Mediator (`IMaterialService`, `IFormulaService` и т.д.) — прямая зависимость от `*ServiceImpl` отсутствует.

### Mediator — `ru.denis.Calculator.Mediator`

Бизнес-логика. Разделён на четыре подпакета.

**`Interfaces/`** — контракты сервисного слоя:
`IUserService`, `IMaterialService`, `IMaterialGroupService`, `IFormulaService`, `IFormulaGroupService`, `ICalculationService`, `IUserRoleService`

**`Impl/`** — основные реализации:

| Класс | Ответственность |
|---|---|
| `UserServiceImpl` | Регистрация, CRUD пользователей |
| `UserRoleServiceImpl` | CRUD ролей и назначение разрешений |
| `MaterialServiceImpl` | CRUD материалов |
| `MaterialGroupServiceImpl` | CRUD групп материалов |
| `FormulaServiceImpl` | CRUD формул |
| `FormulaGroupServiceImpl` | CRUD групп формул |
| `CalculationServiceImpl` | Создание расчёта: сохранение `CalculationItem`, вызов `FormulaEvaluator` |

**`Proxy/`** — паттерн Proxy (проверка прав):
`UserServiceProxy`, `UserRoleServiceProxy`, `MaterialServiceProxy`, `MaterialGroupServiceProxy`, `FormulaServiceProxy`, `FormulaGroupServiceProxy`, `CalculationServiceProxy`

Каждый прокси реализует соответствующий интерфейс, инжектирует `PermissionChecker` и делегирует вызов в `*ServiceImpl` только при наличии прав.

**`Decorator/`** — паттерн Decorator (сквозные функции):
`UserServiceDecorator`, `UserRoleServiceDecorator`, `MaterialServiceDecorator`, `MaterialGroupServiceDecorator`, `FormulaServiceDecorator`, `FormulaGroupServiceDecorator`, `CalculationServiceDecorator`

**Вспомогательные классы:**

| Класс | Назначение |
|---|---|
| `FormulaEvaluator` | Парсинг выражений, извлечение плейсхолдеров `{const}`/`{material}`, вычисление `BigDecimal` результата |
| `PermissionChecker` | Проверка наличия разрешения у текущего пользователя |
| `DeleteGroupStrategy` | Стратегия обработки дочерних элементов при удалении группы |

### Entity — `ru.denis.Calculator.Entity`

JPA-сущности, отображаемые на таблицы PostgreSQL. Используют Lombok для генерации геттеров/сеттеров.

| Сущность | Таблица | Связи |
|---|---|---|
| `User` | `users` | ManyToOne → `UserRole` |
| `UserRole` | `user_roles` | ManyToMany → `Permission` |
| `Permission` | `permissions` | — |
| `Material` | `materials` | ManyToOne → `MaterialGroup` |
| `MaterialGroup` | `material_groups` | OneToMany → `Material` |
| `Formula` | `formulas` | ManyToOne → `FormulaGroup` |
| `FormulaGroup` | `formula_groups` | OneToMany → `Formula` |
| `Calculation` | `calculations` | ManyToOne → `Formula`; OneToMany → `CalculationItem` |
| `CalculationItem` | `calculation_items` | ManyToOne → `Calculation`, `Material` |
| `AuditLog` | `audit_logs` | — (независимая запись: username, action, endpoint, method, ip, timestamp, status) |

### Foundation — `ru.denis.Calculator.Foundation`

Spring Data JPA репозитории. Каждый расширяет `JpaRepository<Entity, Integer>`.

`UserRepository`, `UserRoleRepository`, `PermissionRepository`, `MaterialRepository`, `MaterialGroupRepository`, `FormulaRepository`, `FormulaGroupRepository`, `CalculationRepository`, `CalculationItemRepository`, `AuditLogRepository`

Специфические методы (например, `findByCalculation(Calculation c)` в `CalculationItemRepository`) объявлены через query derivation — без SQL.

### Dto — `ru.denis.Calculator.Dto`

Java records, изолирующие API-контракт от JPA-модели.

- `*Dto` — исходящие ответы (например, `FormulaDto(id, name, expression, groupId, groupName)`)
- `Request/*Request` — входящие тела запросов (например, `FormulaRequest(name, expression, groupId)`)
- `TokenResponse` — ответ `/auth/login`

## Архитектурные решения по безопасности

### Почему CSRF отключён

CSRF-атака эксплуатирует то, что браузер **автоматически** прикладывает куки сессии к любому запросу. В данном проекте аутентификация реализована через **JWT Bearer-токены**, которые клиент передаёт вручную в заголовке `Authorization`. Браузер никогда не добавляет произвольные заголовки при межсайтовом запросе — у стороннего сайта нет доступа к токену. Механизм CSRF в таком случае не добавляет защиты, поэтому он намеренно отключён (`.csrf(csrf -> csrf.disable())`). Это стандартная рекомендация Spring Security для stateless REST API.

### Почему роли динамические, а не захардкожены

Проект использует **RBAC с динамическими ролями** вместо фиксированных `ROLE_USER`, `ROLE_ADMIN`, `ROLE_MANAGER`. Роли и атомарные права хранятся в БД (таблицы `user_roles`, `permissions`, `role_permissions`) и управляются через REST API без изменения кода. Захардкоженные константы — упрощение, уместное в небольших системах; данная архитектура намеренно выбрана ради гибкости: администратор может создать любой набор ролей и прав в runtime.

## Цепочка зависимостей

```
HTTP-запрос
    → Controller (Control)
        → *ServiceProxy (Mediator/Proxy)
            → *ServiceDecorator (Mediator/Decorator)
                → *ServiceImpl (Mediator/Impl)
                    → Repository (Foundation)
                    → Entity (Entity)
```

Все переходы между слоями осуществляются через интерфейсы, что обеспечивает тестируемость каждого слоя в изоляции.

## Пагинация

Эндпоинты `GET /materials` и `GET /formulas` поддерживают постраничную выдачу через стандартный механизм Spring Data `Pageable`.

### Параметры запроса

| Параметр | По умолчанию | Описание |
|---|---|---|
| `page` | `0` | Номер страницы (нумерация с нуля) |
| `size` | `20` | Количество элементов на странице |
| `sort` | `name,asc` | Поле и направление сортировки |

Примеры:
```
GET /api/v1/materials?page=0&size=10&sort=name,asc
GET /api/v1/materials?groupId=2&search=цемент&page=1&size=5
GET /api/v1/formulas?groupId=3&page=0&size=20&sort=name,desc
```

### Структура ответа

Ответ оборачивается в стандартный `Page<T>` объект Spring Data:

```json
{
  "content": [ ... ],
  "totalElements": 150,
  "totalPages": 8,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

### Реализация

Пагинация проходит через все слои PCMEF без дублирования логики:

- **Controller** — принимает `@ParameterObject @PageableDefault(size=20, sort="name") Pageable pageable`; возвращает `Page<Dto>`
- **Proxy** — прокидывает `Pageable` в делегат без изменений (после проверки прав)
- **Decorator** — логирует номер страницы, размер, `totalElements`
- **Impl** — вызывает pageable-перегрузки репозитория (`findAll(pageable)`, `findByGroup(group, pageable)` и т.д.)
- **Repository** — Spring Data генерирует SQL с `LIMIT`/`OFFSET` автоматически по сигнатуре метода

Swagger UI (`/api/v1/swagger-ui/index.html`) отображает параметры `page`, `size`, `sort` как отдельные поля благодаря аннотации `@ParameterObject`.
