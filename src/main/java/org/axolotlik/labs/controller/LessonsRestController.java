package org.axolotlik.labs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.axolotlik.labs.dto.lesson.CreateLessonRequest;
import org.axolotlik.labs.dto.lesson.LessonDto;
import org.axolotlik.labs.dto.lesson.LessonPageDto;
import org.axolotlik.labs.dto.lesson.UpdateLessonRequest;
import org.axolotlik.labs.model.Lesson;
import org.axolotlik.labs.model.LessonPage;
import org.axolotlik.labs.service.JournalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/lessons")
public class LessonsRestController {

    private final JournalService journalService;

    public LessonsRestController(JournalService journalService) {
        this.journalService = journalService;
    }

    // ---------- LIST ----------
    @Operation(
            summary = "Отримати сторінку занять",
            description = "Фільтри: subject, dateFrom, dateTo. Повертає LessonDto без вбудованих оцінок."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успішно",
                    content = @Content(schema = @Schema(implementation = LessonPageDto.class)))
    })
    @GetMapping
    public ResponseEntity<LessonPageDto> getLessons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        LessonPage result = journalService.findLessons(subject, dateFrom, dateTo, page, size);
        List<LessonDto> dto = result.content().stream().map(this::toDto).toList();
        return ResponseEntity.ok(new LessonPageDto(dto, result.page(), result.size(), result.totalElements(), result.totalPages()));
    }

    // ---------- GET ONE ----------
    @Operation(summary = "Отримати заняття", description = "LessonDto без вбудованих оцінок.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Знайдено",
                    content = @Content(schema = @Schema(implementation = LessonDto.class))),
            @ApiResponse(responseCode = "404", description = "Не знайдено",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<LessonDto> getLesson(@PathVariable Long id) {
        Optional<Lesson> lessonOpt = journalService.getLessonById(id);
        return lessonOpt
                .map(l -> ResponseEntity.ok(toDto(l)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).<LessonDto>build());
    }

    // ---------- CREATE ----------
    @Operation(summary = "Створити нове заняття",
            description = "Приймає subject, topic (та опційно date). Оцінки створюються окремими запитами.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Створено",
                    content = @Content(schema = @Schema(implementation = LessonDto.class))),
            @ApiResponse(responseCode = "400", description = "Невірні вхідні дані",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping
    public ResponseEntity<LessonDto> createLesson(@RequestBody CreateLessonRequest req) {
        if (req.getSubject() == null || req.getSubject().isBlank()
            || req.getTopic() == null || req.getTopic().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).<LessonDto>build();
        }
        Lesson created = journalService.createLesson(req.getSubject(), req.getTopic());
        if (req.getDate() != null) {
            created.setDate(req.getDate());
            journalService.updateLesson(created.getId(), created.getSubject(), created.getTopic());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
    }

    // ---------- UPDATE (PUT) ----------
    @Operation(summary = "Повне оновлення заняття", description = "Оновлює subject та topic.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Оновлено",
                    content = @Content(schema = @Schema(implementation = LessonDto.class))),
            @ApiResponse(responseCode = "404", description = "Не знайдено",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<LessonDto> updateLesson(@PathVariable Long id, @RequestBody UpdateLessonRequest req) {
        Optional<Lesson> opt = journalService.getLessonById(id);
        if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).<LessonDto>build();

        journalService.updateLesson(id, req.getSubject(), req.getTopic());
        Lesson fresh = journalService.getLessonById(id).orElseThrow();
        return ResponseEntity.ok(toDto(fresh));
    }

    // ---------- DELETE ----------
    @Operation(summary = "Видалити заняття")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Видалено",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Не знайдено",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        if (journalService.getLessonById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        journalService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- PATCH ----------
    @Operation(summary = "Часткове оновлення (PATCH)",
            description = "Підтримуються ключі: subject, topic, date (yyyy-MM-dd).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Оновлено",
                    content = @Content(schema = @Schema(implementation = LessonDto.class))),
            @ApiResponse(responseCode = "404", description = "Не знайдено",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Невірні дані",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<LessonDto> patchLesson(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return journalService.patchLesson(id, updates)
                .map(l -> ResponseEntity.ok(toDto(l)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).<LessonDto>build());
    }

    // ===== mapper =====
    private LessonDto toDto(Lesson l) {
        int count = (l.getMarks() == null) ? 0 : l.getMarks().size();
        return new LessonDto(l.getId(), l.getSubject(), l.getDate(), l.getTopic(), count);
    }
}
