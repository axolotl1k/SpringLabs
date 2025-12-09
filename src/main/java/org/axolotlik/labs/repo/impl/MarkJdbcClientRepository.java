package org.axolotlik.labs.repo.impl;

import org.axolotlik.labs.model.Mark;
import org.axolotlik.labs.repo.MarkRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class MarkJdbcClientRepository implements MarkRepository {

    private final JdbcClient jdbc;

    public MarkJdbcClientRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Long save(Long lessonId, Mark mark) {
        Map<String, Object> p = new HashMap<>();
        p.put("lessonId", lessonId);
        p.put("studentName", mark.getStudentName());
        p.put("grade", mark.getGrade());
        p.put("present", mark.isPresent());
        p.put("timestamp", mark.getTimestamp()); // значення для updated_at

        if (mark.getId() == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.sql("""
                INSERT INTO mark (lesson_id, student_name, grade, present, updated_at)
                VALUES (:lessonId, :studentName, :grade, :present, :timestamp)
            """).params(p).update(kh);
            Long id = Objects.requireNonNull(kh.getKey(), "No generated key").longValue();
            mark.setId(id);
            return id;
        } else {
            p.put("id", mark.getId());
            int n = jdbc.sql("""
                UPDATE mark
                   SET lesson_id    = :lessonId,
                       student_name = :studentName,
                       grade        = :grade,
                       present      = :present,
                       updated_at   = :timestamp
                 WHERE id = :id
            """).params(p).update();
            if (n == 0) throw new IllegalStateException("Mark " + mark.getId() + " not found");
            return mark.getId();
        }
    }

    @Override
    public Optional<Mark> findById(Long id) {
        return jdbc.sql("""
            SELECT id, lesson_id, student_name, grade, present,
                   updated_at AS timestamp
              FROM mark
             WHERE id = :id
        """).param("id", id).query(Mark.class).optional();
    }

    @Override
    public List<Mark> findByLessonId(Long lessonId) {
        return jdbc.sql("""
            SELECT id, lesson_id, student_name, grade, present,
                   updated_at AS timestamp
              FROM mark
             WHERE lesson_id = :lessonId
             ORDER BY student_name
        """).param("lessonId", lessonId).query(Mark.class).list();
    }

    @Override
    public void deleteById(Long id) {
        jdbc.sql("DELETE FROM mark WHERE id = ?")
                .param(id)
                .update();
    }

    @Override
    public List<Mark> findTopNByOrderByTimestampDesc(int limit) {
        return jdbc.sql("""
            SELECT id, lesson_id, student_name, grade, present,
                   updated_at AS timestamp
              FROM mark
             ORDER BY updated_at DESC
             LIMIT :limit
        """).param("limit", limit).query(Mark.class).list();
    }
}
