// Summary: Spring Boot Einstiegspunkt. ScanBasePackages stellt sicher,
// dass auch api/domain/interfaces/security/service gefunden werden.
package lerncheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "main.java")
public class LerncheckApplication {
    public static void main(String[] args) {
        SpringApplication.run(LerncheckApplication.class, args);
    }
}