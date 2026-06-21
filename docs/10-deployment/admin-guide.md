# Руководство администратора
## Calculator — развёртывание и сборка клиентов

---

## Системные требования

| Компонент | Требование |
|---|---|
| ОС | Windows 10/11, Linux (Ubuntu 20.04+), macOS 12+ |
| Docker | 24.0+ |
| Docker Compose | 2.20+ |
| Java (для сборки десктопа) | JDK 25 |
| Gradle (для сборки) | Поставляется вместе с проектом (`gradlew`) |
| 7-Zip (для portable exe) | Опционально, только для `packageSfx` |

---

## 1. Развёртывание через Docker (сервер + веб-клиент)

### 1.1. Подготовка SQL-скриптов

`docker-compose.yml` находится в `src/` и при старте монтирует SQL-файлы из той же папки. Нужны только два файла:

```bash
cp docs/04-database/ddl.sql      src/ddl.sql
cp docs/04-database/triggers.sql src/triggers.sql
```

**Почему не нужен seed.sql:** бэкенд содержит `DataInitializer`, который при каждом старте автоматически создаёт права доступа, роль суперадмина, учётную запись `admin` и дефолтные группы («Без группы»). Начальные данные не нужно прописывать вручную.

**Почему нужен ddl.sql:** PostgreSQL выполняет init-скрипты при первом старте — раньше, чем поднимается бэкенд. Если таблиц ещё нет, `triggers.sql` упадёт с ошибкой. `ddl.sql` создаёт схему заранее, чтобы триггеры было куда применять.

### 1.2. Настройка переменных окружения

Создайте файл `src/.env` (или задайте переменные в окружении):

```env
# Секрет для подписи JWT-токенов — минимум 32 символа
JWT_SECRET=замените-на-случайную-строку-минимум-32-символа

# Время жизни access-токена в секундах (по умолчанию 86400 = 24 часа)
JWT_EXPIRATION=86400

# Время жизни refresh-токена в секундах (по умолчанию 604800 = 7 дней)
JWT_REFRESH_EXPIRATION=604800
```

> **Важно:** замените `JWT_SECRET` перед развёртыванием в продакшене. Значение по умолчанию (`change-me-to-a-secret-string-32chars-min`) небезопасно.

### 1.3. Запуск

```bash
cd src
docker compose up --build -d
```

После запуска:
- Веб-клиент: `https://localhost` (HTTP на `:80` автоматически редиректит на HTTPS)
- API бэкенда: `http://localhost:8080/api/v1` (внутренний, без TLS)
- Swagger UI: `http://localhost:8080/api/v1/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/api/v1/v3/api-docs`
- PostgreSQL: `localhost:5432`, БД `calculator`, пользователь `calculator`

### 1.4. HTTPS и TLS-сертификат

Nginx-контейнер генерирует **самоподписанный сертификат** автоматически при сборке образа (`docker compose up --build`). Никаких дополнительных действий не требуется.

**При первом открытии браузер покажет предупреждение** — это ожидаемо для самоподписанного сертификата:
- Chrome/Edge: нажмите **«Дополнительно» → «Перейти на сайт»**
- Firefox: нажмите **«Принять риск и продолжить»**

Трафик между браузером и сервером шифруется. Связь Nginx ↔ Spring Boot происходит внутри Docker-сети (без TLS, что безопасно в изолированной сети).

