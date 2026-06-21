package ru.denis.Calculator.Foundation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import ru.denis.Calculator.Entity.Material;

/**
 * Identity Map — паттерн рефакторинга.
 *
 * <p>Хранит ссылки на объекты {@link Material}, загруженные из БД в рамках
 * одного HTTP-запроса. Гарантирует, что один и тот же объект не загружается
 * из базы данных дважды за время обработки одного запроса.</p>
 *
 * <p><b>Проблема до рефакторинга:</b> если несколько методов сервиса или
 * нескольких сервисов обращались к {@code materialRepository.findById(id)}
 * с одним и тем же идентификатором в рамках одного запроса, каждый вызов
 * порождал отдельный SQL-запрос к БД. При цепочке операций (загрузка →
 * проверка → обновление) это означало 2-3 лишних SELECT на один объект.</p>
 *
 * <p><b>Решение:</b> {@code @RequestScope} создаёт новый экземпляр кэша
 * для каждого входящего HTTP-запроса и уничтожает его по завершении.
 * Таким образом, кэш изолирован между запросами и не приводит к проблемам
 * с устаревшими данными.</p>
 *
 * <p><b>Использование:</b>
 * <pre>{@code
 * // Первый вызов — обращается к БД и кладёт объект в кэш
 * Material m = identityMap.get(id)
 *         .orElseGet(() -> {
 *             Material loaded = repo.findById(id).orElseThrow(...);
 *             identityMap.put(id, loaded);
 *             return loaded;
 *         });
 *
 * // Второй вызов с тем же id — возвращает объект из кэша без SQL
 * Material same = identityMap.get(id).orElseThrow(...);
 * }</pre>
 * </p>
 */
@Component
@RequestScope
public class MaterialIdentityMap {

    private final Map<Integer, Material> cache = new HashMap<>();

    /**
     * Возвращает закэшированный объект по идентификатору.
     *
     * @param id идентификатор материала
     * @return {@code Optional} с объектом, если он уже был загружен в этом запросе
     */
    public Optional<Material> get(Integer id) {
        return Optional.ofNullable(cache.get(id));
    }

    /**
     * Помещает объект в кэш.
     *
     * @param id       идентификатор материала
     * @param material загруженный или сохранённый объект
     */
    public void put(Integer id, Material material) {
        cache.put(id, material);
    }

    /**
     * Удаляет объект из кэша (вызывается после удаления из БД).
     *
     * @param id идентификатор удалённого материала
     */
    public void evict(Integer id) {
        cache.remove(id);
    }
}
