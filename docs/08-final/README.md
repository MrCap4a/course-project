# Этап 08: Завершение

## Обзор

Финальный этап курсового проекта **Calculator**. Содержит полный комплект эксплуатационной и проектной документации.

## Артефакты

| Файл | Описание |
|---|---|
| [technical-specification.md](technical-specification.md) | Техническое задание на разработку системы |
| [user-guide.md](user-guide.md) | Руководство пользователя |
| [admin-guide.md](admin-guide.md) | Руководство администратора (развёртывание, сборка клиентов) |
| [wbs.md](wbs.md) | WBS — иерархическая структура работ |
| [gantt.md](gantt.md) | Диаграмма Ганта (Mermaid) |
| [cocomo.md](cocomo.md) | Оценка трудозатрат по модели COCOMO |

## Итоги проекта

| Критерий | Результат |
|---|---|
| Траектория | Г — Enterprise (полный стек) |
| Архитектура | PCMEF (Presentation / Control / Mediator / Entity / Foundation) |
| Клиенты | Веб (React + TypeScript) + Десктоп (JavaFX) |
| Бэкенд | Java 25 + Spring Boot 4.0.6 + Spring Security (JWT) |
| База данных | PostgreSQL 16 |
| Тесты | 157 тестов, покрытие 49% (JaCoCo) |
| Развёртывание | Docker + docker-compose (сервер + БД + веб-клиент) |
| CI/CD | GitHub Actions: тесты → JAR → Docker-образ (ghcr.io) + компиляция десктопа |
| REST API | 20+ эндпоинтов, `/api/v1/...` |
| Безопасность | JWT, ролевая модель (17 прав по 5 областям), защита суперадмина на уровне БД, HTTPS (TLS 1.2/1.3) |
