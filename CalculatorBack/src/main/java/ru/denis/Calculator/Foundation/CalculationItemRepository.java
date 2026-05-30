package ru.denis.Calculator.Foundation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.denis.Calculator.Entity.Calculation;
import ru.denis.Calculator.Entity.CalculationItem;

import java.util.List;

@Repository
public interface CalculationItemRepository extends JpaRepository<CalculationItem, Integer> {
    List<CalculationItem> findByCalculation(Calculation calculation);
}
