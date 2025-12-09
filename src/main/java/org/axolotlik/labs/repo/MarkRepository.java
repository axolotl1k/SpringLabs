package org.axolotlik.labs.repo;

import org.axolotlik.labs.model.Mark;
import java.util.*;

public interface MarkRepository {
    Long save(Long lessonId, Mark mark);
    Optional<Mark> findById(Long id);
    List<Mark> findByLessonId(Long lessonId);
    void deleteById(Long id);
    List<Mark> findTopNByOrderByTimestampDesc(int limit);
}
