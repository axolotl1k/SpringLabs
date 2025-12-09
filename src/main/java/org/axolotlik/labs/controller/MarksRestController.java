package org.axolotlik.labs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.axolotlik.labs.dto.mark.CreateMarkRequest;
import org.axolotlik.labs.dto.mark.MarkDto;
import org.axolotlik.labs.dto.mark.UpdateMarkRequest;
import org.axolotlik.labs.model.Mark;
import org.axolotlik.labs.service.JournalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/lessons/{lessonId}/marks")
public class MarksRestController {

    private final JournalService service;

    public MarksRestController(JournalService service) {
        this.service = service;
    }

    // ===== GET LIST =====
    @Operation(summary = "Отримати всі відмітки для заняття")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MarkDto.class))))
    @GetMapping
    public ResponseEntity<List<MarkDto>> all(@PathVariable Long lessonId) {
        var list = service.getMarksForLesson(lessonId).stream().map(this::toDto).toList();
        return ResponseEntity.ok(list);
    }

    // ===== GET ONE =====
    @Operation(summary = "Отримати відмітку за ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Знайдено",
                    content = @Content(schema = @Schema(implementation = MarkDto.class))),
            @ApiResponse(responseCode = "404", description = "Не знайдено",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{markId}")
    public ResponseEntity<MarkDto> one(@PathVariable Long lessonId, @PathVariable Long markId) {
        Mark m = service.findMarkById(lessonId, markId);
        return (m == null)
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).build()
                : ResponseEntity.ok(toDto(m));
    }

    // ===== CREATE =====
    @Operation(summary = "Додати відмітку до заняття")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Створено",
                    content = @Content(schema = @Schema(implementation = MarkDto.class))),
            @ApiResponse(responseCode = "404", description = "Заняття не знайдено",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Некоректні дані",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping
    public ResponseEntity<MarkDto> create(@PathVariable Long lessonId, @RequestBody CreateMarkRequest req) {
        if (req == null || req.getStudentName() == null || req.getStudentName().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        // перевіримо, що lesson існує
        if (service.getLessonById(lessonId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Mark m = new Mark();
        m.setId(null);
        m.setLessonId(lessonId);
        m.setStudentName(req.getStudentName());
        m.setPresent(req.getPresent() != null ? req.getPresent() : true);
        m.setGrade(req.getGrade());
        m.setTimestamp(req.getTimestamp() != null ? req.getTimestamp() : LocalDateTime.now());

        service.addMark(lessonId, m); // сервіс/репо виставляє ID
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(m));
    }

    // ===== UPDATE (PUT) =====
    @Operation(summary = "Оновити відмітку")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Оновлено",
                    content = @Content(schema = @Schema(implementation = MarkDto.class))),
            @ApiResponse(responseCode = "404", description = "Не знайдено",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/{markId}")
    public ResponseEntity<MarkDto> update(@PathVariable Long lessonId,
                                          @PathVariable Long markId,
                                          @RequestBody UpdateMarkRequest req) {
        if (service.findMarkById(lessonId, markId) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Mark m = new Mark();
        m.setLessonId(lessonId);
        m.setStudentName(req.getStudentName());
        m.setPresent(req.getPresent() != null ? req.getPresent() : true);
        m.setGrade(req.getGrade());
        // timestamp оновлюємо у сервісі
        service.updateMark(lessonId, markId, m);

        Mark updated = service.findMarkById(lessonId, markId);
        return ResponseEntity.ok(toDto(updated));
    }

    // ===== DELETE =====
    @Operation(summary = "Видалити відмітку")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Видалено",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Не знайдено",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{markId}")
    public ResponseEntity<Void> delete(@PathVariable Long lessonId, @PathVariable Long markId) {
        if (service.findMarkById(lessonId, markId) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        service.deleteMark(lessonId, markId);
        return ResponseEntity.noContent().build();
    }

    // ===== Додаткові ендпоїнти для завдання по JPA-запитах =====

    @Operation(summary = "Список присутніх студентів по заняттю (JPQL @Query)")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MarkDto.class))))
    @GetMapping("/present")
    public ResponseEntity<List<MarkDto>> present(@PathVariable Long lessonId) {
        var list = service.findPresentMarks(lessonId).stream().map(this::toDto).toList();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Відмітки в діапазоні часу (NamedQuery)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MarkDto.class)))),
            @ApiResponse(responseCode = "400", description = "Некоректні параметри",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/range")
    public ResponseEntity<List<MarkDto>> range(
            @PathVariable Long lessonId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        if (from.isAfter(to)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        var list = service.findMarksInRangeNamed(lessonId, from, to).stream().map(this::toDto).toList();
        return ResponseEntity.ok(list);
    }

    // ===== MAPPER =====
    private MarkDto toDto(Mark m) {
        MarkDto dto = new MarkDto();
        dto.setId(m.getId());
        dto.setLessonId(m.getLessonId());
        dto.setStudentName(m.getStudentName());
        dto.setGrade(m.getGrade());
        dto.setPresent(m.isPresent());
        dto.setTimestamp(m.getTimestamp());
        return dto;
    }
}
