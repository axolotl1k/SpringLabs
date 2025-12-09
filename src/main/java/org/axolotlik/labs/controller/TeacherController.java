package org.axolotlik.labs.controller;

import org.axolotlik.labs.model.Lesson;
import org.axolotlik.labs.model.Mark;
import org.axolotlik.labs.service.JournalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private JournalService journalService;

    // забороняємо підв'язку поля id у формі створення Mark (щоб не прилітав UPDATE)
    @InitBinder("newMark")
    void disallowId(WebDataBinder b) { b.setDisallowedFields("id"); }

    // --- Marks ---

    // форма "додати відмітку"
    @GetMapping("/lesson/{lessonId}/add")
    public String showAddMarkForm(@PathVariable Long lessonId, Model model) {
        Lesson lesson = journalService.getLessonById(lessonId).orElseThrow();
        model.addAttribute("lesson", lesson);
        model.addAttribute("newMark", new Mark());
        return "add-mark-form";
    }

    // сабміт форми "додати відмітку"
    @PostMapping("/lesson/{lessonId}/add")
    public String addMark(@PathVariable Long lessonId, @ModelAttribute("newMark") Mark mark) {
        mark.setId(null); // гарантуємо INSERT
        if (mark.getTimestamp() == null) mark.setTimestamp(LocalDateTime.now());
        journalService.addMark(lessonId, mark);
        return "redirect:/lesson/" + lessonId;
    }

    @PostMapping("/lesson/{lessonId}/mark/{markId}/delete")
    public String deleteMark(@PathVariable Long lessonId, @PathVariable Long markId) {
        journalService.deleteMark(lessonId, markId);
        return "redirect:/lesson/" + lessonId;
    }

    @GetMapping("/lesson/{lessonId}/mark/{markId}/edit")
    public String showEditMarkForm(@PathVariable Long lessonId, @PathVariable Long markId, Model model) {
        Mark mark = journalService.findMarkById(lessonId, markId);
        model.addAttribute("mark", mark);
        model.addAttribute("lessonId", lessonId);
        return "edit-mark-form";
    }

    @PostMapping("/lesson/{lessonId}/mark/{markId}/update")
    public String updateMark(@PathVariable Long lessonId, @PathVariable Long markId, @ModelAttribute Mark updatedMark) {
        journalService.updateMark(lessonId, markId, updatedMark);
        return "redirect:/lesson/" + lessonId;
    }

    // --- Lessons ---

    @GetMapping("/lesson/new")
    public String showCreateLessonForm(Model model) {
        model.addAttribute("lesson", new Lesson());
        return "create-lesson-form";
    }

    @PostMapping("/lesson/create")
    public String createLesson(@ModelAttribute Lesson lesson) {
        journalService.createLesson(lesson.getSubject(), lesson.getTopic());
        return "redirect:/";
    }

    @PostMapping("/lesson/{id}/delete")
    public String deleteLesson(@PathVariable Long id) {
        journalService.deleteLesson(id);
        return "redirect:/";
    }

    @GetMapping("/lesson/{id}/edit")
    public String showEditLessonForm(@PathVariable Long id, Model model) {
        Lesson lesson = journalService.getLessonById(id).orElseThrow();
        model.addAttribute("lesson", lesson);
        return "edit-lesson-form";
    }

    @PostMapping("/lesson/{id}/update")
    public String updateLesson(@PathVariable Long id, @ModelAttribute Lesson lesson) {
        journalService.updateLesson(id, lesson.getSubject(), lesson.getTopic());
        return "redirect:/";
    }
}
