# CI/CD Pipeline

## Расположение

Файл: `.github/workflows/ci.yml` в корне репозитория.

## Триггеры

| Событие | Ветки |
|---|---|
| `push` | `master` |
| `pull_request` | `master` |

## Структура pipeline

Pipeline состоит из четырёх job'ов:

```
push / pull_request
       │
       ├─► backend-test ──────► backend-package (JAR артефакт)
       │        │
       │        └──────────────► docker-build   (образ → ghcr.io на master)
       │
       └─► desktop-build  (параллельно)
```

## Описание job'ов

### 1. `backend-test` — компиляция и тесты бэкенда

**Среда:** `ubuntu-latest` (Docker доступен из коробки — нужен для Testcontainers)

**Шаги:**
1. Checkout репозитория
2. Установка Java 25 Temurin + кэш Gradle
3. `./gradlew compileJava compileTestJava` — компиляция
4. `./gradlew test` — запуск 157 тестов (JUnit 5 + Testcontainers с реальным PostgreSQL)
5. `./gradlew jacocoTestReport` — генерация HTML-отчёта покрытия

**Артефакты** (доступны во вкладке Actions → выбранный запуск):
- `coverage-report` — HTML-отчёт JaCoCo (`build/reports/jacoco/test/html/`)
- `test-results` — XML-результаты тестов (`build/test-results/`)

### 2. `backend-package` — сборка исполняемого JAR

**Зависимость:** запускается только после успешного `backend-test`

**Шаги:**
1. `./gradlew bootJar -x test` — сборка fat JAR (Spring Boot executable)

**Артефакт:** `backend-jar` — файл `*.jar` из `build/libs/` (хранится 7 дней)

### 3. `docker-build` — сборка Docker-образа

**Зависимость:** запускается только после успешного `backend-test`

**Шаги:**
1. Логин в GitHub Container Registry (`ghcr.io`) — только на push в `master`
2. `docker/build-push-action` — сборка образа из `src/CalculatorBack/Dockerfile`
   - На `pull_request`: только сборка (без push), проверяет работоспособность Dockerfile
   - На `push master`: push образа с тегами `latest` и SHA коммита

**Теги образа:**
```
ghcr.io/<owner>/supercalculator-backend:latest
ghcr.io/<owner>/supercalculator-backend:<commit-sha>
```

**Права:** использует автоматический `GITHUB_TOKEN` — отдельные секреты не нужны.

### 4. `desktop-build` — компиляция десктоп-клиента

**Среда:** `ubuntu-latest` (параллельно с `backend-test`)

**Шаги:**
1. Установка Java 25 Temurin + кэш Gradle
2. `./gradlew compileJava` — компиляция JavaFX-клиента

## Инструменты

| Инструмент | Версия | Назначение |
|---|---|---|
| `actions/checkout` | v4 | Клонирование репозитория |
| `actions/setup-java` | v4 | Java 25 Temurin + кэш Gradle |
| `actions/upload-artifact` | v4 | Сохранение артефактов сборки |
| `docker/login-action` | v3 | Авторизация в ghcr.io |
| `docker/build-push-action` | v5 | Сборка и публикация Docker-образа |

## Локальная проверка перед push

```bash
# Бэкенд: тесты
cd src/CalculatorBack
./gradlew test

# Бэкенд: сборка JAR
./gradlew bootJar -x test

# Десктоп: компиляция
cd ../CalculatorDesktop
./gradlew compileJava
```
