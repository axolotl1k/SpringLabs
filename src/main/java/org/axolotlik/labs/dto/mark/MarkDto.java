package org.axolotlik.labs.dto.mark;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(name = "MarkDto")
public class MarkDto {
    private Long id;
    private Long lessonId;
    private String studentName;
    private Integer grade;
    private boolean present;
    private LocalDateTime timestamp;
}
