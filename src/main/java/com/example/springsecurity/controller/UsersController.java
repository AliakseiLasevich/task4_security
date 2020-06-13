package com.example.springsecurity.controller;

import com.example.springsecurity.model.User;
import com.example.springsecurity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@Controller
public class UsersController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String userIndex(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "users";
    }

    @PostMapping(value = "/edit", params = "action=block")
    public String block(@RequestParam(value = "users", required = false) String[] users) {
        userService.block(Arrays.asList(users));
        return "redirect:/";
    }

    @PostMapping(value = "/edit", params = "action=unblock")
    public String unblock(@RequestParam(value = "users", required = false) String[] users)  {
        userService.unblock(Arrays.asList(users));
        return "redirect:/";
    }

    @PostMapping(value = "/edit", params = "action=delete")
    public String delete(@RequestParam(value = "users", required = false) String[] users) {
        userService.delete(Arrays.asList(users));
        return "redirect:/";
    }
}
