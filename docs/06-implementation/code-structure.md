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
| `AuthController` | `POST /auth/login`, `POST /auth/logout` |
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

### Foundation — `ru.denis.Calculator.Foundation`

Spring Data JPA репозитории. Каждый расширяет `JpaRepository<Entity, Integer>`.

`UserRepository`, `UserRoleRepository`, `PermissionRepository`, `MaterialRepository`, `MaterialGroupRepository`, `FormulaRepository`, `FormulaGroupRepository`, `CalculationRepository`, `CalculationItemRepository`

Специфические методы (например, `findByCalculation(Calculation c)` в `CalculationItemRepository`) объявлены через query derivation — без SQL.

### Dto — `ru.denis.Calculator.Dto`

Java records, изолирующие API-контракт от JPA-модели.

- `*Dto` — исходящие ответы (например, `FormulaDto(id, name, expression, groupId, groupName)`)
- `Request/*Request` — входящие тела запросов (например, `FormulaRequest(name, expression, groupId)`)
- `TokenResponse` — ответ `/auth/login`

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
