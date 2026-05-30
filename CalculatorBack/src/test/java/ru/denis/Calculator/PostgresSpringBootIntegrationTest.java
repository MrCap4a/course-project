package ru.denis.Calculator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.denis.Calculator.Entity.Material;
import ru.denis.Calculator.Entity.MaterialGroup;
import ru.denis.Calculator.Foundation.MaterialGroupRepository;
import ru.denis.Calculator.Foundation.MaterialRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ExtendWith(PostgresSpringBootIntegrationTest.DockerAvailableCondition.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class PostgresSpringBootIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:16.3")
            .withDatabaseName("calculator_test")
            .withUsername("postgres")
            .withPassword("2212");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private MaterialGroupRepository materialGroupRepository;

    @Test
    void contextLoads() {
        assertThat(materialRepository).isNotNull();
        assertThat(materialGroupRepository).isNotNull();
    }

    @Test
    void saveMaterialWithGroupAndQueryByName() {
        MaterialGroup group = new MaterialGroup();
        group.setName("Integration Test Group");
        group = materialGroupRepository.save(group);

        Material material = new Material();
        material.setName("Test board");
        material.setPrice(new BigDecimal("12.50"));
        material.setUnits("pcs");
        material.setGroup(group);
        material = materialRepository.save(material);

        assertThat(material.getId()).isNotNull();

        List<Material> byGroup = materialRepository.findByGroup(group);
        assertThat(byGroup).singleElement().extracting(Material::getName).isEqualTo("Test board");

        List<Material> byName = materialRepository.findByNameContainingIgnoreCase("board");
        assertThat(byName).singleElement().extracting(Material::getPrice).isEqualTo(new BigDecimal("12.50"));
    }

    @Test
    void saveMultipleMaterialsAndSearchByGroupAndName() {
        MaterialGroup group = new MaterialGroup();
        group.setName("Search Group");
        group = materialGroupRepository.save(group);

        Material first = new Material();
        first.setName("Gray Board");
        first.setPrice(new BigDecimal("9.99"));
        first.setUnits("kg");
        first.setGroup(group);
        materialRepository.save(first);

        Material second = new Material();
        second.setName("White Board");
        second.setPrice(new BigDecimal("15.00"));
        second.setUnits("kg");
        second.setGroup(group);
        materialRepository.save(second);

        List<Material> matching = materialRepository.findByGroupAndNameContainingIgnoreCase(group, "white");
        assertThat(matching).hasSize(1);
        assertThat(matching.get(0).getName()).isEqualTo("White Board");
    }

    static class DockerAvailableCondition implements ExecutionCondition {
        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            try {
                boolean dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
                return dockerAvailable
                        ? ConditionEvaluationResult.enabled("Docker is available")
                        : ConditionEvaluationResult.disabled("Docker is not available");
            } catch (Throwable ex) {
                return ConditionEvaluationResult.disabled("Docker availability check failed: " + ex.getMessage());
            }
        }
    }
}
