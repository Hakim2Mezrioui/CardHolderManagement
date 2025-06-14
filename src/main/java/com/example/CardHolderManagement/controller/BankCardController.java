package com.example.CardHolderManagement.controller;

import com.example.CardHolderManagement.model.entity.BankCard;
import com.example.CardHolderManagement.service.BankCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "*")
public class BankCardController {

    @Autowired
    private BankCardService bankCardService;

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        if (bankCardService.findById(cardId).isPresent()) {
            bankCardService.deleteById(cardId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search/partial-number")
    public ResponseEntity<List<BankCard>> searchByPartialNumber(@RequestParam String contains) {
        List<BankCard> cards = bankCardService.findByPartialCardNumber(contains);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/search/expire-before")
    public ResponseEntity<List<BankCard>> searchExpireBefore(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<BankCard> cards = bankCardService.findByExpirationDateBefore(date);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/search/by-email")
    public ResponseEntity<List<BankCard>> searchByEmail(@RequestParam String email) {
        List<BankCard> cards = bankCardService.findByUserEmail(email);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/search/by-email-and-expiry")
    public ResponseEntity<List<BankCard>> searchByEmailAndExpiry(
            @RequestParam String email,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<BankCard> cards = bankCardService.findByUserEmailAndExpirationBefore(email, date);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/search/paginated")
    public ResponseEntity<Page<BankCard>> searchPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "expirationDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<BankCard> cards = bankCardService.findAll(pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/active")
    public ResponseEntity<List<BankCard>> getActiveCards() {
        List<BankCard> cards = bankCardService.findActiveCards();
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/expired-six-months")
    public ResponseEntity<List<BankCard>> getCardsExpiredMoreThanSixMonths() {
        List<BankCard> cards = bankCardService.findCardsExpiredMoreThanSixMonths();
        return ResponseEntity.ok(cards);
    }
}