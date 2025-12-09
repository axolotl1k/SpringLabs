package org.axolotlik.labs.service.impl;

import org.axolotlik.labs.model.Lesson;
import org.axolotlik.labs.model.LessonPage;
import org.axolotlik.labs.model.Mark;
import org.axolotlik.labs.repo.LessonRepository;
import org.axolotlik.labs.repo.MarkRepository;
import org.axolotlik.labs.service.JournalService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class JournalServiceImpl implements JournalService {

    private final LessonRepository lessonRepo;
    private final MarkRepository markRepo;

    public JournalServiceImpl(LessonRepository lessonRepo, MarkRepository markRepo) {
        this.lessonRepo = lessonRepo;
        this.markRepo = markRepo;
    }

    // ===== LESSONS =====

    @Override
    public List<Lesson> getAllLessons() {
        var lessons = lessonRepo.findAll();
        for (var l : lessons) {
            l.setMarks(markRepo.findByLessonId(l.getId()));
        }
        return lessons;
    }

    @Override
    public Optional<Lesson> getLessonById(Long id) {
        var opt = lessonRepo.findById(id);
        opt.ifPresent(l -> l.setMarks(markRepo.findByLessonId(l.getId())));
        return opt;
    }

    @Override
    @Transactional
    public Lesson createLesson(String subject, String topic) {
        Lesson l = new Lesson();
        l.setSubject(subject);
        l.setTopic(topic);
        l.setDate(LocalDate.now());
        return lessonRepo.save(l);
    }

    @Override
    @Transactional
    public void updateLesson(Long lessonId, String newSubject, String newTopic) {
        var opt = lessonRepo.findById(lessonId);
        if (opt.isEmpty()) return;
        Lesson l = opt.get();
        if (newSubject != null) l.setSubject(newSubject);
        if (newTopic != null) l.setTopic(newTopic);
        lessonRepo.save(l);
    }

    @Override
    @Transactional
    public void deleteLesson(Long lessonId) {
        lessonRepo.deleteById(lessonId);
    }

    @Override
    public LessonPage findLessons(String subject, LocalDate dateFrom, LocalDate dateTo, int page, int size) {
        List<Lesson> all = lessonRepo.findAll();

        var stream = all.stream();
        if (subject != null && !subject.isBlank()) {
            String s = subject.toLowerCase();
            stream = stream.filter(l -> l.getSubject() != null && l.getSubject().toLowerCase().contains(s));
        }
        if (dateFrom != null) stream = stream.filter(l -> l.getDate() != null && !l.getDate().isBefore(dateFrom));
        if (dateTo != null)   stream = stream.filter(l -> l.getDate() != null && !l.getDate().isAfter(dateTo));

        List<Lesson> filtered = stream.toList();
        long total = filtered.size();
        if (size <= 0) size = 10;
        if (page < 0) page = 0;

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<Lesson> content = filtered.subList(from, to);
        int pages = (int) Math.ceil((double) total / size);

        return new LessonPage(content, page, size, total, pages);
    }

    @Override
    @Transactional
    public Optional<Lesson> patchLesson(Long id, Map<String, Object> updates) {
        var opt = lessonRepo.findById(id);
        if (opt.isEmpty()) return Optional.empty();
        Lesson l = opt.get();

        if (updates.containsKey("subject") && updates.get("subject") instanceof String s) l.setSubject(s);
        if (updates.containsKey("topic") && updates.get("topic") instanceof String s)   l.setTopic(s);
        if (updates.containsKey("date") && updates.get("date") instanceof String s)    l.setDate(LocalDate.parse(s));

        lessonRepo.save(l);
        return Optional.of(l);
    }

    // ===== MARKS =====

    @Override
    public List<Mark> getMarksForLesson(Long lessonId) {
        return markRepo.findByLessonId(lessonId);
    }

    @Override
    public Mark findMarkById(Long lessonId, Long markId) {
        return markRepo.findById(markId)
                .filter(m -> Objects.equals(m.getLessonId(), lessonId))
                .orElse(null);
    }

    @Override
    @Transactional
    public void addMark(Long lessonId, Mark mark) {
        if (mark.getTimestamp() == null) mark.setTimestamp(LocalDateTime.now());
        if (mark.isPresent() && mark.getGrade() == null) mark.setGrade(0);
        mark.setLessonId(lessonId);
        Mark saved = markRepo.save(mark);
        mark.setId(saved.getId());
    }

    @Override
    @Transactional
    public void updateMark(Long lessonId, Long markId, Mark updatedMark) {
        updatedMark.setId(markId);
        updatedMark.setLessonId(lessonId);
        updatedMark.setTimestamp(LocalDateTime.now());
        markRepo.save(updatedMark);
    }

    @Override
    @Transactional
    public void deleteMark(Long lessonId, Long markId) {
        markRepo.deleteById(markId);
    }

    @Override
    public List<Mark> latestMarks(int limit) {
        int safe = Math.max(1, Math.min(limit, 100));
        return markRepo.findAllByOrderByTimestampDesc(PageRequest.of(0, safe)).getContent();
    }

    // ===== НОВЕ: використання @Query / @NamedQuery / derived =====

    @Override
    public List<Lesson> searchLessonsByQuery(String subject, LocalDate from, LocalDate to) {
        return lessonRepo.search(subject, from, to);
    }

    @Override
    public List<Lesson> searchLessonsByTopicNamed(String pattern) {
        return lessonRepo.findByTopicPattern(pattern);
    }

    @Override
    public List<Mark> findPresentMarks(Long lessonId) {
        return markRepo.findPresentByLesson(lessonId);
    }

    @Override
    public List<Mark> findMarksInRangeNamed(Long lessonId, LocalDateTime from, LocalDateTime to) {
        return markRepo.findInRangeForLesson(lessonId, from, to);
    }
}
