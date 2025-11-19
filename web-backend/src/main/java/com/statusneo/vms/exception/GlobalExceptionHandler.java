package com.statusneo.vms.exception;

import gg.jte.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMethodArgumentNotValid(MethodArgumentNotValidException ex, Model model) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        logger.warn("Method argument validation failed: {}", errorMessage);
        model.addAttribute("error", "Validation failed: " + errorMessage);
        model.addAttribute("fieldErrors", ex.getBindingResult().getFieldErrors());

        return "error/400";
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBindException(BindException ex, Model model) {
        String errorMessage = ex.getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        logger.warn("Binding error: {}", errorMessage);
        model.addAttribute("error", "Form binding failed: " + errorMessage);
        model.addAttribute("fieldErrors", ex.getFieldErrors());

        return "error/400";
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMissingParams(MissingServletRequestParameterException ex, Model model) {
        logger.warn("Missing required parameter: {}", ex.getParameterName());
        model.addAttribute("error", "Missing required parameter: " + ex.getParameterName());
        return "error/400";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleTypeMismatch(MethodArgumentTypeMismatchException ex, Model model) {
        logger.warn("Type mismatch for parameter '{}': expected {}",
                ex.getName(), ex.getRequiredType());
        model.addAttribute("error",
                String.format("Invalid value for parameter '%s'. Expected type: %s",
                        ex.getName(),
                        ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"));
        return "error/400";
    }


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoSuchElement(NoSuchElementException ex, HttpServletRequest request) {
        logger.warn("Resource not found: {}", ex.getMessage());
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.NOT_FOUND.value());
        return "forward:/error/404.html";
    }


    @ExceptionHandler({DataIntegrityViolationException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequestExceptions(Exception ex, Model model) {
        logger.warn("Bad request - {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        model.addAttribute("error", "Invalid request: " + ex.getMessage());
        return "error/400";
    }


    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleConstraintViolation(ConstraintViolationException ex, Model model) {
        String errorMessage = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        logger.warn("Constraint violation: {}", errorMessage);
        model.addAttribute("error", "Invalid input: " + errorMessage);
        return "error/400";
    }


    @ExceptionHandler(TemplateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleTemplateException(TemplateException ex, HttpServletRequest request) {
        logger.error("Template rendering failed: {}", ex.getMessage(), ex);
        // NEVER return a JTE template here - use static HTML only
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
        return "forward:/error/500.html";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error occurred - {}: {}",
                ex.getClass().getSimpleName(), ex.getMessage(), ex);
        // NEVER return a JTE template here - use static HTML only
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
        return "forward:/error/500.html";
    }
}