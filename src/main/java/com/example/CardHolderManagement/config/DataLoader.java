package com.example.CardHolderManagement.config;

import com.example.CardHolderManagement.model.entity.BankCard;
import com.example.CardHolderManagement.model.entity.User;
import com.example.CardHolderManagement.repository.BankCardRepository;
import com.example.CardHolderManagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankCardRepository bankCardRepository;

    @Override
    public void run(String... args) throws Exception {
        // Ne charger les données que si aucun utilisateur n'existe
        if (userRepository.count() == 0) {
            loadTestData();
        }
    }

    private void loadTestData() {
        try {
            // Créer des utilisateurs de test
            User user1 = new User();
            user1.setName("Jean Dupont");
            user1.setEmail("jean.dupont@email.com");
            user1.setBirthDate(LocalDate.of(1985, 3, 15));
            user1 = userRepository.save(user1);

            User user2 = new User();
            user2.setName("Marie Martin");
            user2.setEmail("marie.martin@email.com");
            user2.setBirthDate(LocalDate.of(1990, 7, 22));
            user2 = userRepository.save(user2);

            User user3 = new User();
            user3.setName("Pierre Durand");
            user3.setEmail("pierre.durand@email.com");
            user3.setBirthDate(LocalDate.of(1978, 11, 8));
            user3 = userRepository.save(user3);

            // Créer des cartes de test
            BankCard card1 = new BankCard();
            card1.setCardNumber("1234567890123456");
            card1.setType("VISA");
            card1.setCvv("123");
            card1.setExpirationDate(LocalDate.of(2025, 12, 31));
            card1.setUser(user1);
            bankCardRepository.save(card1);

            BankCard card2 = new BankCard();
            card2.setCardNumber("9876543210987654");
            card2.setType("MASTERCARD");
            card2.setCvv("456");
            card2.setExpirationDate(LocalDate.of(2026, 6, 30));
            card2.setUser(user1);
            bankCardRepository.save(card2);

            BankCard card3 = new BankCard();
            card3.setCardNumber("5555666677778888");
            card3.setType("VISA");
            card3.setCvv("789");
            card3.setExpirationDate(LocalDate.of(2024, 3, 15));
            card3.setUser(user2);
            bankCardRepository.save(card3);

            // Carte expirée
            BankCard expiredCard = new BankCard();
            expiredCard.setCardNumber("1111222233334444");
            expiredCard.setType("AMEX");
            expiredCard.setCvv("1234");
            expiredCard.setExpirationDate(LocalDate.of(2020, 1, 1));
            expiredCard.setUser(user3);
            bankCardRepository.save(expiredCard);

            System.out.println("✅ Données de test chargées avec succès !");
            System.out.println("📊 " + userRepository.count() + " utilisateurs créés");
            System.out.println("💳 " + bankCardRepository.count() + " cartes créées");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des données de test: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 