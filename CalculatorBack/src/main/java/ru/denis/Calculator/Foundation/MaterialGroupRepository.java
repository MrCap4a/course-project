package ru.denis.Calculator.Foundation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.denis.Calculator.Entity.MaterialGroup;

import java.util.Optional;

@Repository
public interface MaterialGroupRepository extends JpaRepository<MaterialGroup, Integer> {

    Optional<MaterialGroup> findByName(String name);
}
