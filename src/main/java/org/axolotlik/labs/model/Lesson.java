package org.axolotlik.labs.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lesson")
@NamedQueries({
        // 5.1.2) @NamedQuery
        @NamedQuery(
                name = "Lesson.findByTopicPattern",
                query = """
                select l from Lesson l
                where lower(l.topic) like lower(concat('%', :pattern, '%'))
                order by l.date desc, l.id desc
                """
        )
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subject;

    @Column
    private String topic;

    // у БД: lesson_date
    @Column(name = "lesson_date", nullable = false)
    private LocalDate date;

    @JsonIgnore // щоб не міняти існуючі JSON-відповіді і уникнути рекурсії
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY)
    private List<Mark> marks = new ArrayList<>();
}
