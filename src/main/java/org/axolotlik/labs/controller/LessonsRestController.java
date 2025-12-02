package org.axolotlik.labs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.axolotlik.labs.model.Lesson;
import org.axolotlik.labs.model.LessonPage;
import org.axolotlik.labs.service.JournalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * REST-контролер для роботи з уроками (журналом).
 * Реалізує:
 *  - CRUD
 *  - фільтрацію та пагінацію
 *  - часткове оновлення (PATCH)
 *  - коректні коди стану HTTP
 */
@RestController
@RequestMapping("/api/lessons")
public class LessonsRestController {

    private final JournalService journalService;

    public LessonsRestController(JournalService journalService) {
        this.journalService = journalService;
    }

    // ---------- READ: список з фільтрацією та пагінацією ----------

    @Operation(
            summary = "Отримати сторінку занять",
            description = "Повертає список занять з можливістю фільтрації за предметом "
                    + "та діапазоном дат, а також пагінацією результатів."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успішне отримання списку занять",
                    content = @Content(schema = @Schema(implementation = LessonPage.class))
            )
    })
    @GetMapping
    public ResponseEntity<LessonPage> getLessons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        LessonPage result = journalService.findLessons(subject, dateFrom, dateTo, page, size);
        return ResponseEntity.ok(result);
    }

    // ---------- READ: одне заняття ----------

    @Operation(
            summary = "Отримати заняття за ідентифікатором",
            description = "Повертає одне заняття за його унікальним ідентифікатором."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Заняття знайдено",
                    content = @Content(schema = @Schema(implementation = Lesson.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заняття не знайдено"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Lesson> getLesson(@PathVariable Long id) {
        Optional<Lesson> lessonOpt = journalService.getLessonById(id);
        return lessonOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // ---------- CREATE ----------

    @Operation(
            summary = "Створити нове заняття",
            description = "Створює новий ресурс Lesson на основі переданих даних (предмет та тема)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Заняття успішно створене",
                    content = @Content(schema = @Schema(implementation = Lesson.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невірні вхідні дані"
            )
    })
    @PostMapping
    public ResponseEntity<Lesson> createLesson(@RequestBody Lesson request) {
        if (request.getSubject() == null || request.getSubject().isBlank()
                || request.getTopic() == null || request.getTopic().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Lesson created = journalService.createLesson(request.getSubject(), request.getTopic());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ---------- UPDATE (PUT) ----------

    @Operation(
            summary = "Повне оновлення заняття",
            description = "Оновлює базові поля заняття (предмет, тема). "
                    + "Якщо заняття не знайдено — повертається 404."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Заняття успішно оновлене",
                    content = @Content(schema = @Schema(implementation = Lesson.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заняття не знайдено"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<Lesson> updateLesson(
            @PathVariable Long id,
            @RequestBody Lesson request
    ) {
        Optional<Lesson> existingOpt = journalService.getLessonById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        journalService.updateLesson(id, request.getSubject(), request.getTopic());
        Lesson updated = journalService.getLessonById(id).orElse(existingOpt.get());
        return ResponseEntity.ok(updated);
    }

    // ---------- PARTIAL UPDATE (PATCH) ----------

    @Operation(
            summary = "Часткове оновлення заняття (PATCH)",
            description = "Оновлює лише ті поля заняття, які передані у тілі запиту. "
                    + "Підхід подібний до JSON Merge Patch (RFC 7386)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Заняття успішно оновлене",
                    content = @Content(schema = @Schema(implementation = Lesson.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заняття не знайдено"
            )
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Lesson> patchLesson(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates
    ) {
        Optional<Lesson> updatedOpt = journalService.patchLesson(id, updates);
        return updatedOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // ---------- DELETE ----------

    @Operation(
            summary = "Видалити заняття",
            description = "Видаляє заняття за його ідентифікатором. "
                    + "Якщо операція успішна — повертається код 204 (No Content)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Заняття успішно видалене"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заняття не знайдено"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        Optional<Lesson> lessonOpt = journalService.getLessonById(id);
        if (lessonOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        journalService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }
}
