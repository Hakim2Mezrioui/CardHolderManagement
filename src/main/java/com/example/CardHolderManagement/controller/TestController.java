package com.example.CardHolderManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "Controller is working!";
    }

    @GetMapping("/web/test")
    @ResponseBody
    public String webTest() {
        return "Web routes are working!";
    }

    @GetMapping("/thymeleaf-test")
    public String thymeleafTest(Model model) {
        model.addAttribute("message", "Thymeleaf fonctionne parfaitement ! 🎉");
        return "test";
    }
} 