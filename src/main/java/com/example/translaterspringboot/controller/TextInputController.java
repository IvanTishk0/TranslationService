package com.example.translaterspringboot.controller;

import com.example.translaterspringboot.TranslationRequest;
import com.example.translaterspringboot.TranslationResponse;
import com.example.translaterspringboot.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

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
            language = "en";
        }

        model.addAttribute("languages", languages);
        model.addAttribute("selectedLanguage", language); // Передаем выбранный язык
        model.addAttribute("inputText", ""); // Изначально поле ввода пустое
        return "index";
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
        model.addAttribute("inputText", text); // Сохраняем введенный текст для следующего запроса
        return "index";
    }
}