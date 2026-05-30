-- =============================================================================
-- SuperCalculator — DDL-скрипт схемы базы данных
-- СУБД: PostgreSQL
-- =============================================================================


-- -----------------------------------------------------------------------------
-- Блок 1: Управление пользователями и правами доступа
-- -----------------------------------------------------------------------------

-- Справочник ролей пользователей
CREATE TABLE IF NOT EXISTS "user_role" (
    "id"   INTEGER GENERATED ALWAYS AS IDENTITY NOT NULL,
    "name" VARCHAR(32)  NOT NULL UNIQUE,
    PRIMARY KEY ("id")
);

-- Атомарные права доступа
CREATE TABLE IF NOT EXISTS "permissions" (
    "id"   INTEGER GENERATED ALWAYS AS IDENTITY NOT NULL,
    "name" VARCHAR(64)  NOT NULL UNIQUE,
    PRIMARY KEY ("id")
);

-- Связующая таблица M:N: роль ↔ права
CREATE TABLE IF NOT EXISTS "role_permissions" (
    "role_id"       INTEGER NOT NULL,
    "permission_id" INTEGER NOT NULL,
    PRIMARY KEY ("role_id", "permission_id")
);

-- Пользователи системы (role_id опционален — пользователь может быть без роли)
-- is_super_admin: флаг супер-администратора; защищён триггерами от удаления и сброса
CREATE TABLE IF NOT EXISTS "user" (
    "id"             INTEGER GENERATED ALWAYS AS IDENTITY NOT NULL,
    "role_id"        INTEGER,
    "login"          VARCHAR(32)   NOT NULL UNIQUE,
    "password"       VARCHAR(255)  NOT NULL,
    "name"           VARCHAR(32)   NOT NULL,
    "surname"        VARCHAR(32)   NOT NULL,
    "is_super_admin" BOOLEAN       NOT NULL DEFAULT FALSE,
    PRIMARY KEY ("id")
);

CREATE INDEX "user_index_0"
    ON "user" ("role_id");


-- -----------------------------------------------------------------------------
-- Блок 2: Справочник материалов
-- -----------------------------------------------------------------------------

-- Группы материалов
CREATE TABLE IF NOT EXISTS "material_group" (
    "id"   INTEGER GENERATED ALWAYS AS IDENTITY NOT NULL,
    "name" VARCHAR(64)  NOT NULL UNIQUE,
    PRIMARY KEY ("id")
);

-- Единицы справочника материалов (units опционален)
CREATE TABLE IF NOT EXISTS "material" (
    "id"       INTEGER GENERATED ALWAYS AS IDENTITY NOT NULL,
    "group_id" INTEGER      NOT NULL,
    "name"     VARCHAR(32)  NOT NULL UNIQUE,
    "price"    NUMERIC      NOT NULL CHECK ("price" >= 0),
    "units"    VARCHAR(8),
    PRIMARY KEY ("id")
);

CREATE INDEX "material_index_0"
    ON "material" ("group_id");


-- -----------------------------------------------------------------------------
-- Блок 3: Формулы и расчёты
-- -----------------------------------------------------------------------------

-- Группы формул
CREATE TABLE IF NOT EXISTS "formula_group" (
    "id"   INTEGER GENERATED ALWAYS AS IDENTITY NOT NULL,
    "name" VARCHAR(64)  NOT NULL UNIQUE,
    PRIMARY KEY ("id")
);

-- Формулы с математическим выражением
CREATE TABLE IF NOT EXISTS "formula" (
    "id"         INTEGER GENERATED ALWAYS AS IDENTITY NOT NULL,
    "group_id"   INTEGER       NOT NULL,
    "name"       VARCHAR(128)  NOT NULL UNIQUE,
    "expression" TEXT          NOT NULL,
    PRIMARY KEY ("id")
);

CREATE INDEX "formula_index_0"
    ON "formula" ("group_id");

-- Расчёты на основе формул
CREATE TABLE IF NOT EXISTS "calculation" (
    "id"         INTEGER GENERATED ALWAYS AS IDENTITY NOT NULL,
    "formula_id" INTEGER      NOT NULL,
    "name"       VARCHAR(64)  NOT NULL UNIQUE,
    PRIMARY KEY ("id")
);

CREATE INDEX "calculation_index_0"
    ON "calculation" ("formula_id");

-- Позиции расчёта: материал + количество + порядковый номер
-- material_id NULL: материал может быть удалён — расчёт становится невычислимым
CREATE TABLE IF NOT EXISTS "calculation_item" (
    "id"             INTEGER GENERATED ALWAYS AS IDENTITY NOT NULL,
    "position"       SMALLINT  NOT NULL,
    "calculation_id" INTEGER   NOT NULL,
    "material_id"    INTEGER,
    "quantity"       NUMERIC   NOT NULL CHECK ("quantity" >= 0),
    PRIMARY KEY ("id")
);

CREATE INDEX "calculation_item_index_0"
    ON "calculation_item" ("calculation_id", "material_id");


-- =============================================================================
-- Внешние ключи
-- =============================================================================

-- Пользователь → Роль (SET NULL: при удалении роли пользователь сохраняется)
ALTER TABLE "user"
    ADD CONSTRAINT "fk_user_role_id_user_role"
    FOREIGN KEY ("role_id") REFERENCES "user_role" ("id")
    ON UPDATE CASCADE ON DELETE SET NULL;

-- role_permissions → user_role
ALTER TABLE "role_permissions"
    ADD CONSTRAINT "fk_role_permissions_role_id_user_role"
    FOREIGN KEY ("role_id") REFERENCES "user_role" ("id")
    ON UPDATE NO ACTION ON DELETE NO ACTION;

-- role_permissions → permissions
ALTER TABLE "role_permissions"
    ADD CONSTRAINT "fk_role_permissions_permission_id_permissions"
    FOREIGN KEY ("permission_id") REFERENCES "permissions" ("id")
    ON UPDATE NO ACTION ON DELETE NO ACTION;

-- Материал → Группа материалов
ALTER TABLE "material"
    ADD CONSTRAINT "fk_material_group_id_material_group"
    FOREIGN KEY ("group_id") REFERENCES "material_group" ("id")
    ON UPDATE NO ACTION ON DELETE NO ACTION;

-- Формула → Группа формул (CASCADE: удаление группы → удаление формул)
ALTER TABLE "formula"
    ADD CONSTRAINT "fk_formula_group_id_formula_group"
    FOREIGN KEY ("group_id") REFERENCES "formula_group" ("id")
    ON UPDATE CASCADE ON DELETE CASCADE;

-- Расчёт → Формула (CASCADE: удаление формулы → удаление расчётов)
ALTER TABLE "calculation"
    ADD CONSTRAINT "fk_calculation_formula_id_formula"
    FOREIGN KEY ("formula_id") REFERENCES "formula" ("id")
    ON UPDATE CASCADE ON DELETE CASCADE;

-- Позиция расчёта → Расчёт (CASCADE: удаление расчёта → удаление позиций)
ALTER TABLE "calculation_item"
    ADD CONSTRAINT "fk_calculation_item_calculation_id_calculation"
    FOREIGN KEY ("calculation_id") REFERENCES "calculation" ("id")
    ON UPDATE CASCADE ON DELETE CASCADE;

-- Позиция расчёта → Материал (SET NULL: удаление материала обнуляет ссылку)
ALTER TABLE "calculation_item"
    ADD CONSTRAINT "fk_calculation_item_material_id_material"
    FOREIGN KEY ("material_id") REFERENCES "material" ("id")
    ON UPDATE NO ACTION ON DELETE SET NULL;
