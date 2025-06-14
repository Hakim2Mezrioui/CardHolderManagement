package com.example.CardHolderManagement.controller;

import com.example.CardHolderManagement.model.entity.User;
import com.example.CardHolderManagement.model.entity.BankCard;
import com.example.CardHolderManagement.service.UserService;
import com.example.CardHolderManagement.service.BankCardService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BankCardService bankCardService;

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user, BindingResult result) {
        if (result.hasErrors()) {
            throw new ValidationException("Données invalides : " + result.getAllErrors());
        }
        User savedUser = userService.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userService.findById(id).isPresent()) {
            userService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{userId}/cards")
    public ResponseEntity<BankCard> addCard(@PathVariable Long userId,
                                            @Valid @RequestBody BankCard bankCard,
                                            BindingResult result) {
        if (result.hasErrors()) {
            throw new ValidationException("Données invalides : " + result.getAllErrors());
        }

        return userService.findById(userId)
                .map(user -> {
                    bankCard.setUser(user);
                    BankCard savedCard = bankCardService.save(bankCard);
                    return ResponseEntity.ok(savedCard);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}/cards")
    public ResponseEntity<List<BankCard>> getUserCards(@PathVariable Long userId) {
        List<BankCard> cards = bankCardService.findByUserId(userId);
        return ResponseEntity.ok(cards);
    }
}