> **Для продакшена** замените самоподписанный сертификат на выданный удостоверяющим центром (например, Let's Encrypt). Для этого смонтируйте готовые PEM-файлы через `volumes` в `docker-compose.yml` и укажите путь в `nginx.conf`.

### 1.6. Первый вход

После первого запуска в БД будет создан суперадмин из `seed.sql`:

| Поле | Значение |
|---|---|
| Логин | `admin` |
| Пароль | `admin` |

> **Смените пароль суперадмина после первого входа.** Хеш в `seed.sql` соответствует паролю `admin` (bcrypt cost 10).

### 1.7. Остановка и данные

```bash
# Остановить контейнеры (данные сохранятся в томе pgdata)
docker compose down

# Остановить и удалить все данные (полный сброс)
docker compose down -v
```

### 1.8. Просмотр логов

```bash
docker compose logs backend   # логи бэкенда
docker compose logs postgres  # логи БД
docker compose logs -f        # все логи в реальном времени
```

---

## 2. Сборка и запуск бэкенда вручную (без Docker)

```bash
cd src/CalculatorBack

# Сборка исполняемого JAR
./gradlew bootJar

# Запуск (требует запущенного PostgreSQL)
java -jar build/libs/Calculator-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/calculator \
  --spring.datasource.username=calculator \
  --spring.datasource.password=calculator \
  --security.jwt.secret=ваш-секрет-минимум-32-символа
```

На Windows замените `./gradlew` на `.\gradlew.bat`.

---

## 3. Сборка десктоп-клиента (JavaFX)

Десктоп-клиент собирается в самодостаточный исполняемый файл с встроенным JRE — на целевой машине **не требуется** установленная Java.

Все команды выполняются из папки `src/CalculatorDesktop`.

### 3.1. App-image (папка с exe, рекомендуется)

Создаёт папку `build/package/Calculator/` с `Calculator.exe` и всеми зависимостями. Не требует дополнительных инструментов.

```bat
cd src\CalculatorDesktop
.\gradlew.bat packageApp
```

Результат: `build\package\Calculator\Calculator.exe`

Запуск: двойной клик на `Calculator.exe` или из командной строки.

### 3.2. Portable single-file exe (через 7-Zip SFX)

Создаёт один файл `Calculator-portable.exe` (~120 МБ), который при запуске самораспаковывается и запускает приложение. Удобен для передачи пользователям.

**Предварительные требования:**
1. Установите [7-Zip](https://www.7-zip.org/) (в `C:\Program Files\7-Zip\`)
2. Скачайте [7-Zip Extra](https://www.7-zip.org/download.html) и скопируйте `7zSD.sfx` в `C:\Program Files\7-Zip\7zSD.sfx`

```bat
cd src\CalculatorDesktop
.\gradlew.bat packageSfx
```

Результат: `build\package\Calculator-portable.exe`

### 3.3. Установщик Windows (.exe или .msi)

Создаёт установщик с мастером установки. Требует [WiX Toolset 3](https://github.com/wixtoolset/wix3/releases) в PATH.

```bat
# .exe установщик
.\gradlew.bat packageApp -Ptype=exe

# .msi пакет
.\gradlew.bat packageApp -Ptype=msi
```

Результат: `build\package\Calculator-1.0.0.exe` / `build\package\Calculator-1.0.0.msi`

### 3.4. Настройка адреса API

По умолчанию десктоп-клиент обращается к бэкенду на `http://localhost:8080`. Если бэкенд развёрнут на другом хосте, перед сборкой укажите адрес в файле конфигурации клиента (properties-файл или конфиг-класс в `src/CalculatorDesktop/src/main/resources/`).

---

## 4. Управление пользователями

Первоначально в системе создаётся один суперадмин (`admin`). Для создания дополнительных пользователей войдите как суперадмин и перейдите в раздел **Пользователи**.

### Порядок начального развёртывания

1. Войдите под `admin` / `admin`
2. Создайте роли с нужными наборами прав
3. Создайте пользователей, назначьте роли
4. (Рекомендуется) Смените пароль суперадмина через прямое обновление в БД (UI не предоставляет смену пароля суперадмина):

```sql
UPDATE "user"
SET "password" = '$2a$10$<новый_bcrypt_хеш>'
WHERE "is_super_admin" = TRUE;
```

Сгенерировать BCrypt-хеш (cost 10):
```java
System.out.println(new BCryptPasswordEncoder(10).encode("новый_пароль"));
```

---

## 5. Резервное копирование данных

```bash
# Создать дамп БД
docker exec <postgres_container_name> \
  pg_dump -U calculator calculator > backup_$(date +%Y%m%d).sql

# Восстановить из дампа
docker exec -i <postgres_container_name> \
  psql -U calculator calculator < backup_20260601.sql
```

Имя контейнера postgres: `src-postgres-1` (по умолчанию при запуске из папки `src`).
