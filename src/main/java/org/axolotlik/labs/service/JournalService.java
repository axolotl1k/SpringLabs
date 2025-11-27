package org.axolotlik.labs.service;

import org.axolotlik.labs.model.Lesson;
import org.axolotlik.labs.model.LessonPage;
import org.axolotlik.labs.model.Mark;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface JournalService {

    // --- Уроки (Lesson) ---
    List<Lesson> getAllLessons();
    Optional<Lesson> getLessonById(Long id);
    Lesson createLesson(String subject, String topic);
    void updateLesson(Long lessonId, String newSubject, String newTopic);
    void deleteLesson(Long lessonId);

    // --- Оцінки (Mark) ---
    void addMark(Long lessonId, Mark mark);
    Mark findMarkById(Long lessonId, Long markId);
    void updateMark(Long lessonId, Long markId, Mark updatedMark);
    void deleteMark(Long lessonId, Long markId);

    // --- фільтрація + пагінація ---
    LessonPage findLessons(String subject,
                           LocalDate dateFrom,
                           LocalDate dateTo,
                           int page,
                           int size);

    // --- часткове оновлення (PATCH) ---
    Optional<Lesson> patchLesson(Long id, Map<String, Object> updates);
}
