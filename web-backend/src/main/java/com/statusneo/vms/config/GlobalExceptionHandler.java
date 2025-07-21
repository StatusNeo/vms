package com.statusneo.vms.config;

import gg.jte.TemplateException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

public class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElement(NoSuchElementException ex, Model model) {
        model.addAttribute("error", "Resource not found: " + ex.getMessage());
        return "jte/404.jte";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolation(DataIntegrityViolationException ex, Model model) {
        model.addAttribute("error", "Data integrity error: " + ex.getRootCause().getMessage());
        return "jte/400.jte";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        model.addAttribute("error", "Invalid input: " + ex.getMessage());
        return "jte/400.jte";
    }

    @ExceptionHandler(TemplateException.class)
    public String handleTemplateException(TemplateException ex, Model model) {
        model.addAttribute("error", "Template rendering failed.");
        return "jte/500.jte";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        model.addAttribute("error", "An unexpected error occurred: " + ex.getMessage());
        return "jte/500.jte";
    }
}
