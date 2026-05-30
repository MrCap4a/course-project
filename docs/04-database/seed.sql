-- =============================================================================
-- SuperCalculator — первичное наполнение БД (seed)
-- Применяется после ddl.sql и triggers.sql
-- =============================================================================


-- -----------------------------------------------------------------------------
-- Права доступа (17 штук)
-- OVERRIDING SYSTEM VALUE: явные ID нужны для стабильных ссылок в role_permissions
-- -----------------------------------------------------------------------------
-- Материалы
INSERT INTO "permissions" ("id", "name") OVERRIDING SYSTEM VALUE VALUES
    (1,  'materials.view'),
    (2,  'materials.create'),
    (3,  'materials.edit'),
    (4,  'materials.delete'),
-- Формулы
    (5,  'formulas.view'),
    (6,  'formulas.create'),
    (7,  'formulas.edit'),
    (8,  'formulas.delete'),
-- Расчёты
    (9,  'calculations.view'),
    (10, 'calculations.create'),
    (11, 'calculations.edit'),
    (12, 'calculations.delete'),
-- Роли
    (13, 'roles.create'),
    (14, 'roles.edit'),
    (15, 'roles.delete'),
-- Пользователи
    (16, 'users.create'),
    (17, 'users.delete');

SELECT setval(pg_get_serial_sequence('"permissions"', 'id'), 17);


-- -----------------------------------------------------------------------------
-- Роль супер-администратора со всеми правами
-- -----------------------------------------------------------------------------
INSERT INTO "user_role" ("id", "name") OVERRIDING SYSTEM VALUE VALUES
    (1, 'Супер-администратор');

SELECT setval(pg_get_serial_sequence('"user_role"', 'id'), 1);

INSERT INTO "role_permissions" ("role_id", "permission_id")
SELECT 1, id FROM "permissions";


-- -----------------------------------------------------------------------------
-- Супер-администратор
-- Пароль: admin (bcrypt, $2a$12$... — замените перед деплоем)
-- -----------------------------------------------------------------------------
INSERT INTO "user" ("id", "role_id", "login", "password", "name", "surname", "is_super_admin")
OVERRIDING SYSTEM VALUE
VALUES (
    1,
    1,
    'admin',
    '$2a$12$zQh2I0n7f9MkQJMPiTZmOeWVRQBRqoH8Y2bXJAGw9qH1kEzCrX3Di',
    'Супер',
    'Администратор',
    TRUE
);

SELECT setval(pg_get_serial_sequence('"user"', 'id'), 1);
