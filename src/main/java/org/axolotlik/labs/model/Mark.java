package org.axolotlik.labs.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mark")
@NamedQueries({
        // 5.1.2) @NamedQuery
        @NamedQuery(
                name = "Mark.findInRangeForLesson",
                query = """
                select m from Mark m
                where m.lessonId = :lessonId
                  and m.timestamp between :from and :to
                order by m.timestamp desc
                """
        )
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Mark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @JsonIgnore // щоб не міняти JSON і не зловити рекурсію при серіалізації
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", insertable = false, updatable = false)
    private Lesson lesson;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    private Integer grade;

    @Column(nullable = false)
    private boolean present;

    // у БД: updated_at
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime timestamp;

    // зручний сеттер: тримає lessonId і lesson в sync
    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
        this.lessonId = (lesson != null ? lesson.getId() : null);
    }
}
