package com.example.translaterspringboot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TranslationRepository {
    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String user;

    @Value("${spring.datasource.password}")
    private String password;

    // Создание подключения к базе данных
    private Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
        return connection;
    }

    // Создание нового запроса на перевод
    public Translation save(Translation translation) {
        String sql = "INSERT INTO translation_requests (user_ip, input_text, translated_text, target_language, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = connect(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, translation.getUserIp());
            preparedStatement.setString(2, translation.getInputText());
            preparedStatement.setString(3, translation.getTranslatedText());
            preparedStatement.setString(4, translation.getTargetLanguage());
            preparedStatement.setObject(5, translation.getTimestamp());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка при создании запроса: " + e.getMessage());
        }
        return translation;
    }

    // Удаление запроса на перевод по ID
    public void deleteById(Long id) {
        String sql = "DELETE FROM translation_requests WHERE id = ?";
        try (Connection connection = connect(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении запроса на перевод: " + e.getMessage());
        }
    }

    public void deleteAll() {
        String sql = "DELETE FROM translation_requests";
        try (Connection connection = connect();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
             pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Поиск запроса по ID
    public Translation findById(Long id) {
        String sql = "SELECT * FROM translation_requests WHERE id = ?";
        Translation translation = null;
        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                translation = new Translation();
                translation.setId(resultSet.getLong("id"));
                translation.setUserIp(resultSet.getString("user_ip"));
                translation.setInputText(resultSet.getString("input_text"));
                translation.setTranslatedText(resultSet.getString("translated_text"));
                translation.setTargetLanguage(resultSet.getString("target_language"));
                translation.setTimestamp(resultSet.getObject("timestamp", LocalDateTime.class));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске запроса: " + e.getMessage());
        }
        return translation;
    }

    // Вывод всех запросов
    public List<Translation> findAll() {
        String sql = "SELECT * FROM translation_requests";
        List<Translation> translations = new ArrayList<>();
        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Translation translation = new Translation();
                translation.setId(resultSet.getLong("id"));
                translation.setUserIp(resultSet.getString("user_ip"));
                translation.setInputText(resultSet.getString("input_text"));
                translation.setTranslatedText(resultSet.getString("translated_text"));
                translation.setTargetLanguage(resultSet.getString("target_language"));
                translation.setTimestamp(resultSet.getObject("timestamp", LocalDateTime.class));
                translations.add(translation);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении всех запросов: " + e.getMessage());
        }
        return translations;
    }
}