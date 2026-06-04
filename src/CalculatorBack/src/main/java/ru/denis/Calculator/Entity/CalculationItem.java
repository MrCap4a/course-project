package ru.denis.Calculator.Entity;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "calculation_item", indexes = {
        @Index(name = "calculation_item_index_0", columnList = "calculation_id,material_id")
})
public class CalculationItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Short position; // Соответствует SMALLINT

    @Column(nullable = false, columnDefinition = "NUMERIC CHECK (quantity >= 0)")
    private BigDecimal quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculation_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_calculation_item_calculation_id_calculation"))
    private Calculation calculation;

    // nullable: материал может быть удалён — расчёт становится невычислимым (UC-007)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = true,
            foreignKey = @ForeignKey(name = "fk_calculation_item_material_id_material"))
    private Material material;
}
