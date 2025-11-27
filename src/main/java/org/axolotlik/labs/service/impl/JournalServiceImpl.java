package org.axolotlik.labs.service.impl;

import org.axolotlik.labs.model.Lesson;
import org.axolotlik.labs.model.LessonPage;
import org.axolotlik.labs.model.Mark;
import org.axolotlik.labs.repo.JournalRepository;
import org.axolotlik.labs.service.JournalService;
import org.axolotlik.labs.service.NotificationService;
import org.axolotlik.labs.util.IdGenerator;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Реалізація сервісу журналу:
 * - робота з уроками (Lesson)
 * - робота з відмітками (Mark)
 * - фільтрація, пагінація, PATCH для ЛР4
 */
@Service
public class JournalServiceImpl implements JournalService {

    private final JournalRepository repository;
    private final ObjectFactory<IdGenerator> idGeneratorFactory;

    private NotificationService notificationService;

    @Autowired
    public JournalServiceImpl(JournalRepository repository,
                              ObjectFactory<IdGenerator> idGeneratorFactory) {
        this.repository = repository;
        this.idGeneratorFactory = idGeneratorFactory;
    }

    // Демонстрація інʼєкції через сеттер
    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ==================== УРОКИ (Lesson) ====================

    @Override
    public List<Lesson> getAllLessons() {
        return repository.findAll();
    }

    @Override
    public Optional<Lesson> getLessonById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Lesson createLesson(String subject, String topic) {
        // Для кожного нового Lesson створюємо власний генератор для Marks
        IdGenerator markGen = idGeneratorFactory.getObject();
        Lesson lesson = new Lesson(null, subject, topic, markGen);
        lesson.setDate(LocalDate.now());

        Lesson saved = repository.save(lesson);

        if (notificationService != null) {
            notificationService.notify("Створено новий урок з предмету: " + subject);
        }
        return saved;
    }

    @Override
    public void updateLesson(Long lessonId, String newSubject, String newTopic) {
        Optional<Lesson> lessonOpt = repository.findById(lessonId);
        if (lessonOpt.isEmpty()) {
            return;
        }

        Lesson lesson = lessonOpt.get();
        lesson.setSubject(newSubject);
        lesson.setTopic(newTopic);
        repository.save(lesson);

        if (notificationService != null) {
            notificationService.notify("Оновлено урок з предмету: " + newSubject);
        }
    }

    @Override
    public void deleteLesson(Long lessonId) {
        Optional<Lesson> lessonOpt = repository.findById(lessonId);
        if (lessonOpt.isEmpty()) {
            return;
        }

        repository.deleteById(lessonId);

        if (notificationService != null) {
            notificationService.notify("Видалено урок з id = " + lessonId);
        }
    }

    // ==================== ВІДМІТКИ (Mark) ====================

    @Override
    public void addMark(Long lessonId, Mark mark) {
        Optional<Lesson> lessonOpt = repository.findById(lessonId);
        if (lessonOpt.isEmpty()) {
            return;
        }

        Lesson lesson = lessonOpt.get();
        // Lesson сам виставляє id/timestamp/grade через addMark(...)
        lesson.addMark(mark);
        repository.save(lesson);

        if (notificationService != null) {
            notificationService.notify("Додано відмітку для " + mark.getStudentName());
        }
    }

    @Override
    public Mark findMarkById(Long lessonId, Long markId) {
        Optional<Lesson> lessonOpt = repository.findById(lessonId);
        if (lessonOpt.isEmpty()) {
            return null;
        }

        Lesson lesson = lessonOpt.get();
        return lesson.findMarkById(markId).orElse(null);
    }

    @Override
    public void updateMark(Long lessonId, Long markId, Mark updatedMark) {
        Optional<Lesson> lessonOpt = repository.findById(lessonId);
        if (lessonOpt.isEmpty()) {
            return;
        }

        Lesson lesson = lessonOpt.get();

        updatedMark.setId(markId);
        updatedMark.setTimestamp(LocalDateTime.now());

        lesson.updateMark(updatedMark);
        repository.save(lesson);

        if (notificationService != null) {
            notificationService.notify("Оновлено відмітку для " + updatedMark.getStudentName());
        }
    }

    @Override
    public void deleteMark(Long lessonId, Long markId) {
        Optional<Lesson> lessonOpt = repository.findById(lessonId);
        if (lessonOpt.isEmpty()) {
            return;
        }

        Lesson lesson = lessonOpt.get();

        String studentName = lesson.findMarkById(markId)
                .map(Mark::getStudentName)
                .orElse("невідомого студента");

        lesson.deleteMarkById(markId);
        repository.save(lesson);

        if (notificationService != null) {
            notificationService.notify("Видалено відмітку (id = "
                    + markId + ") для " + studentName);
        }
    }

    // ==================== ФІЛЬТРАЦІЯ + ПАГІНАЦІЯ ====================

    @Override
    public LessonPage findLessons(String subject,
                                  LocalDate dateFrom,
                                  LocalDate dateTo,
                                  int page,
                                  int size) {

        List<Lesson> all = repository.findAll();
        var stream = all.stream();

        if (subject != null && !subject.isBlank()) {
            String s = subject.toLowerCase();
            stream = stream.filter(l ->
                    l.getSubject() != null &&
                            l.getSubject().toLowerCase().contains(s));
        }

        if (dateFrom != null) {
            stream = stream.filter(l ->
                    l.getDate() != null && !l.getDate().isBefore(dateFrom));
        }

        if (dateTo != null) {
            stream = stream.filter(l ->
                    l.getDate() != null && !l.getDate().isAfter(dateTo));
        }

        List<Lesson> filtered = stream.toList();
        long totalElements = filtered.size();

        if (size <= 0) size = 10;
        if (page < 0) page = 0;

        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());
        List<Lesson> content = filtered.subList(fromIndex, toIndex);

        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new LessonPage(content, page, size, totalElements, totalPages);
    }

    // ==================== PATCH (часткове оновлення) ====================

    @Override
    public Optional<Lesson> patchLesson(Long id, Map<String, Object> updates) {
        Optional<Lesson> opt = repository.findById(id);
        if (opt.isEmpty()) {
            return Optional.empty();
        }

        Lesson lesson = opt.get();

        if (updates.containsKey("subject")) {
            Object value = updates.get("subject");
            if (value instanceof String s) {
                lesson.setSubject(s);
            }
        }

        if (updates.containsKey("topic")) {
            Object value = updates.get("topic");
            if (value instanceof String s) {
                lesson.setTopic(s);
            }
        }

        if (updates.containsKey("date")) {
            Object value = updates.get("date");
            if (value instanceof String s) {
                LocalDate parsed = LocalDate.parse(s);
                lesson.setDate(parsed);
            }
        }

        Lesson saved = repository.save(lesson);
        return Optional.of(saved);
    }
}
