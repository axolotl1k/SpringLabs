package org.axolotlik.labs.repo;

import org.axolotlik.labs.model.Lesson;
import java.util.List;
import java.util.Optional;

public interface JournalRepository {
    List<Lesson> findAll();
    Optional<Lesson> findById(Long id);
    Lesson save(Lesson lesson);
    void deleteById(Long lessonId);
}