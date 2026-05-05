package com.campusevent.controller;

import com.campusevent.model.Event;
import com.campusevent.model.User;
import com.campusevent.service.EventService;
import com.campusevent.service.RegistrationService;
import com.campusevent.service.SupportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final SupportService supportService;

    @Autowired
    public AdminController(EventService eventService, RegistrationService registrationService, SupportService supportService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.supportService = supportService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("events", eventService.getAllEvents());
        model.addAttribute("registrations", registrationService.getAllRegistrations());
        model.addAttribute("supportTickets", supportService.getAllPendingTickets());
        return "admin-dashboard";
    }

    @GetMapping("/add-event")
    public String showAddEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "add-event";
    }

    @PostMapping("/add-event")
    public String addEvent(@Valid @ModelAttribute("event") Event event, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "add-event";
        }
        eventService.saveEvent(event);
        return "redirect:/admin/dashboard?success=eventAdded";
    }

    @PostMapping("/delete-event/{id}")
    public String deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return "redirect:/admin/dashboard?success=eventDeleted";
    }

    @PostMapping("/resolve-ticket/{id}")
    public String resolveTicket(@PathVariable Long id) {
        supportService.processTicket(id);
        return "redirect:/admin/dashboard?success=ticketResolved";
    }
}
