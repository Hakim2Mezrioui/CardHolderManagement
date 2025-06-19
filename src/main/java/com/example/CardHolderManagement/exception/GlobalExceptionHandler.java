package com.example.CardHolderManagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
    
    // Gestion spéciale pour les ressources statiques manquantes
    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNoResourceFound(NoResourceFoundException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/web/");
        return modelAndView;
    }
    
    // CORRECTION CRITIQUE: Séparer les erreurs Web vs API
    @ExceptionHandler(Exception.class)
    public Object handleGeneralException(Exception ex, HttpServletRequest request) {
        // Log l'erreur pour debugging
        System.err.println("ERROR: " + ex.getMessage());
        ex.printStackTrace();
        
        String requestURI = request.getRequestURI();
        
        // Si c'est une requête vers l'interface web (/web/*), retourner une page HTML
        if (requestURI != null && requestURI.startsWith("/web")) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("redirect:/web/?error=" + ex.getMessage());
            return modelAndView;
        }
        
        // Si c'est une requête API (/api/*), retourner du JSON
        Map<String, String> error = new HashMap<>();
        error.put("error", "Une erreur interne s'est produite");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}