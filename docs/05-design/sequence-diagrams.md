# Sequence Diagrams

Диаграммы последовательности для ключевых сценариев использования проекта **SuperCalculator** — конфигурируемого калькулятора для автоматизации ценообразования. Архитектура соответствует слоям PCMEF: Presentation → Control → Mediator → Entity → Foundation → Database.

---

## UC-1: Добавить материал

Пользователь создаёт новый материал, заполняя данные о нём. После этого материал можно использовать для расчёта стоимости товара.

![images/sequence-uc1-add-material.png](images/sequence-uc1-add-material.png)

```plantuml
@startuml
title Диаграмма последовательности: Добавить материал

actor "Пользователь" as User
boundary "MaterialForm\n[Presentation]" as UI
control "MaterialController\n[Control]" as Ctrl
participant "IMaterialService\n[Mediator]" as Service
entity "Material\n[Entity]" as MatEnt
database "IMaterialRepository\n[Foundation]" as Repo
database "PostgreSQL\n[Database]" as DB

User -> UI: Открывает форму добавления материала
activate UI
UI -> Ctrl: openAddMaterialForm()
activate Ctrl
Ctrl --> UI: showForm()
deactivate Ctrl

User -> UI: Заполняет поля (name, price, units, group)\nи нажимает "Сохранить"
UI -> Ctrl: createMaterial(name, price, units, groupId)
activate Ctrl
Ctrl -> Service: checkPermission(userId)
activate Service
Service --> Ctrl: hasPermission
deactivate Service

alt Валидация не пройдена / нет прав / имя занято
    Ctrl -> Ctrl: validateInput(name, price, units)
    Ctrl -> Service: existsByName(name)
    activate Service
    Service -> Repo: existsByName(name)
    activate Repo
    Repo -> DB: SELECT * FROM material WHERE name = ?
    activate DB
    DB --> Repo: result
    deactivate DB
    Repo --> Service: boolean
    deactivate Repo
    Service --> Ctrl: boolean
    deactivate Service
    Ctrl --> UI: showError(reason)
    deactivate Ctrl
    UI --> User: Отобразить ошибку

else Всё в порядке
    Ctrl -> Service: createMaterial(name, price, units, groupId)
    activate Ctrl
    activate Service
    Service -> MatEnt: new Material(name, price, units, groupId)
    activate MatEnt
    MatEnt --> Service: material
    deactivate MatEnt
    Service -> Repo: save(material)
    activate Repo
    Repo -> DB: INSERT INTO material (name, price, units, group_id) VALUES (...)
    activate DB
    DB --> Repo: savedMaterial
    deactivate DB
    Repo --> Service: savedMaterial
    deactivate Repo
    Service --> Ctrl: savedMaterial
    deactivate Service
    deactivate Ctrl
    Ctrl --> UI: showSuccess(savedMaterial)
    deactivate Ctrl
    UI --> User: Отобразить подтверждение\n"Материал успешно добавлен"
    deactivate UI
end
@enduml
```

---

## UC-2: Создать нового пользователя

Пользователь с соответствующими правами доступа может регистрировать новых пользователей в системе.

![images/sequence-uc2-create-user.png](images/sequence-uc2-create-user.png)

```plantuml
@startuml
title Диаграмма последовательности: Создание нового пользователя

actor "Администратор" as Admin
boundary "UserManagementDialog\n[Presentation]" as UI
control "UserController\n[Control]" as Ctrl
participant "IUserService\n[Mediator]" as Service
entity "User\n[Entity]" as UserEnt
database "IUserRepository\n[Foundation]" as Repo
database "PostgreSQL\n[Database]" as DB

Admin -> UI: Открывает форму создания пользователя
activate UI
UI -> Ctrl: openCreateUserForm()
activate Ctrl
Ctrl -> Service: getAvailableRoles()
activate Service
Service -> Repo: findAllRoles()
activate Repo
Repo -> DB: SELECT * FROM user_role
activate DB
DB --> Repo: roles
deactivate DB
Repo --> Service: List<Role>
deactivate Repo
Service --> Ctrl: List<Role>
deactivate Service
Ctrl --> UI: showFormWithRoles(roles)
deactivate Ctrl

Admin -> UI: Заполняет поля (login, password, name, surname, role)\nи нажимает "Создать"
UI -> Ctrl: createUser(login, password, name, surname, roleId)
activate Ctrl
Ctrl -> Service: checkPermission(userId)
activate Service
Service --> Ctrl: hasPermission
deactivate Service

alt Валидация не пройдена / нет прав / логин занят
    Ctrl -> Ctrl: validateInput(login, password, name, surname)
    Ctrl -> Service: existsByLogin(login)
    activate Service
    Service -> Repo: existsByLogin(login)
    activate Repo
    Repo -> DB: SELECT * FROM user WHERE login = ?
    activate DB
    DB --> Repo: result
    deactivate DB
    Repo --> Service: boolean
    deactivate Repo
    Service --> Ctrl: boolean
    deactivate Service
    Ctrl --> UI: showError(reason)
    deactivate Ctrl
    UI --> Admin: Отобразить ошибку

else Всё в порядке
    Ctrl -> Service: createUser(login, password, name, surname, roleId)
    activate Ctrl
    activate Service
    Service -> Service: hashPassword(password)
    Service -> UserEnt: new User(login, hashedPassword, name, surname, roleId)
    activate UserEnt
    UserEnt --> Service: user
    deactivate UserEnt
    Service -> Repo: save(user)
    activate Repo
    Repo -> DB: INSERT INTO user (login, password, name, surname, role_id) VALUES (...)
    activate DB
    DB --> Repo: savedUser
    deactivate DB
    Repo --> Service: savedUser
    deactivate Repo
    Service --> Ctrl: savedUser
    deactivate Service
    deactivate Ctrl
    Ctrl --> UI: showSuccess(savedUser)
    deactivate Ctrl
    UI --> Admin: Отобразить подтверждение\n"Пользователь успешно создан"
    deactivate UI
end
@enduml
```

