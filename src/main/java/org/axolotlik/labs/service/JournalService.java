package org.axolotlik.labs.service;

import org.axolotlik.labs.model.Lesson;
import org.axolotlik.labs.model.LessonPage;
import org.axolotlik.labs.model.Mark;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface JournalService {
    // Lessons
    List<Lesson> getAllLessons();
    Optional<Lesson> getLessonById(Long id);
    Lesson createLesson(String subject, String topic);
    void updateLesson(Long lessonId, String newSubject, String newTopic);
    void deleteLesson(Long lessonId);
    LessonPage findLessons(String subject, LocalDate dateFrom, LocalDate dateTo, int page, int size);
    Optional<Lesson> patchLesson(Long id, Map<String, Object> updates);

    // Marks
    List<Mark> getMarksForLesson(Long lessonId);
    Mark findMarkById(Long lessonId, Long markId);
    void addMark(Long lessonId, Mark mark);
    void updateMark(Long lessonId, Long markId, Mark updatedMark);
    void deleteMark(Long lessonId, Long markId);
    List<Mark> latestMarks(int limit);
}
