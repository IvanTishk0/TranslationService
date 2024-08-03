package com.example.translaterspringboot;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Service
public class TranslationService {
    @Autowired
    private final TranslationRepository translationRepository;

    private static final int MAX_THREADS = 10; // Максимальное число потоков
    private static final String FOLDER_ID = "b1g0qu41gfb4javdujt4";
    private static final String API_KEY = "t1.9euelZrOmJTHxsaQzcuUx8iMm4yLje3rnpWaiYnOmpSMxpnLl4qVk4yNy47l8_caI0dK-e8NYkRZ_d3z91pRREr57w1iRFn9zef1656VmorPzMvPisyPzY-Sm56VkJiJ7_zF656VmorPzMvPisyPzY-Sm56VkJiJ.6i781SPf3PYOP92V5EkuQ7pAgTDP-Ty72PDVLKYsSpQfbjjqsJC5MipIkKvLEG7KoKkx90qLCf03EoFiO-1ADA";
    private static final String API_URL = "https://translate.api.cloud.yandex.net/translate/v2/translate";
    private static final String LANGUAGES_API_URL = "https://translate.api.cloud.yandex.net/translate/v2/languages";

    public TranslationResponse translate(TranslationRequest request) {
        List<String> translatedTexts = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
        List<Future<String>> futures = new ArrayList<>();

        try {
            String[] texts = StringToWordArray(request.getInputText());

            for (String text : texts) {
                // Отправляем задачу на выполнение в отдельном потоке
                futures.add(executorService.submit(() -> translateWord(text, request.getTargetLanguage())));
            }

            // Получаем результаты
            for (Future<String> future : futures) {
                try {
                    translatedTexts.add(future.get()); // Добавляем переведённые слова в список
                } catch (ExecutionException e) {
                    System.out.println("Ошибка при переводе слова: " + e.getCause());
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Восстанавливаем прерывание
            e.printStackTrace();
        } finally {
            executorService.shutdown(); // Завершаем ExecutorService
        }

        // Формируем итоговую строку перевода
        String finalTranslatedText = String.join(" ", translatedTexts);

        // Создаем объект ответа
        TranslationResponse translationResponse = new TranslationResponse();
        translationResponse.setTranslatedText(finalTranslatedText);

        // Сохраняем перевод в репозиторий
        Translation translation = new Translation();
        translation.setUserIp(getPublicIPAddress());
        translation.setInputText(request.getInputText());
        translation.setTargetLanguage(request.getTargetLanguage());
        translation.setTranslatedText(translationResponse.getTranslatedText());
        translation.setTimestamp(LocalDateTime.now());
        translationRepository.save(translation);

        return translationResponse;
    }

    private String translateWord(String text, String targetLanguage) {
        String translatedText = "";
        try {


            // Заголовки запроса
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + API_KEY);

            // Формирование JSON тела запроса
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("targetLanguageCode", targetLanguage);
            jsonRequest.put("texts", new JSONArray().put(text));
            jsonRequest.put("folderId", FOLDER_ID);

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequest.toString(), headers);

            // Отправка POST-запроса
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, String.class);

            // Обработка JSON ответа
            JSONObject jsonResponse = new JSONObject(responseEntity.getBody());
            JSONArray translations = jsonResponse.getJSONArray("translations");
            if (translations.length() > 0) {
                translatedText = translations.getJSONObject(0).getString("text");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return translatedText;
    }

    public HashMap<String, String> getLanguages() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);

        // Формирование JSON тела запроса
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("folderId", FOLDER_ID);

        // Создание сущности запроса
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequest.toString(), headers);

        // Отправка GET-запроса
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(LANGUAGES_API_URL, HttpMethod.POST, requestEntity, String.class);

        HashMap<String, String> languagesMap = new HashMap<>();

        // Обработка ответа
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();

            // Парсинг JSON-ответа
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray languagesArray = jsonResponse.getJSONArray("languages"); // Предполагается, что массив языков находится по этому ключу

            // Заполнение хэш-таблицы языками
            for (int i = 0; i < languagesArray.length(); i++) {
                JSONObject languageObject = languagesArray.getJSONObject(i);
                try {
                    String code = languageObject.getString("code"); // Получение кода языка
                    String name = languageObject.getString("name"); // Получение названия языка
                    languagesMap.put(code, name); // Сохранение в хэш-таблицу
                } catch (JSONException e) {
                }
            }

            return languagesMap;
        } else {
            System.err.println("Ошибка при получении языков: " + responseEntity.getStatusCode());
        }

        return languagesMap; // Возврат пустой хэш-таблицы в случае ошибки
    }

    public String getPublicIPAddress() {
        String ipAddress = new String();
        try {
            URL url = new URL("http://api.ipify.org");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            ipAddress = in.readLine();
            in.close();

        } catch (Exception e) {
            System.out.println("Ошибка при получении публичного IP-адреса.");
            e.printStackTrace();
        }
        return ipAddress;
    }

    private String[] StringToWordArray(String inputText) {
        return inputText.split(" "); // Разделение текста на слова
    }

    public TranslationService(TranslationRepository translationRepository) {
        this.translationRepository = translationRepository;
    }

    // Метод для получения всех переводов
    public List<Translation> getAllTranslations() {
        return translationRepository.findAll();
    }

    // Метод для сохранения нового перевода
    public Translation saveTranslation(Translation translation) {
        return translationRepository.save(translation);
    }

    // Метод для получения перевода по ID
    public Translation getTranslationById(Long id) {
        return translationRepository.findById(id).orElse(null);
    }

    // Метод для удаления перевода по ID
    public void deleteTranslation(Long id) {
        translationRepository.deleteById(id);
    }

}
