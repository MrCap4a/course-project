package ru.denis.Calculator.Mapper;

import org.springframework.stereotype.Component;
import ru.denis.Calculator.Dto.MaterialDto;
import ru.denis.Calculator.Dto.Request.MaterialRequest;
import ru.denis.Calculator.Entity.Material;
import ru.denis.Calculator.Entity.MaterialGroup;

/**
 * Data Mapper — паттерн рефакторинга.
 *
 * <p>Отвечает за преобразование между Entity-объектами слоя Foundation/Entity
 * и DTO-объектами слоя Control. Изолирует логику маппинга в одном месте,
 * устраняя её дублирование по всем методам *ServiceImpl.</p>
 *
 * <p>До рефакторинга: конвертация {@code new MaterialDto(...)} выполнялась
 * непосредственно в {@code MaterialServiceImpl} и повторялась в каждом методе,
 * возвращающем DTO.</p>
 *
 * <p>После рефакторинга: {@code MaterialServiceImpl} делегирует всю конвертацию
 * этому классу через {@code mapper.toDto(entity)} и {@code mapper.applyRequest(entity, request)}.</p>
 */
@Component
public class MaterialMapper {

    /**
     * Преобразует JPA-сущность {@link Material} в DTO для передачи клиенту.
     *
     * @param material сущность из БД (не null)
     * @return DTO с плоской структурой данных
     */
    public MaterialDto toDto(Material material) {
        return new MaterialDto(
                material.getId(),
                material.getName(),
                material.getPrice(),
                material.getUnits(),
                material.getGroup().getId(),
                material.getGroup().getName()
        );
    }

    /**
     * Создаёт новый {@link Material} из запроса без сохранения.
     * Группа устанавливается отдельно вызывающим кодом.
     *
     * @param request данные из тела HTTP-запроса
     * @param group   уже загруженная или разрешённая группа
     * @return несохранённая сущность
     */
    public Material toEntity(MaterialRequest request, MaterialGroup group) {
        Material material = new Material();
        material.setName(request.name());
        material.setPrice(request.price());
        material.setUnits(request.units());
        material.setGroup(group);
        return material;
    }

    /**
     * Применяет данные из запроса к существующей сущности (update-сценарий).
     *
     * @param material сущность из БД
     * @param request  данные из тела HTTP-запроса
     * @param group    уже загруженная или разрешённая группа
     */
    public void applyRequest(Material material, MaterialRequest request, MaterialGroup group) {
        material.setName(request.name());
        material.setPrice(request.price());
        material.setUnits(request.units());
        material.setGroup(group);
    }
}
