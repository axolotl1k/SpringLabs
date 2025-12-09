package org.axolotlik.labs.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mark {
    private Long id;
    private Long lessonId;       // FK на lesson
    private String studentName;
    private Integer grade;
    private boolean present = true;
    private LocalDateTime timestamp;
}
