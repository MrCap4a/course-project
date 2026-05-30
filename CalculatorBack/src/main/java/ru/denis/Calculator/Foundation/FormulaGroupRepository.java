package ru.denis.Calculator.Foundation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.denis.Calculator.Entity.FormulaGroup;

@Repository
public interface FormulaGroupRepository extends JpaRepository<FormulaGroup, Integer> {
}
