package org.axolotlik.labs.model;

import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {
    private Long id;
    private String subject;
    private String topic;
    // у БД колонка lesson_date
    private LocalDate date;

    @Builder.Default
    private List<Mark> marks = new ArrayList<>();
}
