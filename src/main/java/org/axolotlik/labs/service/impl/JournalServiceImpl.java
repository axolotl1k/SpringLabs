package org.axolotlik.labs.service.impl;

import org.axolotlik.labs.model.Lesson;
import org.axolotlik.labs.model.Mark;
import org.axolotlik.labs.repo.JournalRepository;
import org.axolotlik.labs.service.JournalService;
import org.axolotlik.labs.service.NotificationService;
import org.axolotlik.labs.util.IdGenerator;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JournalServiceImpl implements JournalService {

    private final JournalRepository repository;
    private final ObjectFactory<IdGenerator> idGeneratorFactory;

    private NotificationService notificationService;

    @Autowired
    public JournalServiceImpl(JournalRepository repository, ObjectFactory<IdGenerator> idGeneratorFactory) {
        this.repository = repository;
        this.idGeneratorFactory = idGeneratorFactory;
    }

    // Демонстрація ін'єкції через сетер
    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // --- Методи для Lesson ---

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
        IdGenerator markIdGen = idGeneratorFactory.getObject();
        Lesson newLesson = new Lesson(null, subject, topic, markIdGen);
        Lesson savedLesson = repository.save(newLesson); // Зберігаємо, щоб отримати фінальний об'єкт

        if (notificationService != null) {
            notificationService.notify("Створено нове заняття: " + savedLesson.getSubject());
        }
        return savedLesson;
    }

    @Override
    public void deleteLesson(Long lessonId) {
        // Треба отримати дані ДО видалення для нотифікації
        Optional<Lesson> lessonOpt = repository.findById(lessonId);
        if (lessonOpt.isPresent()) {
            String subject = lessonOpt.get().getSubject();
            repository.deleteById(lessonId);

            if (notificationService != null) {
                notificationService.notify("Видалено заняття: " + subject);
            }
        }
    }

    @Override
    public void updateLesson(Long lessonId, String newSubject, String newTopic) {
        Optional<Lesson> lessonOpt = repository.findById(lessonId);
        if (lessonOpt.isPresent()) {
            Lesson lesson = lessonOpt.get();
            lesson.setSubject(newSubject);
            lesson.setTopic(newTopic);
            repository.save(lesson);

            if (notificationService != null) {
                notificationService.notify("Оновлено заняття: " + newSubject);
            }
        }
    }

    // --- Методи для Mark ---

    @Override
    public void addMark(Long lessonId, Mark mark) {
        Optional<Lesson> lessonOpt = repository.findById(lessonId);
        if (lessonOpt.isPresent()) {
            Lesson lesson = lessonOpt.get();
            lesson.addMark(mark);
            repository.save(lesson);

            if (notificationService != null) {
                // 'mark' вже має ім'я з форми
                notificationService.notify("Додано нову відмітку для " + mark.getStudentName());
            }
        }
    }

    @Override
    public void deleteMark(Long lessonId, Long markId) {
        Optional<Lesson> lessonOpt = repository.findById(lessonId);
        if (lessonOpt.isPresent()) {
            Lesson lesson = lessonOpt.get();


            String studentName = lesson.findMarkById(markId)
                    .map(Mark::getStudentName)
                    .orElse("невідомого студента");

            lesson.deleteMarkById(markId);
            repository.save(lesson);

            if (notificationService != null) {
                notificationService.notify("Видалено відмітку для " + studentName);
            }
        }
    }

    @Override
    public Mark findMarkById(Long lessonId, Long markId) {
        Lesson lesson = repository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        return lesson.findMarkById(markId)
                .orElseThrow(() -> new RuntimeException("Mark not found"));
    }

    @Override
    public void updateMark(Long lessonId, Long markId, Mark updatedMark) {
        Optional<Lesson> lessonOpt = repository.findById(lessonId);
        if (lessonOpt.isPresent()) {
            Lesson lesson = lessonOpt.get();
            updatedMark.setId(markId);
            lesson.updateMark(updatedMark);
            repository.save(lesson);

            if (notificationService != null) {
                notificationService.notify("Оновлено відмітку для " + updatedMark.getStudentName());
            }
        }
    }
}