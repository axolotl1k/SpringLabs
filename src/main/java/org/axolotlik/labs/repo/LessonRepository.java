package org.axolotlik.labs.repo;

import org.axolotlik.labs.model.Lesson;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LessonRepository extends CrudRepository<Lesson, Long> {

    // зручно сервісу — одразу List
    List<Lesson> findAll();

    // 5.1.1) @Query (JPQL) — пошук з необов'язковими фільтрами
    @Query("""
           select l from Lesson l
           where (:subject is null or lower(l.subject) like lower(concat('%', :subject, '%')))
             and (:from is null or l.date >= :from)
             and (:to   is null or l.date <= :to)
           order by l.date desc, l.id desc
           """)
    List<Lesson> search(
            @Param("subject") String subject,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // 5.1.2) @NamedQuery — ім'я = "Lesson.findByTopicPattern"
    // Spring Data підхопить наявний NamedQuery за назвою методу
    List<Lesson> findByTopicPattern(@Param("pattern") String pattern);

    // 5.2) Derived query — генерується за назвою методу
    List<Lesson> findBySubjectContainingIgnoreCaseAndDateBetween(
            String subject, LocalDate from, LocalDate to
    );
}
