package ru.denis.Calculator.Foundation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.denis.Calculator.Entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
}
