package com.example.CardHolderManagement.repository;

import com.example.CardHolderManagement.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE SIZE(u.cards) > 2 AND " +
            "EXISTS (SELECT c FROM BankCard c WHERE c.user = u AND c.expirationDate > CURRENT_DATE)")
    List<User> findUsersWithMoreThanTwoActiveCards();

    @Query("SELECT u FROM User u WHERE u.name LIKE %:namePattern%")
    List<User> findByNameContaining(@Param("namePattern") String namePattern);
}