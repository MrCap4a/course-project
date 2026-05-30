package ru.denis.Calculator.Foundation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.denis.Calculator.Entity.Formula;
import ru.denis.Calculator.Entity.FormulaGroup;

import java.util.List;

@Repository
public interface FormulaRepository extends JpaRepository<Formula, Integer> {

    List<Formula> findByGroup(FormulaGroup group);
}
