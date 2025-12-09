package org.axolotlik.labs.dto.mark;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UpdateMarkRequest")
public class UpdateMarkRequest {
    private String studentName;
    private Integer grade;
    private Boolean present;
}
