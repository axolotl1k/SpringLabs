package org.axolotlik.labs.service;

import org.axolotlik.labs.model.Lesson;
import org.axolotlik.labs.model.Mark;

import java.util.List;
import java.util.Optional;

public interface JournalService {
    List<Lesson> getAllLessons();
    Optional<Lesson> getLessonById(Long id);
    Lesson createLesson(String subject, String topic);
    void updateLesson(Long lessonId, String newSubject, String newTopic);
    void deleteLesson(Long lessonId);

    void addMark(Long lessonId, Mark mark);
    Mark findMarkById(Long lessonId, Long markId);
    void updateMark(Long lessonId, Long markId, Mark updatedMark);
    void deleteMark(Long lessonId, Long markId);
}