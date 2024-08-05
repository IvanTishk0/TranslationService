package com.example.translaterspringboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class TranslaterSpringbootApplication implements CommandLineRunner {

    @Autowired
    private TranslationService translationService;

    public static void main(String[] args) {
        SpringApplication.run(TranslaterSpringbootApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

    }
}