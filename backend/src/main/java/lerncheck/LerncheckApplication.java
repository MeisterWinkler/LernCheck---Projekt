package lerncheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "lerncheck",   // falls du später alles in lerncheck.* packst
        "api",
        "domain",
        "interfaces",
        "security",
        "service"
})
@EnableJpaRepositories(basePackages = "interfaces")
@EntityScan(basePackages = "domain")
public class LerncheckApplication {

    public static void main(String[] args) {
        SpringApplication.run(LerncheckApplication.class, args);
    }
}