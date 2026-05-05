package com.campusevent.controller;

import com.campusevent.model.Event;
import com.campusevent.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final EventService eventService;

    @Autowired
    public DashboardController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        List<Event> upcomingEvents = eventService.getAllEvents();
        model.addAttribute("events", upcomingEvents);
        
        // Show all upcoming events on dashboard for simplicity
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        }
        
        return "dashboard";
    }
}
