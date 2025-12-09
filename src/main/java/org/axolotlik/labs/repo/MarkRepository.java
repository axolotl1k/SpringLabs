package org.axolotlik.labs.repo;

import org.axolotlik.labs.model.Mark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MarkRepository extends CrudRepository<Mark, Long> {

    // те, що вже використовує сервіс
    List<Mark> findByLessonId(Long lessonId);

    Optional<Mark> findById(Long id);

    // для latestMarks(limit): Page + Pageable
    Page<Mark> findAllByOrderByTimestampDesc(Pageable pageable);

    // 5.1.1) @Query (JPQL): присутні оцінки по уроку
    @Query("""
           select m from Mark m
           where m.lessonId = :lessonId and m.present = true
           order by m.timestamp desc
           """)
    List<Mark> findPresentByLesson(@Param("lessonId") Long lessonId);

    // 5.1.2) @NamedQuery: діапазон дат по уроку
    // Працює, якщо у сутності Mark є @NamedQuery(name="Mark.findInRangeForLesson", ...)
    List<Mark> findInRangeForLesson(@Param("lessonId") Long lessonId,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);
}
