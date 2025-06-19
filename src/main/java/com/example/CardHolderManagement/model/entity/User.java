package com.example.CardHolderManagement.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "cards")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String name;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    @Column(unique = true, nullable = false)
    private String email;

    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate birthDate;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BankCard> cards;

    public List<BankCard> getCards() {
        if (cards == null) {
            cards = new ArrayList<>();
        }
        return cards;
    }
}