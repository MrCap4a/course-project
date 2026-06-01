package ru.denis.Calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class CalculatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CalculatorApplication.class, args);
	}

	@Bean
	public CommandLineRunner printAdminHash() {
		return args -> {
			String hash = new BCryptPasswordEncoder().encode("admin");
			System.out.println("========================================");
			System.out.println("ADMIN HASH: " + hash);
			System.out.println("========================================");
		};
	}
}
