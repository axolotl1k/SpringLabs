package org.axolotlik.labs.controller;

import org.axolotlik.labs.service.JournalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class StudentController {

    private final JournalService journalService;

    // @Autowired тут не обов'язкова, бо конструктор один, але використана в якості прикладу
    @Autowired
    public StudentController(JournalService journalService) {
        this.journalService = journalService;
    }

    @GetMapping
    public String showJournal(Model model) {
        model.addAttribute("lessons", journalService.getAllLessons());
        return "journal";
    }

    @GetMapping("/lesson/{id}")
    public String showLesson(@PathVariable Long id, Model model) {
        model.addAttribute("lesson", journalService.getLessonById(id).orElseThrow());
        return "lesson-details";
    }
}