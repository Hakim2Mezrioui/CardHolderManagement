package com.example.CardHolderManagement.repository;

import com.example.CardHolderManagement.model.entity.BankCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BankCardRepository extends JpaRepository<BankCard, Long> {

    // Retrouver les cartes d'un utilisateur
    List<BankCard> findByUserId(Long userId);

    // Retrouver des cartes par numéro partiel
    @Query("SELECT c FROM BankCard c WHERE c.cardNumber LIKE %:partialNumber%")
    List<BankCard> findByPartialCardNumber(@Param("partialNumber") String partialNumber);

    // Retrouver des cartes expirant avant une date donnée
    List<BankCard> findByExpirationDateBefore(LocalDate date);

    // Retrouver des cartes par email d'utilisateur
    @Query("SELECT c FROM BankCard c WHERE c.user.email = :email")
    List<BankCard> findByUserEmail(@Param("email") String email);

    // Recherche combinée : par email et expiration
    @Query("SELECT c FROM BankCard c WHERE c.user.email = :email AND c.expirationDate < :date")
    List<BankCard> findByUserEmailAndExpirationBefore(@Param("email") String email,
                                                      @Param("date") LocalDate date);

    // Pagination et tri
    Page<BankCard> findAll(Pageable pageable);

    // Retrouver les cartes actives (non expirées)
    @Query("SELECT c FROM BankCard c WHERE c.expirationDate > CURRENT_DATE")
    List<BankCard> findActiveCards();

    // Rechercher les cartes associées à un utilisateur dont le nom contient une chaîne donnée
    @Query("SELECT c FROM BankCard c WHERE c.user.name LIKE %:namePattern%")
    List<BankCard> findByUserNameContaining(@Param("namePattern") String namePattern);

    // Retrouver les cartes triées par date d'expiration décroissante
    List<BankCard> findAllByOrderByExpirationDateDesc();

    // Retrouver les cartes expirées depuis plus de 6 mois
    @Query("SELECT c FROM BankCard c WHERE c.expirationDate < :sixMonthsAgo")
    List<BankCard> findCardsExpiredMoreThanSixMonths(@Param("sixMonthsAgo") LocalDate sixMonthsAgo);
}