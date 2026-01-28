// Summary: Spring Boot Einstiegspunkt. ScanBasePackages sorgt dafür, dass api/domain/interfaces/... gefunden werden.
package lerncheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"api", "domain", "interfaces", "security", "service", "lerncheck"})
public class LerncheckApplication {
    public static void main(String[] args) {
        SpringApplication.run(LerncheckApplication.class, args);
    }
}