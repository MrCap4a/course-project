# API-документация

## Swagger UI

Интерактивная документация доступна при запущенном бэкенде:

```
http://localhost:8080/api/v1/swagger-ui/index.html
```

OpenAPI-спецификация (JSON/YAML):
```
http://localhost:8080/api/v1/v3/api-docs
http://localhost:8080/api/v1/v3/api-docs.yaml
```

## Postman-коллекция

Файл: [postman-collection.json](postman-collection.json)

Импортировать: Postman → Import → выбрать файл.

---

## Базовый URL

```
http://localhost:8080/api/v1
```

Все эндпоинты требуют заголовок `Authorization: Bearer <token>`, кроме публичных.

---

## Аутентификация

| Метод | Эндпоинт | Описание | Доступ |
|---|---|---|---|
| POST | `/auth/login` | Вход: возвращает access + refresh токены | Публичный |
| POST | `/auth/refresh` | Обновить access-токен по refresh-токену | Публичный |
| POST | `/auth/logout` | Выход | Авторизованный |
| GET | `/auth/me` | Текущий пользователь | Авторизованный |

---

## Материалы

| Метод | Эндпоинт | Описание | Роль |
|---|---|---|---|
| GET | `/materials` | Список (пагинация, `?groupId`, `?search`) | USER+ |
| GET | `/materials/{id}` | Один материал | USER+ |
| POST | `/materials` | Создать | ADMIN |
| PUT | `/materials/{id}` | Изменить | ADMIN |
| DELETE | `/materials/{id}` | Удалить (`?strategy=CASCADE\|MOVE`) | ADMIN |
| GET | `/material-groups` | Список групп | USER+ |
| POST | `/material-groups` | Создать группу | ADMIN |
| PUT | `/material-groups/{id}` | Изменить группу | ADMIN |
| DELETE | `/material-groups/{id}` | Удалить группу | ADMIN |

---

## Формулы

| Метод | Эндпоинт | Описание | Роль |
|---|---|---|---|
| GET | `/formulas` | Список (пагинация, `?groupId`) | USER+ |
| GET | `/formulas/{id}` | Одна формула | USER+ |
| POST | `/formulas` | Создать | ADMIN |
| PUT | `/formulas/{id}` | Изменить | ADMIN |
| DELETE | `/formulas/{id}` | Удалить | ADMIN |
| GET | `/formula-groups` | Список групп | USER+ |
| POST | `/formula-groups` | Создать группу | ADMIN |
| PUT | `/formula-groups/{id}` | Изменить группу | ADMIN |
| DELETE | `/formula-groups/{id}` | Удалить группу | ADMIN |

---

## Расчёты

| Метод | Эндпоинт | Описание | Роль |
|---|---|---|---|
| GET | `/calculations` | Список расчётов | USER+ |
| GET | `/calculations/{id}` | Один расчёт с позициями | USER+ |
| POST | `/calculations` | Создать расчёт | USER+ |
| PUT | `/calculations/{id}` | Изменить расчёт | USER+ |
| DELETE | `/calculations/{id}` | Удалить расчёт | USER+ |

---

## Пользователи и роли

| Метод | Эндпоинт | Описание | Роль |
|---|---|---|---|
| GET | `/users` | Список пользователей | ADMIN |
| POST | `/users` | Создать пользователя | ADMIN |
| PUT | `/users/{id}` | Изменить пользователя | ADMIN |
| DELETE | `/users/{id}` | Удалить пользователя | ADMIN |
| GET | `/roles` | Список ролей | ADMIN |
| POST | `/roles` | Создать роль | ADMIN |
| PUT | `/roles/{id}` | Изменить роль | ADMIN |
| DELETE | `/roles/{id}` | Удалить роль | ADMIN |
| GET | `/permissions` | Список прав доступа | ADMIN |

---

## SQL-консоль (только ADMIN)

| Метод | Эндпоинт | Описание |
|---|---|---|
| POST | `/sql/query` | Выполнить SELECT-запрос |
| GET | `/sql/schema` | Получить схему БД |

---

## Коды ответов

| Код | Значение |
|---|---|
| 200 | OK |
| 201 | Created |
| 204 | No Content (DELETE) |
| 400 | Bad Request (ошибка валидации) |
| 401 | Unauthorized (нет/устаревший токен) |
| 403 | Forbidden (недостаточно прав) |
| 404 | Not Found |
| 429 | Too Many Requests (rate limit: 60 req/min per IP) |
| 500 | Internal Server Error |
