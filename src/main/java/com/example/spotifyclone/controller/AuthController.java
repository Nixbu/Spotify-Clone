package com.example.spotifyclone.controller;

import com.example.spotifyclone.entity.User;
import com.example.spotifyclone.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {

        if (error != null) {
            model.addAttribute("errorMsg", "Invalid username or password!");
        }

        if (logout != null) {
            model.addAttribute("msg", "You have been logged out successfully!");
        }

        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("user") User user,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        // Check if username already exists
        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("errorMsg", "Username already exists!");
            return "auth/register";
        }

        // Check if email already exists
        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("errorMsg", "Email already exists!");
            return "auth/register";
        }

        try {
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Registration successful! Please login to continue.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMsg", "Registration failed! Please try again.");
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("msg", "You have been logged out successfully!");
        return "redirect:/home";
    }
}