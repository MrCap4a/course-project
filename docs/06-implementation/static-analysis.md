# Статический анализ кода

## Инструмент

**Checkstyle 10.21.4** — инструмент статического анализа Java-кода, проверяющий соответствие стандартам оформления и базовым правилам качества.

Интегрирован в Gradle-сборку через плагин `checkstyle`. Конфигурация: [`config/checkstyle/checkstyle.xml`](../../src/CalculatorBack/config/checkstyle/checkstyle.xml).

Запуск:
```
./gradlew checkstyleMain
```

HTML-отчёт генерируется в: `build/reports/checkstyle/main.html`

---

## Итог последнего запуска

| Параметр | Значение |
|---|---|
| Дата | 2026-06-01 |
| Версия Checkstyle | 10.21.4 |
| Статус сборки | ✅ BUILD SUCCESSFUL |
| Файлов проанализировано | 47 |
| Файлов с замечаниями | 26 |
| Всего замечаний | 69 |
| Уровень severity | WARNING (сборка не падает) |
| Errors | 0 |

---

## Правила анализа

| Правило | Описание |
|---|---|
| `FileTabCharacter` | Запрет символов табуляции — использовать пробелы |
| `LineLength` | Максимальная длина строки: 120 символов |
| `NewlineAtEndOfFile` | Файл должен заканчиваться переводом строки |
| `TypeName` | Классы: `UpperCamelCase` |
| `MethodName` | Методы: `lowerCamelCase` |
| `ConstantName` | Константы: `UPPER_SNAKE_CASE` |
| `AvoidStarImport` | Запрет wildcard-импортов (`import foo.*`) |
| `UnusedImports` | Запрет неиспользуемых импортов |
| `NeedBraces` | Обязательные фигурные скобки у `if`/`else`/`for`/`while` |
| `OneStatementPerLine` | Один оператор на строку |
| `ModifierOrder` | Порядок модификаторов по JLS §8.3.1 |
| `EmptyLineSeparator` | Пустая строка между методами в классе |
| `Regexp` | Запрет `System.out.print*` — использовать логгер |
| `CyclomaticComplexity` | Цикломатическая сложность ≤ 15 |
| `MethodLength` | Длина метода ≤ 80 строк |

---

## Замечания по категориям

### FileTabCharacter — 15 замечаний
**Файл:** `CalculatorApplication.java`

Все строки класса используют табуляцию вместо пробелов (настройка IDE по умолчанию). Затрагивает только один файл — входная точка приложения.

### NewlineAtEndOfFile — 8 замечаний
**Файлы:** `Calculation.java`, `CalculationItem.java`, `Formula.java`, `FormulaGroup.java`, `MaterialGroup.java`, `Permission.java`, `User.java`, `UserRole.java`

Entity-файлы не заканчиваются переводом строки. Не влияет на компиляцию, устраняется настройкой редактора (`*.editorconfig`).

### EmptyLineSeparator — 28 замечаний
**Файлы:** `ICalculationService.java`, `IFormulaGroupService.java`, `IFormulaService.java`, `IMaterialGroupService.java`, `IMaterialService.java`, `IUserRoleService.java`, `IUserService.java`

Все интерфейсы Mediator/Interfaces объявляют методы без пустой строки между ними. Стилистическое замечание, не затрагивает логику.

### AvoidStarImport — 6 замечаний
**Файлы:** `CalculationController.java`, `FormulaController.java`, `MaterialController.java`, `UserController.java`, `AuditLog.java`, `UserRole.java`

Wildcard-импорты `org.springframework.web.bind.annotation.*` и `jakarta.persistence.*`. Распространённая практика в Spring-контроллерах.

### NeedBraces — 9 замечаний
**Файлы:** `DataInitializer.java`, `FormulaEvaluator.java`, `UserServiceImpl.java`, `PermissionChecker.java`

Однострочные `if`/`else` без фигурных скобок. Потенциально ухудшает читаемость при дальнейших изменениях.

### Прочие — 3 замечания
- `LineLength` — 1: `SecurityConfig.java` (строка 39, 121 символ)
- `OneStatementPerLine` — 2: `FormulaEvaluator.java` (строки 93–94)
- `Regexp` (System.out) — 3: `CalculatorApplication.java` (отладочный вывод при инициализации)

---

## Приоритизация исправлений

| Приоритет | Правило | Причина |
|---|---|---|
| Высокий | `Regexp` (System.out) | Отладочный вывод попадает в production-логи — заменить на `log.info()` |
| Средний | `NeedBraces` | Снижает риск логических ошибок при изменении кода |
| Средний | `AvoidStarImport` | Ухудшает читаемость зависимостей, усложняет рефакторинг |
| Низкий | `FileTabCharacter` | Настройка `.editorconfig` решает проблему автоматически |
| Низкий | `NewlineAtEndOfFile` | Автоматически фиксируется в `.editorconfig` |
| Низкий | `EmptyLineSeparator` | Косметика, не влияет на поведение |

---

## Конфигурация сборки

В `build.gradle.kts`:

```kotlin
checkstyle {
    toolVersion = "10.21.4"
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = true  // предупреждения не роняют сборку
}
```

Флаг `isIgnoreFailures = true` установлен намеренно: все замечания являются стилистическими (WARNING), ни одного ERROR. Это позволяет отслеживать качество кода без блокировки CI.
