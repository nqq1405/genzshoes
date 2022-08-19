package com.quyquang.genzshoes3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
public class Genzshoes3Application {
    public static void main(String[] args) {
        SpringApplication.run(Genzshoes3Application.class, args);
    }
    // abcaaa
}
