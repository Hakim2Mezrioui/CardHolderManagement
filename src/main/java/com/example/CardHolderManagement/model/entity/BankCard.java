package com.example.CardHolderManagement.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDate;

@Entity
@Table(name = "bank_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user")
public class BankCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Pattern(regexp = "\\d{16}", message = "Le numéro de carte doit contenir exactement 16 chiffres")
    @Column(unique = true, nullable = false)
    private String cardNumber;

    @Future(message = "La date d'expiration doit être dans le futur")
    @Column(nullable = false)
    private LocalDate expirationDate;

    @Pattern(regexp = "\\d{3}", message = "Le CVV doit contenir exactement 3 chiffres")
    @Column(nullable = false)
    private String cvv;

    @NotBlank(message = "Le type de carte est obligatoire")
    @Column(nullable = false)
    private String type; // Visa, Mastercard, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}