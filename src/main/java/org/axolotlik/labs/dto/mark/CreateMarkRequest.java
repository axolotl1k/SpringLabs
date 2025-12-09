package org.axolotlik.labs.dto.mark;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(name = "CreateMarkRequest")
public class CreateMarkRequest {
    private String studentName;
    private Integer grade;          // optional
    private Boolean present;        // optional, default true
    private LocalDateTime timestamp; // optional, default now
}
