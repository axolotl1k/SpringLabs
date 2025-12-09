package org.axolotlik.labs.dto.lesson;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UpdateLessonRequest")
public class UpdateLessonRequest {
    private String subject;
    private String topic;
}
