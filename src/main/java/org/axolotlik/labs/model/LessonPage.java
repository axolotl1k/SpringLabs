package org.axolotlik.labs.model;

import java.util.List;

/**
 * Модель сторінки уроків для повернення результатів
 * фільтрації та пагінації.
 */
public record LessonPage(
        List<Lesson> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
