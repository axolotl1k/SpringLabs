package org.axolotlik.labs.repo.impl;

import org.axolotlik.labs.model.Lesson;
import org.axolotlik.labs.repo.LessonRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
public class LessonJdbcTemplateRepository implements LessonRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Lesson> MAPPER = (rs, n) -> Lesson.builder()
            .id(rs.getLong("id"))
            .subject(rs.getString("subject"))
            .topic(rs.getString("topic"))
            .date(rs.getDate("lesson_date").toLocalDate())
            .build();

    public LessonJdbcTemplateRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Long save(Lesson lesson) {
        if (lesson.getId() == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO lesson (subject, topic, lesson_date) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, lesson.getSubject());
                ps.setString(2, lesson.getTopic());
                ps.setDate(3, java.sql.Date.valueOf(lesson.getDate()));
                return ps;
            }, kh);
            Long id = Objects.requireNonNull(kh.getKey(), "No generated key").longValue();
            lesson.setId(id);
            return id;
        } else {
            jdbc.update("""
                UPDATE lesson
                   SET subject = ?, topic = ?, lesson_date = ?
                 WHERE id = ?
            """, lesson.getSubject(), lesson.getTopic(),
                    java.sql.Date.valueOf(lesson.getDate()), lesson.getId());
            return lesson.getId();
        }
    }

    @Override
    public int update(Lesson lesson) {
        return jdbc.update("""
            UPDATE lesson
               SET subject = ?, topic = ?, lesson_date = ?
             WHERE id = ?
        """, lesson.getSubject(), lesson.getTopic(),
                java.sql.Date.valueOf(lesson.getDate()), lesson.getId());
    }

    @Override
    public Optional<Lesson> findById(Long id) {
        List<Lesson> list = jdbc.query("""
            SELECT id, subject, topic, lesson_date
              FROM lesson
             WHERE id = ?
        """, MAPPER, id);
        return list.stream().findFirst();
    }

    @Override
    public List<Lesson> findAll() {
        return jdbc.query("""
            SELECT id, subject, topic, lesson_date
              FROM lesson
             ORDER BY lesson_date DESC, id DESC
        """, MAPPER);
    }

    @Override
    public void deleteById(Long id) {
        jdbc.update("DELETE FROM lesson WHERE id = ?", id);
    }
}
