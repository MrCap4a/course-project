package ru.denis.Calculator.Foundation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.denis.Calculator.Entity.Material;
import ru.denis.Calculator.Entity.MaterialGroup;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Integer> {

    List<Material> findByGroup(MaterialGroup group);

    Page<Material> findByGroup(MaterialGroup group, Pageable pageable);

    Page<Material> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Material> findByGroupAndNameContainingIgnoreCase(MaterialGroup group, String name, Pageable pageable);
}
