package org.axolotlik.labs.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axolotlik.labs.util.IdGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
public class Lesson {
    private Long id;
    private String subject;
    private LocalDate date;
    private String topic;
    private List<Mark> marks = new ArrayList<>();

    // 'Lesson' володіє власним генератором ID для своїх 'Marks'
    private IdGenerator markIdGenerator;

    public Lesson(Long id, String subject, String topic, IdGenerator markIdGenerator) {
        this.id = id;
        this.subject = subject;
        this.topic = topic;
        this.date = LocalDate.now();
        this.markIdGenerator = markIdGenerator;
    }

    // --- Методи для керування 'Marks' ---
    // Це дозволяє тримати логіку інкапсульованою тут,
    // а не в сервісі.

    public void addMark(Mark mark) {
        mark.setId(this.markIdGenerator.getNextId());

        if (!mark.isPresent()) {
            mark.setGrade(null);
        }
        mark.setTimestamp(LocalDateTime.now());
        this.marks.add(mark);
    }

    public Optional<Mark> findMarkById(Long markId) {
        return this.marks.stream()
                .filter(m -> m.getId().equals(markId))
                .findFirst();
    }

    public void deleteMarkById(Long markId) {
        this.marks.removeIf(m -> m.getId().equals(markId));
    }

    public void updateMark(Mark updatedMark) {
        if (updatedMark.getId() == null) {
            return;
        }

        Optional<Mark> originalMarkOpt = findMarkById(updatedMark.getId());

        if (originalMarkOpt.isPresent()) {
            Mark originalMark = originalMarkOpt.get();

            if (!updatedMark.isPresent()) {
                updatedMark.setGrade(null);
            }

            originalMark.setStudentName(updatedMark.getStudentName());
            originalMark.setPresent(updatedMark.isPresent());
            originalMark.setGrade(updatedMark.getGrade());
        }
    }
}