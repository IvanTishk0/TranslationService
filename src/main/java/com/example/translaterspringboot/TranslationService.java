package com.example.translaterspringboot;


import org.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private static final String API_URL = "https://translate.api.cloud.yandex.net/translate/v2/translate";
    private static final String LANGUAGES_API_URL = "https://translate.api.cloud.yandex.net/translate/v2/languages";

    private static final String API_KEY = "AQVN2JTbI2QDjIrdOGteAZ6aBFO_0JT0kiK1Y-oi"; // Ваш API ключ

    public TranslationService(TranslationRepository translationRepository) {
        this.translationRepository = translationRepository;
    }

    public TranslationResponse translate(TranslationRequest request) {
        List<String> translatedTexts = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
        List<Future<String>> futures = new ArrayList<>();

        try {
            String[] texts = StringToWordArray(request.getInputText());
            int i = 0;
            for (String text : texts) {
                if (i == 19) {
                    Thread.sleep(1001);
                    i = 0;
                }
                futures.add(executorService.submit(() -> translateWord(text, request.getTargetLanguage())));
                i++;
            }

            for (Future<String> future : futures) {
                try {
                    translatedTexts.add(future.get());
                } catch (ExecutionException e) {
                    System.out.println("Ошибка при переводе слова: " + e.getCause());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }

        String finalTranslatedText = String.join(" ", translatedTexts);
        TranslationResponse translationResponse = new TranslationResponse();
        translationResponse.setTranslatedText(finalTranslatedText);
        saveTranslation(request, translationResponse);

        return translationResponse;
    }

    private String translateWord(String text, String targetLanguage) {
        String translatedText = "";
        try {
            // Заголовки запроса
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Api-Key " + API_KEY);

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("targetLanguageCode", targetLanguage);
            jsonRequest.put("texts", new JSONArray().put(text));
            jsonRequest.put("folderId", FOLDER_ID);

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequest.toString(), headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, String.class);

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

    private void saveTranslation(TranslationRequest request, TranslationResponse response) {
        // Сохраняем перевод в БД
        Translation translation = new Translation();
        translation.setUserIp(getPublicIPAddress());
        translation.setInputText(request.getInputText());
        translation.setTargetLanguage(request.getTargetLanguage());
        translation.setTranslatedText(response.getTranslatedText());
        translation.setTimestamp(LocalDateTime.now());
        translationRepository.save(translation);
    }

    public HashMap<String, String> getLanguages() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Api-Key " + API_KEY);

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
            JSONArray languagesArray = jsonResponse.getJSONArray("languages");

            // Заполнение хэш-таблицы языками
            for (int i = 0; i < languagesArray.length(); i++) {
                JSONObject languageObject = languagesArray.getJSONObject(i);
                try {
                    String code = languageObject.getString("code");
                    String name = languageObject.getString("name");
                    languagesMap.put(code, name);
                } catch (JSONException e) {
                }
            }
            return languagesMap;
        } else {
            System.err.println("Ошибка при получении языков: " + responseEntity.getStatusCode());
        }

        return languagesMap; // Возврат пустой хэш-таблицы
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

}
