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

    // ===== LIST =====
    @Operation(summary = "Список відміток уроку")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ок",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MarkDto.class)))),
            @ApiResponse(responseCode = "404", description = "Урок не знайдено",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping
    public ResponseEntity<List<MarkDto>> list(@PathVariable Long lessonId) {
        return service.getLessonById(lessonId)
                .map(l -> {
                    var dto = service.getMarksForLesson(lessonId).stream().map(this::toDto).toList();
                    return ResponseEntity.ok(dto);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).<List<MarkDto>>build());
    }

    // ===== GET ONE =====
    @Operation(summary = "Отримати відмітку")
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
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).<MarkDto>build()
                : ResponseEntity.ok(toDto(m));
    }

    // ===== CREATE =====
    @Operation(summary = "Створити відмітку", description = "Ігнорує id у тілі запиту.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Створено",
                    content = @Content(schema = @Schema(implementation = MarkDto.class))),
            @ApiResponse(responseCode = "404", description = "Урок не знайдено",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Невірні дані",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping
    public ResponseEntity<MarkDto> create(@PathVariable Long lessonId,
                                          @RequestBody CreateMarkRequest req) {
        return service.getLessonById(lessonId).map(l -> {
            if (req.getStudentName() == null || req.getStudentName().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).<MarkDto>build();
            }
            Mark m = new Mark();
            m.setId(null);
            m.setLessonId(lessonId);
            m.setStudentName(req.getStudentName());
            m.setGrade(req.getGrade());
            m.setPresent(req.getPresent() == null || req.getPresent());
            m.setTimestamp(req.getTimestamp() == null ? LocalDateTime.now() : req.getTimestamp());

            service.addMark(lessonId, m);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(m));
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).<MarkDto>build());
    }

    // ===== UPDATE (PUT) =====
    @Operation(summary = "Оновити відмітку", description = "Повне оновлення. Якщо не існує — 404.")
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).<MarkDto>build();
        }
        Mark m = new Mark();
        m.setStudentName(req.getStudentName());
        m.setGrade(req.getGrade());
        if (req.getPresent() != null) m.setPresent(req.getPresent());
        service.updateMark(lessonId, markId, m);

        Mark fresh = service.findMarkById(lessonId, markId);
        return ResponseEntity.ok(toDto(fresh));
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

    // ===== mapper =====
    private MarkDto toDto(Mark m) {
        return new MarkDto(
                m.getId(),
                m.getLessonId(),
                m.getStudentName(),
                m.getGrade(),
                m.isPresent(),
                m.getTimestamp()
        );
    }
}
