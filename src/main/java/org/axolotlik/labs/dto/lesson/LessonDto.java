package org.axolotlik.labs.dto.lesson;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(name = "LessonDto", description = "Lesson без вбудованих оцінок. За оцінками звертайся до /api/lessons/{id}/marks")
public class LessonDto {
    private Long id;
    private String subject;
    private LocalDate date;
    private String topic;

    @Schema(description = "Кількість відміток для уроку")
    private Integer marksCount;
}
