package com.example.demoCookie.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OauthController {
    @GetMapping("/login")
    public String login() {
        return "redirect:/oauth2/authorization/google";
    }

    @GetMapping("/callback")
    public String callback() {
        return "redirect:/";
    }
}
