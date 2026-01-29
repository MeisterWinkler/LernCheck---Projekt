// Summary: Spring Boot Einstiegspunkt. ScanBasePackages sorgt dafür, dass api/domain/interfaces/... gefunden werden.
package lerncheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"api", "domain", "interfaces", "security", "service", "lerncheck"})
@EnableJpaRepositories(basePackages = {"interfaces"})
@EntityScan(basePackages = {"domain"})
public class LerncheckApplication {
    public static void main(String[] args) {
        SpringApplication.run(LerncheckApplication.class, args);
    }
}