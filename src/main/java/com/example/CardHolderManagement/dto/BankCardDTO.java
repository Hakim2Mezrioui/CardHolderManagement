package com.example.CardHolderManagement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BankCardDTO {

    @Pattern(regexp = "\\d{16}", message = "Le numéro de carte doit contenir exactement 16 chiffres")
    private String cardNumber;

    @Future(message = "La date d'expiration doit être dans le futur")
    private LocalDate expirationDate;

    @Pattern(regexp = "\\d{3}", message = "Le CVV doit contenir exactement 3 chiffres")
    private String cvv;

    @NotBlank(message = "Le type de carte est obligatoire")
    private String type;
}