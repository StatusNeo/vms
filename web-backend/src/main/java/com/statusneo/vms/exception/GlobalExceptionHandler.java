package com.statusneo.vms.exception;

import gg.jte.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElement(NoSuchElementException ex, Model model) {
        logger.warn("Resource not found", ex);
        model.addAttribute("error", "The requested resource was not found.");
        return "jte/404.jte";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolation(DataIntegrityViolationException ex, Model model) {
        logger.error("Data integrity violation", ex);
        model.addAttribute("error", "A data validation error occurred. Please check your input.");
        return "jte/400.jte";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        logger.warn("Invalid argument", ex);
        model.addAttribute("error", "Invalid input provided.");
        return "jte/400.jte";
    }

    @ExceptionHandler(TemplateException.class)
    public String handleTemplateException(TemplateException ex, Model model) {
        logger.error("Template rendering failed", ex);
        model.addAttribute("error", "A technical error occurred while rendering the page.");
        return "jte/500.jte";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        logger.error("Unexpected error occurred", ex);
        model.addAttribute("error", "Something went wrong. Please try again later.");
        return "jte/500.jte";
    }
}
