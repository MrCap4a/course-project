package ru.denis.Calculator.Foundation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.denis.Calculator.Entity.Formula;
import ru.denis.Calculator.Entity.FormulaGroup;

@Repository
public interface FormulaRepository extends JpaRepository<Formula, Integer> {

    Page<Formula> findByGroup(FormulaGroup group, Pageable pageable);
}
