# Method Specifications

Спецификации методов, выявленных при построении диаграмм последовательности для проекта **Calculator**. Каждый вызов на диаграмме последовательности соответствует методу с совпадающей сигнатурой в диаграмме классов.

---

## Сводная таблица методов

| Класс | Метод | Параметры | Возвращаемое значение |
|-------|-------|-----------|----------------------|
| `MaterialController` | `openAddMaterialForm()` | — | `void` |
| `MaterialController` | `createMaterial()` | `name: String, price: Double, units: String, groupId: Long` | `void` |
| `MaterialController` | `validateInput()` | `name: String, price: Double, units: String` | `boolean` |
| `UserController` | `openCreateUserForm()` | — | `void` |
| `UserController` | `createUser()` | `login: String, password: String, name: String, surname: String, roleId: Long` | `void` |
| `UserController` | `validateInput()` | `login: String, password: String, name: String, surname: String` | `boolean` |
| `UserController` | `openCreateRoleForm()` | — | `void` |
| `UserController` | `createRole()` | `name: String, selectedPermissionIds: List<Long>` | `void` |
| `UserController` | `validateInput()` | `name: String, selectedPermissionIds: List<Long>` | `boolean` |
| `IUserService` | `checkPermission()` | `userId: Long` | `boolean` |
| `IUserService` | `getAvailableRoles()` | — | `List<Role>` |
| `IUserService` | `getAvailablePermissions()` | — | `List<Permission>` |
| `IUserService` | `existsByLogin()` | `login: String` | `boolean` |
| `IUserService` | `createUser()` | `login: String, hashedPassword: String, name: String, surname: String, roleId: Long` | `User` |
| `IUserService` | `hashPassword()` | `password: String` | `String` |
| `IUserService` | `existsByName()` | `name: String` | `boolean` |
| `IUserService` | `createRole()` | `name: String, selectedPermissionIds: List<Long>` | `Role` |
| `IMaterialService` | `checkPermission()` | `userId: Long` | `boolean` |
| `IMaterialService` | `existsByName()` | `name: String` | `boolean` |
| `IMaterialService` | `createMaterial()` | `name: String, price: Double, units: String, groupId: Long` | `Material` |
| `IUserRepository` | `findAllRoles()` | — | `List<Role>` |
| `IUserRepository` | `findAllPermissions()` | — | `List<Permission>` |
| `IUserRepository` | `existsByLogin()` | `login: String` | `boolean` |
| `IUserRepository` | `save()` | `user: User` | `User` |
| `IUserRepository` | `existsByName()` | `name: String` | `boolean` |
| `IUserRepository` | `save()` | `role: Role` | `Role` |
| `IUserRepository` | `linkPermissionToRole()` | `roleId: Long, permissionId: Long` | `void` |
| `IMaterialRepository` | `existsByName()` | `name: String` | `boolean` |
| `IMaterialRepository` | `save()` | `material: Material` | `Material` |

---

## Детальные спецификации ключевых методов

### 1. `IMaterialService.createMaterial()`

```java
/**
 * Создаёт новый материал и сохраняет его в базе данных.
 *
 * @param name    название материала
 * @param price   цена материала за единицу
 * @param units   единица измерения материала
 * @param groupId идентификатор группы материалов
 * @return созданный и сохранённый материал
 * @throws AccessDeniedException          если у пользователя нет прав на создание материала
 * @throws DuplicateMaterialNameException если материал с таким именем уже существует
 */
Material createMaterial(String name, Double price, String units, Long groupId);
```

---

### 2. `IMaterialRepository.save()`

```java
/**
 * Сохраняет материал в базе данных.
 *
 * @param material объект материала для сохранения
 * @return сохранённый материал с присвоенным идентификатором
 * @throws DataAccessException если произошла ошибка при обращении к базе данных
 */
Material save(Material material);
```

---

### 3. `IUserService.createUser()`

```java
/**
 * Создаёт нового пользователя в системе.
 *
 * @param login          логин нового пользователя
 * @param hashedPassword хэшированный пароль пользователя
 * @param name           имя пользователя
 * @param surname        фамилия пользователя
 * @param roleId         идентификатор роли, назначаемой пользователю
 * @return созданный и сохранённый пользователь
 * @throws AccessDeniedException   если у пользователя нет прав на создание новых пользователей
 * @throws DuplicateLoginException если пользователь с таким логином уже существует
 */
User createUser(String login, String hashedPassword, String name, String surname, Long roleId);
```

---

### 4. `IUserService.createRole()`

```java
/**
 * Создаёт новую роль и привязывает к ней набор разрешений.
 *
 * @param name                 название новой роли
 * @param selectedPermissionIds список идентификаторов разрешений, назначаемых роли
 * @return созданная и сохранённая роль с привязанными разрешениями
 * @throws AccessDeniedException      если у пользователя нет прав на создание ролей
 * @throws DuplicateRoleNameException если роль с таким именем уже существует
 */
UserRole createRole(String name, List<Long> selectedPermissionIds);
```

---

### 5. `IUserService.checkPermission()`

```java
/**
 * Проверяет наличие у пользователя прав на выполнение операции.
 *
 * @param userId идентификатор пользователя
 * @return true если пользователь имеет необходимые права, false в противном случае
 * @throws UserNotFoundException если пользователь с указанным идентификатором не найден
 */
boolean checkPermission(Long userId);
```

---

### 6. `Calculation.Builder.build()`

```java
/**
 * Собирает и возвращает готовый объект расчёта на основе
 * заданных параметров и добавленных позиций материалов.
 *
 * @return собранный объект Calculation
 * @throws IllegalStateException если не указано имя расчёта
 * @throws IllegalStateException если не указана формула расчёта
 * @throws IllegalStateException если не добавлено ни одной позиции материала
 */
Calculation build();
```
