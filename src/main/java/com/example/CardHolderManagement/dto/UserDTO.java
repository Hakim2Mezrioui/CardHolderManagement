package com.example.CardHolderManagement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDTO {

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate birthDate;
}