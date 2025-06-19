package com.example.CardHolderManagement.controller;

import com.example.CardHolderManagement.model.entity.User;
import com.example.CardHolderManagement.model.entity.BankCard;
import com.example.CardHolderManagement.service.UserService;
import com.example.CardHolderManagement.service.BankCardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/web")
public class WebController {

    // Root redirect to web interface
    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/web/";
    }

    @Autowired
    private UserService userService;

    @Autowired
    private BankCardService bankCardService;

    // Homepage
    @GetMapping("/web")
    public String homepage(Model model) {
        long totalUsers = userService.findAll().size();
        long totalCards = bankCardService.findAll().size();
        long activeCards = bankCardService.findActiveCards().size();
        long expiredCards = totalCards - activeCards;

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalCards", totalCards);
        model.addAttribute("activeCards", activeCards);
        model.addAttribute("expiredCards", expiredCards);

        return "index";
    }

    // Users management
    @GetMapping("/web/users")
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "users/list";
    }

    @GetMapping("/web/users/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        return userService.findById(id)
                .map(user -> {
                    List<BankCard> cards = bankCardService.findByUserId(id);
                    model.addAttribute("user", user);
                    model.addAttribute("cards", cards);
                    return "users/view";
                })
                .orElse("redirect:/web/users?error=User not found");
    }

    @GetMapping("/web/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        return "users/form";
    }

    @PostMapping("/web/users")
    public String createUser(@Valid @ModelAttribute User user,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "users/form";
        }

        try {
            User savedUser = userService.save(user);
            redirectAttributes.addFlashAttribute("success", "Utilisateur créé avec succès!");
            return "redirect:/web/users/" + savedUser.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création: " + e.getMessage());
            return "redirect:/web/users/new";
        }
    }

    @GetMapping("/web/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Utilisateur supprimé avec succès!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/web/users";
    }

    // Cards management
    @GetMapping("/web/users/{userId}/cards/new")
    public String newCardForm(@PathVariable Long userId, Model model) {
        return userService.findById(userId)
                .map(user -> {
                    model.addAttribute("user", user);
                    model.addAttribute("card", new BankCard());
                    return "cards/form";
                })
                .orElse("redirect:/web/users?error=User not found");
    }

    @PostMapping("/web/users/{userId}/cards")
    public String createCard(@PathVariable Long userId,
                             @Valid @ModelAttribute BankCard card,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            userService.findById(userId).ifPresent(user -> model.addAttribute("user", user));
            model.addAttribute("card", card);
            return "cards/form";
        }

        return userService.findById(userId)
                .map(user -> {
                    try {
                        card.setUser(user);
                        bankCardService.save(card);
                        redirectAttributes.addFlashAttribute("success", "Carte ajoutée avec succès!");
                        return "redirect:/web/users/" + userId;
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", "Erreur lors de l'ajout: " + e.getMessage());
                        return "redirect:/web/users/" + userId + "/cards/new";
                    }
                })
                .orElse("redirect:/web/users?error=User not found");
    }

    @GetMapping("/web/cards/{cardId}/delete")
    public String deleteCard(@PathVariable Long cardId, RedirectAttributes redirectAttributes) {
        try {
            BankCard card = bankCardService.findById(cardId).orElse(null);
            if (card != null) {
                Long userId = card.getUser().getId();
                bankCardService.deleteById(cardId);
                redirectAttributes.addFlashAttribute("success", "Carte supprimée avec succès!");
                return "redirect:/web/users/" + userId;
            } else {
                redirectAttributes.addFlashAttribute("error", "Carte introuvable!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/web/users";
    }

    // Cards search and listing
    @GetMapping("/web/cards")
    public String listCards(@RequestParam(required = false) String search,
                            @RequestParam(required = false) String searchType,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("expirationDate").descending());
        Page<BankCard> cardsPage;

        if (search != null && !search.trim().isEmpty() && searchType != null) {
            List<BankCard> cards;
            switch (searchType) {
                case "partial":
                    cards = bankCardService.findByPartialCardNumber(search);
                    break;
                case "email":
                    cards = bankCardService.findByUserEmail(search);
                    break;
                case "expired":
                    try {
                        LocalDate date = LocalDate.parse(search);
                        cards = bankCardService.findByExpirationDateBefore(date);
                    } catch (Exception e) {
                        cards = List.of();
                    }
                    break;
                default:
                    cards = bankCardService.findAll();
            }
            // Convert list to page (simplified)
            cardsPage = new org.springframework.data.domain.PageImpl<>(cards, pageable, cards.size());
        } else {
            cardsPage = bankCardService.findAll(pageable);
        }

        model.addAttribute("cardsPage", cardsPage);
        model.addAttribute("search", search);
        model.addAttribute("searchType", searchType);

        return "cards/list";
    }

    @GetMapping("/web/cards/active")
    public String activeCards(Model model) {
        List<BankCard> cards = bankCardService.findActiveCards();
        model.addAttribute("cards", cards);
        model.addAttribute("title", "Cartes Actives");
        return "cards/simple-list";
    }

    @GetMapping("/web/cards/expired")
    public String expiredCards(Model model) {
        List<BankCard> cards = bankCardService.findCardsExpiredMoreThanSixMonths();
        model.addAttribute("cards", cards);
        model.addAttribute("title", "Cartes Expirées (6+ mois)");
        return "cards/simple-list";
    }
}