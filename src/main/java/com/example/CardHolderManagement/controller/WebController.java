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
public class WebController {

    @Autowired
    private UserService userService;

    @Autowired
    private BankCardService bankCardService;

    // Root redirect to dashboard
    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/web";
    }
    
    // Alternative root mappings
    @GetMapping("")
    public String rootRedirectAlt() {
        return "redirect:/web";
    }

    // Debug endpoint - accessible via navigateur
    @GetMapping("/web/debug")
    public String debug(Model model) {
        try {
            List<User> users = userService.findAll();
            List<BankCard> cards = bankCardService.findAll();
            
            StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("=== INFORMATIONS DE DEBUG ===\n");
            debugInfo.append("Nombre d'utilisateurs: ").append(users.size()).append("\n\n");
            
            for (User user : users) {
                debugInfo.append("ID: ").append(user.getId())
                         .append(", Nom: ").append(user.getName())
                         .append(", Email: ").append(user.getEmail()).append("\n");
            }
            
            debugInfo.append("\nNombre de cartes: ").append(cards.size()).append("\n");
            
            model.addAttribute("debugInfo", debugInfo.toString());
            model.addAttribute("users", users);
            model.addAttribute("cards", cards);
            return "debug";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "debug";
        }
    }

    // Homepage
    @GetMapping("/web")
    public String homepage(Model model) {
        try {
            long totalUsers = userService.findAll().size();
            long totalCards = bankCardService.findAll().size();
            long activeCards = bankCardService.findActiveCards().size();
            long expiredCards = totalCards - activeCards;

            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalCards", totalCards);
            model.addAttribute("activeCards", activeCards);
            model.addAttribute("expiredCards", expiredCards);
        } catch (Exception e) {
            // En cas d'erreur avec la base de données, on met des valeurs par défaut
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalCards", 0);
            model.addAttribute("activeCards", 0);
            model.addAttribute("expiredCards", 0);
            model.addAttribute("error", "Erreur de connexion à la base de données: " + e.getMessage());
        }

        return "index";
    }

    // Users management
    @GetMapping("/web/users")
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        System.out.println("👥 Nombre d'utilisateurs dans la base: " + users.size());
        for (User user : users) {
            System.out.println("   - ID: " + user.getId() + ", Nom: " + user.getName());
        }
        model.addAttribute("users", users);
        return "users/list";
    }

    @GetMapping("/web/users/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        System.out.println("🔍 Tentative d'accès à l'utilisateur ID: " + id);
        try {
            return userService.findById(id)
                    .map(user -> {
                        System.out.println("✅ Utilisateur trouvé: " + user.getName());
                        try {
                            List<BankCard> cards = bankCardService.findByUserId(id);
                            System.out.println("💳 Cartes trouvées: " + cards.size());
                            model.addAttribute("user", user);
                            model.addAttribute("cards", cards);
                            return "users/view";
                        } catch (Exception e) {
                            System.err.println("❌ Erreur cartes: " + e.getMessage());
                            model.addAttribute("error", "Erreur lors de la récupération des cartes: " + e.getMessage());
                            model.addAttribute("user", user);
                            model.addAttribute("cards", java.util.List.of());
                            return "users/view";
                        }
                    })
                    .orElseGet(() -> {
                        System.err.println("❌ Utilisateur non trouvé avec ID: " + id);
                        return "redirect:/web/users?error=Utilisateur non trouvé avec ID: " + id;
                    });
        } catch (Exception e) {
            System.err.println("❌ Erreur système: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/web/users?error=Erreur système: " + e.getMessage();
        }
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

    @GetMapping("/web/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        return userService.findById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    return "users/edit-form";
                })
                .orElse("redirect:/web/users?error=User not found");
    }

    @PostMapping("/web/users/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute User user,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "users/edit-form";
        }

        return userService.findById(id)
                .map(existingUser -> {
                    try {
                        user.setId(id);
                        user.setCards(existingUser.getCards()); // Preserve existing cards
                        userService.save(user);
                        redirectAttributes.addFlashAttribute("success", "Utilisateur mis à jour avec succès!");
                        return "redirect:/web/users/" + id;
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
                        return "redirect:/web/users/" + id + "/edit";
                    }
                })
                .orElse("redirect:/web/users?error=User not found");
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

    // Cards search and listing with advanced search
    @GetMapping("/web/cards")
    public String listCards(@RequestParam(required = false) String search,
                            @RequestParam(required = false) String searchType,
                            @RequestParam(required = false) String cardType,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) String sortBy,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
        try {

        // Determine sort order
        Sort sort = Sort.by("expirationDate").descending();
        if (sortBy != null) {
            switch (sortBy) {
                case "cardNumber":
                    sort = Sort.by("cardNumber").ascending();
                    break;
                case "type":
                    sort = Sort.by("type").ascending();
                    break;
                case "userName":
                    sort = Sort.by("user.name").ascending();
                    break;
                case "expirationDate":
                    sort = Sort.by("expirationDate").ascending();
                    break;
                default:
                    sort = Sort.by("expirationDate").descending();
            }
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BankCard> cardsPage;

        // Advanced search logic
        if ((search != null && !search.trim().isEmpty()) || 
            (cardType != null && !cardType.trim().isEmpty()) || 
            (status != null && !status.trim().isEmpty())) {
            
            List<BankCard> allCards = bankCardService.findAll();
            List<BankCard> filteredCards = allCards.stream()
                .filter(card -> {
                    // Filter by search term and type
                    boolean matchesSearch = true;
                    if (search != null && !search.trim().isEmpty() && searchType != null) {
                        switch (searchType) {
                            case "partial":
                                matchesSearch = card.getCardNumber().contains(search);
                                break;
                            case "email":
                                matchesSearch = card.getUser().getEmail().toLowerCase()
                                    .contains(search.toLowerCase());
                                break;
                            case "userName":
                                matchesSearch = card.getUser().getName().toLowerCase()
                                    .contains(search.toLowerCase());
                                break;
                            case "cvv":
                                matchesSearch = card.getCvv().contains(search);
                                break;
                            case "expired":
                                try {
                                    LocalDate date = LocalDate.parse(search);
                                    matchesSearch = card.getExpirationDate().isBefore(date);
                                } catch (Exception e) {
                                    matchesSearch = false;
                                }
                                break;
                            default:
                                matchesSearch = true;
                        }
                    }

                    // Filter by card type
                    boolean matchesCardType = true;
                    if (cardType != null && !cardType.trim().isEmpty()) {
                        matchesCardType = card.getType().equalsIgnoreCase(cardType);
                    }

                    // Filter by status
                    boolean matchesStatus = true;
                    if (status != null && !status.trim().isEmpty()) {
                        boolean isExpired = card.getExpirationDate().isBefore(LocalDate.now());
                        if ("active".equals(status)) {
                            matchesStatus = !isExpired;
                        } else if ("expired".equals(status)) {
                            matchesStatus = isExpired;
                        }
                    }

                    return matchesSearch && matchesCardType && matchesStatus;
                })
                .toList();

            cardsPage = new org.springframework.data.domain.PageImpl<>(filteredCards, pageable, filteredCards.size());
        } else {
            cardsPage = bankCardService.findAll(pageable);
        }

        // Add available card types for filter dropdown
        List<String> cardTypes = bankCardService.findAll().stream()
            .map(BankCard::getType)
            .distinct()
            .sorted()
            .toList();

        model.addAttribute("cardsPage", cardsPage);
        model.addAttribute("search", search);
        model.addAttribute("searchType", searchType);
        model.addAttribute("cardType", cardType);
        model.addAttribute("status", status);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("cardTypes", cardTypes);

        return "cards/list";
        } catch (Exception e) {
            model.addAttribute("cardsPage", org.springframework.data.domain.Page.empty());
            model.addAttribute("search", search);
            model.addAttribute("searchType", searchType);
            model.addAttribute("cardType", cardType);
            model.addAttribute("status", status);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("cardTypes", java.util.List.of());
            model.addAttribute("error", "Erreur lors de la récupération des cartes: " + e.getMessage());
            return "cards/list";
        }
    }

    // Advanced search page
    @GetMapping("/web/cards/search")
    public String advancedSearchForm(Model model) {
        // Get available card types
        List<String> cardTypes = bankCardService.findAll().stream()
            .map(BankCard::getType)
            .distinct()
            .sorted()
            .toList();

        model.addAttribute("cardTypes", cardTypes);
        return "cards/advanced-search";
    }

    @GetMapping("/web/cards/active")
    public String activeCards(Model model) {
        try {
            List<BankCard> cards = bankCardService.findActiveCards();
            model.addAttribute("cards", cards);
            model.addAttribute("title", "Cartes Actives");
        } catch (Exception e) {
            model.addAttribute("cards", List.of());
            model.addAttribute("title", "Cartes Actives");
            model.addAttribute("error", "Erreur lors de la récupération des cartes: " + e.getMessage());
        }
        return "cards/simple_list";
    }

    @GetMapping("/web/cards/expired")
    public String expiredCards(Model model) {
        try {
            List<BankCard> cards = bankCardService.findCardsExpiredMoreThanSixMonths();
            model.addAttribute("cards", cards);
            model.addAttribute("title", "Cartes Expirées (6+ mois)");
        } catch (Exception e) {
            model.addAttribute("cards", List.of());
            model.addAttribute("title", "Cartes Expirées");
            model.addAttribute("error", "Erreur lors de la récupération des cartes: " + e.getMessage());
        }
        return "cards/simple_list";
    }

    // Card details view
    @GetMapping("/web/cards/{cardId}")
    public String viewCard(@PathVariable Long cardId, Model model) {
        return bankCardService.findById(cardId)
                .map(card -> {
                    // Check if card is expired
                    boolean isExpired = card.getExpirationDate().isBefore(LocalDate.now());
                    long daysUntilExpiration = LocalDate.now().until(card.getExpirationDate()).getDays();
                    
                    model.addAttribute("card", card);
                    model.addAttribute("isExpired", isExpired);
                    model.addAttribute("daysUntilExpiration", daysUntilExpiration);
                    
                    return "cards/detail";
                })
                .orElse("redirect:/web/cards?error=Card not found");
    }

    // Edit card form
    @GetMapping("/web/cards/{cardId}/edit")
    public String editCardForm(@PathVariable Long cardId, Model model) {
        return bankCardService.findById(cardId)
                .map(card -> {
                    model.addAttribute("card", card);
                    model.addAttribute("user", card.getUser());
                    return "cards/edit-form";
                })
                .orElse("redirect:/web/cards?error=Card not found");
    }

    // Update card
    @PostMapping("/web/cards/{cardId}/edit")
    public String updateCard(@PathVariable Long cardId,
                             @Valid @ModelAttribute BankCard card,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            bankCardService.findById(cardId).ifPresent(existingCard -> {
                model.addAttribute("user", existingCard.getUser());
            });
            model.addAttribute("card", card);
            return "cards/edit-form";
        }

        return bankCardService.findById(cardId)
                .map(existingCard -> {
                    try {
                        // Keep the original ID and user
                        card.setId(cardId);
                        card.setUser(existingCard.getUser());
                        bankCardService.save(card);
                        redirectAttributes.addFlashAttribute("success", "Carte mise à jour avec succès!");
                        return "redirect:/web/cards/" + cardId;
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
                        return "redirect:/web/cards/" + cardId + "/edit";
                    }
                })
                .orElse("redirect:/web/cards?error=Card not found");
    }
}