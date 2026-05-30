package ru.denis.Calculator.Foundation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.denis.Calculator.Entity.Calculation;

@Repository
public interface CalculationRepository extends JpaRepository<Calculation, Integer> {
    Calculation findByName(String name);
}
