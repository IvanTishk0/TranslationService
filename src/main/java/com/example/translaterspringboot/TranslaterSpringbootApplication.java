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
//        TranslationRequest translationRequest = new TranslationRequest();
//        translationRequest.setInputText("Hello world, this is my first program");
//        translationRequest.setTargetLanguage("ru");
//        translationService.translate(translationRequest);
//        translationService.getLanguages();
//        // Пример создания нового перевода
//        Translation translation1 = new Translation();
//        translation1.setInputText("Привет");
//        translation1.setTranslatedText("Hello");
//        translationService.saveTranslation(translation1);

        // Удаление всех переводов
//        List<Translation> translations = translationService.getAllTranslations();
//        translations.forEach(t -> System.out.println(t.getInputText() + " => " + t.getTranslatedText()));
//        while (!translations.isEmpty()) {
//            translationService.deleteTranslation(translations.get(0).getId());
//            System.out.println("Перевод удалён.");
//            translations = translationService.getAllTranslations();
//        }
    }
}