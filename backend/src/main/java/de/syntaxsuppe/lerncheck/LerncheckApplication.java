// Summary: Spring Boot Einstiegspunkt.
package de.syntaxsuppe.lerncheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LerncheckApplication {
    public static void main(String[] args) {
        SpringApplication.run(LerncheckApplication.class, args);
    }
}