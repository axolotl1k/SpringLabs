package org.axolotlik.labs.repo;

import org.axolotlik.labs.model.Lesson;
import java.util.*;

public interface LessonRepository {
    Long save(Lesson lesson);
    int update(Lesson lesson);
    Optional<Lesson> findById(Long id);
    List<Lesson> findAll();
    void deleteById(Long id);
}
