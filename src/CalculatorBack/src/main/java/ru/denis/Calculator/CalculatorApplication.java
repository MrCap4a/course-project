package ru.denis.Calculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class CalculatorApplication {

    private static final Logger LOG = LoggerFactory.getLogger(CalculatorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CalculatorApplication.class, args);
    }

    @Bean
    public CommandLineRunner printAdminHash() {
        return args -> {
            String hash = new BCryptPasswordEncoder().encode("admin");
            LOG.info("========================================");
            LOG.info("ADMIN HASH: {}", hash);
            LOG.info("========================================");
        };
    }
}
