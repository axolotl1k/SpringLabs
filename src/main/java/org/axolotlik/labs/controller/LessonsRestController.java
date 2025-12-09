package org.axolotlik.labs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

    private final JournalService service;

    public LessonsRestController(JournalService service) {
        this.service = service;
    }

    // ===== LIST + FILTER + PAGE =====
    @Operation(
            summary = "Отримати список занять (з фільтрами та пагінацією)",
            description = "Фільтри: subject, dateFrom/dateTo; параметри сторінки: page, size."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Сторінка занять",
            content = @Content(schema = @Schema(implementation = LessonPageDto.class))
    )
    @GetMapping
    public ResponseEntity<LessonPageDto> list(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        LessonPage p = service.findLessons(subject, dateFrom, dateTo, page, size);
        return ResponseEntity.ok(toDto(p));
    }

    // ===== GET ONE =====
    @Operation(summary = "Отримати заняття за ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Знайдено",
                    content = @Content(schema = @Schema(implementation = LessonDto.class))),
            @ApiResponse(responseCode = "404", description = "Не знайдено",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<LessonDto> one(@PathVariable Long id) {
        Optional<Lesson> lessonOpt = service.getLessonById(id);
        return lessonOpt
                .map(l -> ResponseEntity.ok(toDto(l)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // ===== CREATE =====
    @Operation(summary = "Створити нове заняття")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Створено",
                    content = @Content(schema = @Schema(implementation = LessonDto.class))),
            @ApiResponse(responseCode = "400", description = "Некоректні дані",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping
    public ResponseEntity<LessonDto> create(@RequestBody CreateLessonRequest req) {
        if (req == null || req.getSubject() == null || req.getSubject().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Lesson created = service.createLesson(req.getSubject(), req.getTopic());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
    }

    // ===== UPDATE (PUT) =====
    @Operation(summary = "Оновити заняття (повне оновлення)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Оновлено",
                    content = @Content(schema = @Schema(implementation = LessonDto.class))),
            @ApiResponse(responseCode = "404", description = "Не знайдено",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<LessonDto> update(@PathVariable Long id, @RequestBody UpdateLessonRequest req) {
        if (service.getLessonById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        service.updateLesson(id, req.getSubject(), req.getTopic());
        return service.getLessonById(id)
                .map(l -> ResponseEntity.ok(toDto(l)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // ===== PATCH =====
    @Operation(summary = "Часткове оновлення заняття (PATCH)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Оновлено",
                    content = @Content(schema = @Schema(implementation = LessonDto.class))),
            @ApiResponse(responseCode = "404", description = "Не знайдено",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<LessonDto> patch(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return service.patchLesson(id, updates)
                .map(l -> ResponseEntity.ok(toDto(l)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // ===== DELETE =====
    @Operation(summary = "Видалити заняття")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Видалено",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Не знайдено",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.getLessonById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        service.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    // ===== JPA-запити, які Є в сервісі =====

    // JPQL @Query із фільтрами
    @Operation(summary = "Пошук занять (JPQL @Query + фільтри)")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = LessonDto.class))))
    @GetMapping("/search")
    public ResponseEntity<List<LessonDto>> searchByQuery(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<Lesson> list = service.searchLessonsByQuery(subject, from, to);
        return ResponseEntity.ok(list.stream().map(this::toDto).toList());
    }

    // NamedQuery по темі (pattern)
    @Operation(summary = "Пошук занять за темою (NamedQuery)")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = LessonDto.class))))
    @GetMapping("/search/by-topic")
    public ResponseEntity<List<LessonDto>> searchByTopicNamed(@RequestParam String pattern) {
        List<Lesson> list = service.searchLessonsByTopicNamed(pattern);
        return ResponseEntity.ok(list.stream().map(this::toDto).toList());
    }

    // ===== MAPPERS =====
    private LessonDto toDto(Lesson l) {
        LessonDto dto = new LessonDto();
        dto.setId(l.getId());
        dto.setSubject(l.getSubject());
        dto.setTopic(l.getTopic());
        dto.setDate(l.getDate());
        int count = (l.getMarks() != null) ? l.getMarks().size()
                : service.getMarksForLesson(l.getId()).size();
        dto.setMarksCount(count);
        return dto;
    }

    private LessonPageDto toDto(LessonPage p) {
        LessonPageDto dto = new LessonPageDto();
        dto.setPage(p.page());
        dto.setSize(p.size());
        dto.setTotalElements(p.totalElements());
        dto.setTotalPages(p.totalPages());
        dto.setContent(p.content().stream().map(this::toDto).toList());
        return dto;
    }
}
