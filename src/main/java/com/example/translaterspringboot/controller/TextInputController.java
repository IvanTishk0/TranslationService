package com.example.translaterspringboot.controller;


import com.example.translaterspringboot.TranslationRequest;
import com.example.translaterspringboot.TranslationResponse;
import com.example.translaterspringboot.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class TextInputController {

    @Autowired
    private TranslationService translationService;
    HashMap<String, String> languages;

    @RequestMapping("/")
    public String showForm(@RequestParam(value = "language", required = false) String language, Model model) {
        languages = translationService.getLanguages();

        // Устанавливаем язык по умолчанию, если еще не выбран
        if (language == null) {
            language = "en"; // Задаем язык по умолчанию
        }

        model.addAttribute("languages", languages);
        model.addAttribute("selectedLanguage", language); // Передаем выбранный язык
        return "index"; // Имя вашего шаблона
    }

    @PostMapping("/translate")
    public String translate(@RequestParam("text") String text,
                            @RequestParam("language") String language,
                            Model model) {
        languages = translationService.getLanguages();

        TranslationRequest translationRequest = new TranslationRequest();
        translationRequest.setInputText(text);
        translationRequest.setTargetLanguage(language);
        TranslationResponse translatedText = translationService.translate(translationRequest);

        model.addAttribute("languages", languages);
        model.addAttribute("selectedLanguage", language); // Передаем выбранный язык
        model.addAttribute("translatedText", translatedText.getTranslatedText());
        return "index";
    }

}