---

## UC-3: Создать новую роль

Пользователь с соответствующими правами доступа может создавать новые роли, чтобы назначать на них других пользователей.

![images/sequence-uc3-create-role.png](images/sequence-uc3-create-role.png)

```plantuml
@startuml
title Диаграмма последовательности: Создание новой роли

actor "Администратор" as Admin
boundary "RoleManagementDialog\n[Presentation]" as UI
control "UserController\n[Control]" as Ctrl
participant "IUserService\n[Mediator]" as Service
entity "Role\n[Entity]" as RoleEnt
database "IUserRepository\n[Foundation]" as Repo
database "PostgreSQL\n[Database]" as DB

Admin -> UI: Открывает форму создания роли
activate UI
UI -> Ctrl: openCreateRoleForm()
activate Ctrl
Ctrl -> Service: getAvailablePermissions()
activate Service
Service -> Repo: findAllPermissions()
activate Repo
Repo -> DB: SELECT * FROM permissions
activate DB
DB --> Repo: permissions
deactivate DB
Repo --> Service: List<Permission>
deactivate Repo
Service --> Ctrl: List<Permission>
deactivate Service
Ctrl --> UI: showFormWithPermissions(permissions)
deactivate Ctrl

Admin -> UI: Вводит название роли,\nвыбирает разрешения\nи нажимает "Создать"
UI -> Ctrl: createRole(name, selectedPermissionIds)
activate Ctrl
Ctrl -> Service: checkPermission(userId)
activate Service
Service --> Ctrl: hasPermission
deactivate Service

alt Валидация не пройдена / нет прав / имя занято
    Ctrl -> Ctrl: validateInput(name, selectedPermissionIds)
    Ctrl -> Service: existsByName(name)
    activate Service
    Service -> Repo: existsByName(name)
    activate Repo
    Repo -> DB: SELECT * FROM user_role WHERE name = ?
    activate DB
    DB --> Repo: result
    deactivate DB
    Repo --> Service: boolean
    deactivate Repo
    Service --> Ctrl: boolean
    deactivate Service
    Ctrl --> UI: showError(reason)
    deactivate Ctrl
    UI --> Admin: Отобразить ошибку

else Всё в порядке
    Ctrl -> Service: createRole(name, selectedPermissionIds)
    activate Ctrl
    activate Service
    Service -> RoleEnt: new Role(name)
    activate RoleEnt
    RoleEnt --> Service: role
    deactivate RoleEnt
    Service -> Repo: save(role)
    activate Repo
    Repo -> DB: INSERT INTO user_role (name) VALUES (?)
    activate DB
    DB --> Repo: savedRole
    deactivate DB
    Repo --> Service: savedRole
    deactivate Repo

    loop Для каждого permissionId из selectedPermissionIds
        Service -> Repo: linkPermissionToRole(savedRole.id, permissionId)
        activate Repo
        Repo -> DB: INSERT INTO role_permissions (role_id, permission_id) VALUES (...)
        activate DB
        DB --> Repo: ok
        deactivate DB
        Repo --> Service: ok
        deactivate Repo
    end

    Service --> Ctrl: savedRole
    deactivate Service
    deactivate Ctrl
    Ctrl --> UI: showSuccess(savedRole)
    deactivate Ctrl
    UI --> Admin: Отобразить подтверждение\n"Роль успешно создана"
    deactivate UI
end
@enduml
```
