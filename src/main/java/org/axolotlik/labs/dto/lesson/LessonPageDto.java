package org.axolotlik.labs.dto.lesson;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "LessonPageDto")
public record LessonPageDto(
        List<LessonDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
