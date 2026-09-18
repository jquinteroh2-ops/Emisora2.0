package com.emisora;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class EmisoraApplicationTests {

    @Test
    void contextLoads() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashAdmin = encoder.encode("Admin2026*");
        String hashAmerican = encoder.encode("American8");
        System.out.println("BCRYPT Admin2026*: " + hashAdmin);
        System.out.println("BCRYPT American8: " + hashAmerican);
        assertTrue(encoder.matches("Admin2026*", hashAdmin));
        assertTrue(encoder.matches("American8", hashAmerican));
    }
}
