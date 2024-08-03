package com.example.translaterspringboot;

import com.example.translaterspringboot.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, Long> {
    // Метод для поиска переводов по тексту
    List<Translation> findByInputText(String inputText);
}