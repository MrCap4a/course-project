# Рефакторинг

## Обзор

На данном этапе к проекту применены два паттерна рефакторинга: **Data Mapper** и **Identity Map**.
Оба паттерна направлены на улучшение разделения ответственности и устранение дублирования кода между слоями Entity и Control.

---

## Выявленные запахи кода

### 1. Дублирование логики маппинга (Duplicated Code)

**Описание:** в классах `MaterialServiceImpl` и `FormulaServiceImpl` логика преобразования Entity → DTO была встроена в приватный метод `toDto()` каждого сервиса. При добавлении нового поля в DTO нужно было менять код в нескольких местах. Аналогично, при создании и редактировании объекта (`createMaterial`, `editMaterial`) блок `material.setName(...); material.setPrice(...); ...` повторялся дважды в одном классе.

**Пример до рефакторинга** (`MaterialServiceImpl.java`):
```java
// Метод createMaterial
Material material = new Material();
material.setName(request.name());
material.setPrice(request.price());
material.setUnits(request.units());
material.setGroup(group);

// Метод editMaterial — тот же блок повторяется
material.setName(request.name());
material.setPrice(request.price());
material.setUnits(request.units());
material.setGroup(group);

// Метод toDto — встроен прямо в сервис
private MaterialDto toDto(Material material) {
    return new MaterialDto(
            material.getId(),
            material.getName(),
            material.getPrice(),
            material.getUnits(),
            material.getGroup().getId(),
            material.getGroup().getName()
    );
}
```

**Проблема:** при изменении DTO (например, добавлении поля `description`) нужно обновить `toDto()` в каждом сервисе отдельно.

---

### 2. Повторные запросы к БД в одном HTTP-запросе (Repeated Database Calls)

**Описание:** при выполнении операции редактирования или проверки прав доступа объект `Material` мог загружаться из БД несколько раз в рамках одного HTTP-запроса. Слой Proxy (`MaterialServiceProxy`) проверяет существование объекта через `getMaterialById()`, который вызывает `findById()` — затем основной сервис снова вызывает `findById()` для редактирования. Итого: 2 SELECT к одной строке за один запрос.

---

## Паттерн 1: Data Mapper

### Что изменилось

Создан новый пакет `ru.denis.Calculator.Mapper` с классами `MaterialMapper` и `FormulaMapper`.
Каждый mapper содержит:
- `toDto(entity)` — Entity → DTO
- `toEntity(request, group)` — Request + Group → новая Entity
- `applyRequest(entity, request, group)` — применение изменений к существующей Entity (для update)

Сервисы (`MaterialServiceImpl`, `FormulaServiceImpl`) инжектируют mapper через конструктор и больше не содержат встроенных блоков маппинга.

### До рефакторинга

```java
// MaterialServiceImpl.java
private MaterialDto toDto(Material material) {
    return new MaterialDto(
            material.getId(),
            material.getName(),
            material.getPrice(),
            material.getUnits(),
            material.getGroup().getId(),
            material.getGroup().getName()
    );
}

public MaterialDto createMaterial(MaterialRequest request) {
    MaterialGroup group = resolveGroup(request.groupId());
    Material material = new Material();
    material.setName(request.name());       // маппинг встроен в сервис
    material.setPrice(request.price());
    material.setUnits(request.units());
    material.setGroup(group);
    return toDto(materialRepository.save(material));
}
```

### После рефакторинга

```java
// MaterialMapper.java — изолированная логика маппинга
@Component
public class MaterialMapper {
    public MaterialDto toDto(Material material) { ... }
    public Material toEntity(MaterialRequest request, MaterialGroup group) { ... }
    public void applyRequest(Material material, MaterialRequest request, MaterialGroup group) { ... }
}

// MaterialServiceImpl.java — только бизнес-логика
public MaterialDto createMaterial(MaterialRequest request) {
    MaterialGroup group = resolveGroup(request.groupId());
    Material material = materialMapper.toEntity(request, group);  // делегирование маперу
    return materialMapper.toDto(materialRepository.save(material));
}
```

### Затронутые файлы

| Файл | Изменение |
|---|---|
| `Mapper/MaterialMapper.java` | Создан |
| `Mapper/FormulaMapper.java` | Создан |
| `Mediator/Impl/MaterialServiceImpl.java` | Рефакторинг: удалён `toDto()`, добавлен `MaterialMapper` |
| `Mediator/Impl/FormulaServiceImpl.java` | Рефакторинг: удалён `toDto()`, добавлен `FormulaMapper` |

---

## Паттерн 2: Identity Map

### Что изменилось

Создан класс `MaterialIdentityMap` в пакете `ru.denis.Calculator.Foundation`.
Аннотирован `@RequestScope` — Spring создаёт новый экземпляр кэша для каждого HTTP-запроса и уничтожает его по завершении, что гарантирует изоляцию данных между запросами.

`MaterialServiceImpl` инжектирует `MaterialIdentityMap` и использует его в методе `getMaterialById()`:
1. Сначала проверяет кэш — если объект уже загружен, возвращает без SQL.
2. При промахе — загружает из репозитория и помещает в кэш.
3. При создании/редактировании — обновляет кэш.
4. При удалении — вызывает `evict()`.

### До рефакторинга

```java
// Каждый вызов getMaterialById → новый SELECT
public MaterialDto getMaterialById(Integer id) {
    return toDto(materialRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Material not found: " + id)));
}
```

### После рефакторинга

```java
// MaterialIdentityMap.java
@Component
@RequestScope
public class MaterialIdentityMap {
    private final Map<Integer, Material> cache = new HashMap<>();

    public Optional<Material> get(Integer id) { return Optional.ofNullable(cache.get(id)); }
    public void put(Integer id, Material material) { cache.put(id, material); }
    public void evict(Integer id) { cache.remove(id); }
}

// MaterialServiceImpl.java
public MaterialDto getMaterialById(Integer id) {
    return identityMap.get(id)                   // 1. смотрим в кэш
            .map(materialMapper::toDto)
            .orElseGet(() -> {
                Material material = materialRepository.findById(id)  // 2. промах → SQL
                        .orElseThrow(() -> new RuntimeException("Material not found: " + id));
                identityMap.put(id, material);   // 3. кладём в кэш
                return materialMapper.toDto(material);
            });
}
```

### Затронутые файлы

| Файл | Изменение |
|---|---|
| `Foundation/MaterialIdentityMap.java` | Создан |
| `Mediator/Impl/MaterialServiceImpl.java` | Инжектирован `MaterialIdentityMap`, обновлены методы `getMaterialById`, `createMaterial`, `editMaterial`, `deleteMaterial` |

---

## Итог

| Паттерн | Устранённый запах | Новые файлы |
|---|---|---|
| Data Mapper | Дублирование маппинга в сервисах | `MaterialMapper.java`, `FormulaMapper.java` |
| Identity Map | Повторные SELECT к одной строке в одном запросе | `MaterialIdentityMap.java` |
