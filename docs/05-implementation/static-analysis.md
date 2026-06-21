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
| Дата | 2026-06-02 |
| Версия Checkstyle | 10.21.4 |
| Статус сборки | ✅ BUILD SUCCESSFUL |
| Файлов проанализировано | 47 |
| Файлов с замечаниями | 0 |
| Всего замечаний | 0 |
| Уровень severity | — |
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

## Исправленные замечания

До исправлений в коде было **69 замечаний** в 26 файлах (запуск 2026-06-01). После исправлений — **0 замечаний**.

| Правило | Кол-во | Что было сделано |
|---|---|---|
| `FileTabCharacter` | 15 | `CalculatorApplication.java` — заменены табуляции на пробелы |
| `NewlineAtEndOfFile` | 8 | Entity-файлы — добавлен перевод строки в конце |
| `EmptyLineSeparator` | 28 | Все интерфейсы `Mediator/Interfaces` — добавлены пустые строки между методами |
| `AvoidStarImport` | 6 | `CalculationController`, `UserController`, `AuditLog`, `UserRole` — wildcard-импорты раскрыты явно |
| `NeedBraces` | 9 | `DataInitializer`, `FormulaEvaluator`, `UserServiceImpl`, `PermissionChecker` — добавлены фигурные скобки |
| `LineLength` | 1 | `SecurityConfig` — длинная строка `.requestMatchers(...)` разбита на несколько строк |
| `OneStatementPerLine` | 2 | `FormulaEvaluator` — операторы разнесены по отдельным строкам |
| `Regexp` (System.out) | 3 | `CalculatorApplication` — `System.out.println` заменён на `LOG.info(...)` (SLF4J) |

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

Флаг `isIgnoreFailures = true` установлен намеренно: все замечания являлись стилистическими (WARNING), ни одного ERROR. После исправлений замечаний не осталось.
