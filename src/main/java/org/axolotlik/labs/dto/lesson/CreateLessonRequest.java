package org.axolotlik.labs.dto.lesson;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(name = "CreateLessonRequest")
public class CreateLessonRequest {
    private String subject;
    private String topic;
    @Schema(description = "Дата уроку; якщо не передати — буде today")
    private LocalDate date;
}

