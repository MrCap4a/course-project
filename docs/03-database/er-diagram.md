# ER-диаграмма

## Описание

ER-диаграмма описывает физическую схему базы данных системы **Calculator**. Схема состоит из 9 таблиц, объединённых в три логических блока: управление пользователями и правами, справочник материалов, формулы и расчёты.

## Диаграмма

![ER-диаграмма](images/er-diagram.png)

## Таблицы

| Таблица | Описание |
|---|---|
| `user` | Пользователи системы: логин, пароль, имя, фамилия, опциональная роль и флаг супер-администратора |
| `user_role` | Справочник ролей пользователей |
| `permissions` | Атомарные права доступа (разрешения) |
| `role_permissions` | Связующая таблица M:N между ролями и правами |
| `material` | Единица справочника материалов: название, цена, единица измерения, группа |
| `material_group` | Группы для логической организации материалов |
| `formula` | Математическая формула с текстовым выражением (`expression`) и группой |
| `formula_group` | Группы для логической организации формул |
| `calculation` | Расчёт на основе формулы; содержит набор позиций из материалов |
| `calculation_item` | Позиция расчёта: ссылка на материал, количество и порядковый номер |

## Связи

| Таблица A | Мощность | Таблица B | Каскад | Описание |
|---|---|---|---|---|
| `user_role` | 1 | 0..* `user` | SET NULL при удалении роли | Роль назначена нулю или более пользователям |
| `user_role` | 0..* | 0..* `permissions` | NO ACTION | Роль имеет набор прав через `role_permissions` |
| `material_group` | 1 | 0..* `material` | NO ACTION | Группа содержит ноль или более материалов |
| `formula_group` | 1 | 0..* `formula` | CASCADE | Группа содержит ноль или более формул; удаление группы → удаление формул |
| `formula` | 1 | 0..* `calculation` | CASCADE | Формула является основой расчётов; удаление формулы → удаление расчётов |
| `calculation` | 1 | 1..* `calculation_item` | CASCADE | Расчёт состоит из позиций; удаление расчёта → удаление позиций |
| `material` | 1 | 0..* `calculation_item` | NO ACTION | Материал используется в позициях; удаление не затрагивает позиции |

## PlantUML: ER-диаграмма

```plantuml
@startuml
!define TABLE(name,desc) class name as "desc" << (T,#AADDFF) >>
hide methods
hide stereotypes

TABLE(user_role, "user_role") {
    + id : INTEGER <<PK>>
    --
    name : VARCHAR(32) UNIQUE NOT NULL
}

TABLE(permissions, "permissions") {
    + id : INTEGER <<PK>>
    --
    name : VARCHAR(64) UNIQUE NOT NULL
}

TABLE(role_permissions, "role_permissions") {
    + role_id : INTEGER <<PK, FK>>
    + permission_id : INTEGER <<PK, FK>>
}

TABLE(user, "user") {
    + id : INTEGER <<PK>>
    --
    role_id : INTEGER <<FK>> NULL
    login : VARCHAR(32) UNIQUE NOT NULL
    password : VARCHAR(255) NOT NULL
    name : VARCHAR(32) NOT NULL
    surname : VARCHAR(32) NOT NULL
    is_super_admin : BOOLEAN NOT NULL DEFAULT FALSE
}

TABLE(material_group, "material_group") {
    + id : INTEGER <<PK>>
    --
    name : VARCHAR(64) UNIQUE NOT NULL
}

TABLE(material, "material") {
    + id : INTEGER <<PK>>
    --
    group_id : INTEGER <<FK>>
    name : VARCHAR(32) UNIQUE NOT NULL
    price : NUMERIC NOT NULL CHECK >= 0
    units : VARCHAR(8) NULL
}

TABLE(formula_group, "formula_group") {
    + id : INTEGER <<PK>>
    --
    name : VARCHAR(64) UNIQUE NOT NULL
}

TABLE(formula, "formula") {
    + id : INTEGER <<PK>>
    --
    group_id : INTEGER <<FK>>
    name : VARCHAR(128) UNIQUE NOT NULL
    expression : TEXT NOT NULL
}

TABLE(calculation, "calculation") {
    + id : INTEGER <<PK>>
    --
    formula_id : INTEGER <<FK>>
    name : VARCHAR(64) UNIQUE NOT NULL
}

TABLE(calculation_item, "calculation_item") {
    + id : INTEGER <<PK>>
    --
    calculation_id : INTEGER <<FK>>
    material_id : INTEGER <<FK>>
    position : SMALLINT NOT NULL
    quantity : NUMERIC NOT NULL CHECK >= 0
}

user_role "1" --> "0..*" user : role_id\nSET NULL
user_role "0..*" --> "0..*" role_permissions : role_id
permissions "0..*" --> "0..*" role_permissions : permission_id
material_group "1" --> "0..*" material : group_id\nNO ACTION
formula_group "1" --> "0..*" formula : group_id\nCASCADE
formula "1" --> "0..*" calculation : formula_id\nCASCADE
calculation "1" --> "1..*" calculation_item : calculation_id\nCASCADE
material "1" --> "0..*" calculation_item : material_id\nNO ACTION

@enduml
```

## Ограничения целостности

| Таблица | Поле | Ограничение | Описание |
|---|---|---|---|
| `material` | `price` | `CHECK (price >= 0)` | Цена материала не может быть отрицательной |
| `calculation_item` | `quantity` | `CHECK (quantity >= 0)` | Количество в позиции не может быть отрицательным |
| `user` | `login` | `UNIQUE NOT NULL` | Логин уникален в системе |
| `user` | `is_super_admin` | `TRIGGER BEFORE DELETE` | Запрещает удаление строки, если флаг = TRUE |
| `user` | `is_super_admin` | `TRIGGER BEFORE UPDATE` | Запрещает сброс флага (TRUE → FALSE) |
| `material` | `name` | `UNIQUE NOT NULL` | Название материала уникально |
| `formula` | `name` | `UNIQUE NOT NULL` | Название формулы уникально |
| `calculation` | `name` | `UNIQUE NOT NULL` | Название расчёта уникально |
| `role_permissions` | `(role_id, permission_id)` | `PRIMARY KEY` | Составной PK исключает дубли прав в роли |

## Индексы

| Индекс | Таблица | Поля | Назначение |
|---|---|---|---|
| `formula_index_0` | `formula` | `group_id` | Ускорение выборки формул по группе |
| `calculation_index_0` | `calculation` | `formula_id` | Ускорение выборки расчётов по формуле |
| `calculation_item_index_0` | `calculation_item` | `(calculation_id, material_id)` | Ускорение JOIN при загрузке позиций расчёта |
| `material_index_0` | `material` | `group_id` | Ускорение выборки материалов по группе |
| `user_index_0` | `user` | `role_id` | Ускорение выборки пользователей по роли |
