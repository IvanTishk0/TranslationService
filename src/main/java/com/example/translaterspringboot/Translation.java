package com.example.translaterspringboot;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "translation_requests")
public class Translation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_ip")
    private String userIp;

    @Column(name = "input_text")
    private String inputText;

    @Column(name = "translated_text")
    private String translatedText;

    @Column(name = "target_language")
    private String targetLanguage;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    // Конструкторы, геттеры и сеттеры
    public Translation() {}
    public Translation(Long id, String userIp, String inputText, String translatedText, String targetLanguage, LocalDateTime timestamp) {
        this.id = id;
        this.userIp = userIp;
        this.inputText = inputText;
        this.translatedText = translatedText;
        this.targetLanguage = targetLanguage;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}