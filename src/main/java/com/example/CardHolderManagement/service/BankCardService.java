package com.example.CardHolderManagement.service;

import com.example.CardHolderManagement.model.entity.BankCard;
import com.example.CardHolderManagement.repository.BankCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BankCardService {

    @Autowired
    private BankCardRepository bankCardRepository;

    public List<BankCard> findAll() {
        return bankCardRepository.findAll();
    }

    public Optional<BankCard> findById(Long id) {
        return bankCardRepository.findById(id);
    }

    public BankCard save(BankCard bankCard) {
        return bankCardRepository.save(bankCard);
    }

    public void deleteById(Long id) {
        bankCardRepository.deleteById(id);
    }

    public List<BankCard> findByUserId(Long userId) {
        return bankCardRepository.findByUserId(userId);
    }

    public List<BankCard> findByPartialCardNumber(String partialNumber) {
        return bankCardRepository.findByPartialCardNumber(partialNumber);
    }

    public List<BankCard> findByExpirationDateBefore(LocalDate date) {
        return bankCardRepository.findByExpirationDateBefore(date);
    }

    public List<BankCard> findByUserEmail(String email) {
        return bankCardRepository.findByUserEmail(email);
    }

    public List<BankCard> findByUserEmailAndExpirationBefore(String email, LocalDate date) {
        return bankCardRepository.findByUserEmailAndExpirationBefore(email, date);
    }

    public Page<BankCard> findAll(Pageable pageable) {
        return bankCardRepository.findAll(pageable);
    }

    public List<BankCard> findActiveCards() {
        return bankCardRepository.findActiveCards();
    }

    public List<BankCard> findCardsExpiredMoreThanSixMonths() {
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        return bankCardRepository.findCardsExpiredMoreThanSixMonths(sixMonthsAgo);
    }
}