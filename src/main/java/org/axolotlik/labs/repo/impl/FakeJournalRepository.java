package org.axolotlik.labs.repo.impl;

import org.axolotlik.labs.model.Lesson;
import org.axolotlik.labs.model.Mark;
import org.axolotlik.labs.repo.JournalRepository;
import org.axolotlik.labs.util.IdGenerator;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Repository
public class FakeJournalRepository implements JournalRepository {

    private final Map<Long, Lesson> journalStorage = new ConcurrentHashMap<>();

    // Генератор ID для 'Lesson'
    private final IdGenerator lessonIdGenerator;

    // Фабрика для створення генераторів для 'Mark'
    private final ObjectFactory<IdGenerator> markIdGenFactory;

    // Демонстрація ін'єкції через конструктор
    @Autowired
    public FakeJournalRepository(ObjectFactory<IdGenerator> idGenFactory) {
        this.lessonIdGenerator = idGenFactory.getObject();
        this.markIdGenFactory = idGenFactory;
    }

    @PostConstruct
    public void init() {
        // Створюємо Lesson 1
        IdGenerator markGen1 = markIdGenFactory.getObject();
        Lesson lesson1 = new Lesson(lessonIdGenerator.getNextId(), "Математика", "Інтеграли", markGen1);

        lesson1.addMark(new Mark("Іванов", 10, true));
        lesson1.addMark(new Mark("Петров", 8, true));
        journalStorage.put(lesson1.getId(), lesson1);

        // Створюємо Lesson 2
        IdGenerator markGen2 = markIdGenFactory.getObject();
        Lesson lesson2 = new Lesson(lessonIdGenerator.getNextId(), "Фізика", "Термодинаміка", markGen2);

        lesson2.addMark(new Mark("Іванов", 9, true));
        lesson2.addMark(new Mark("Сидоров", null, false));
        journalStorage.put(lesson2.getId(), lesson2);
    }

    @Override
    public List<Lesson> findAll() {
        return new ArrayList<>(journalStorage.values());
    }

    @Override
    public Optional<Lesson> findById(Long id) {
        return Optional.ofNullable(journalStorage.get(id));
    }

    @Override
    public Lesson save(Lesson lesson) {
        if (lesson.getId() == null) {
            lesson.setId(lessonIdGenerator.getNextId());
        }
        journalStorage.put(lesson.getId(), lesson);
        return lesson;
    }

    @Override
    public void deleteById(Long lessonId) {
        journalStorage.remove(lessonId);
    }
}