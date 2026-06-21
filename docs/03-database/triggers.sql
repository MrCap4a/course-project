-- =============================================================================
-- Calculator — триггеры
-- СУБД: PostgreSQL
-- Применяется поверх ddl.sql
-- =============================================================================


-- -----------------------------------------------------------------------------
-- Защита супер-администратора
-- -----------------------------------------------------------------------------

-- Запрет удаления пользователя с флагом is_super_admin = TRUE
CREATE OR REPLACE FUNCTION prevent_super_admin_delete()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.is_super_admin THEN
        RAISE EXCEPTION 'Нельзя удалить супер-администратора';
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prevent_super_admin_delete
BEFORE DELETE ON "user"
FOR EACH ROW EXECUTE FUNCTION prevent_super_admin_delete();

-- Запрет сброса флага is_super_admin (TRUE → FALSE)
CREATE OR REPLACE FUNCTION prevent_super_admin_flag_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.is_super_admin AND NOT NEW.is_super_admin THEN
        RAISE EXCEPTION 'Нельзя снять флаг супер-администратора';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prevent_super_admin_flag_change
BEFORE UPDATE ON "user"
FOR EACH ROW EXECUTE FUNCTION prevent_super_admin_flag_change();
