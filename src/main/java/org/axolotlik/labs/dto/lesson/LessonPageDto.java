package org.axolotlik.labs.dto.lesson;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(name = "LessonPageDto")
public class LessonPageDto {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private List<LessonDto> content;
}
