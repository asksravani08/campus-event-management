package com.campusevent.controller;

import com.campusevent.model.Event;
import com.campusevent.model.Registration;
import com.campusevent.model.User;
import com.campusevent.service.EventService;
import com.campusevent.service.RegistrationService;
import com.campusevent.service.SupportService;
import com.campusevent.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class StudentController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final UserService userService;
    private final SupportService supportService;

    @Autowired
    public StudentController(EventService eventService, RegistrationService registrationService, UserService userService, SupportService supportService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.userService = userService;
        this.supportService = supportService;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/events")
    public String viewEvents(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Event> events = eventService.searchEvents(keyword);
        model.addAttribute("events", events);
        model.addAttribute("keyword", keyword);
        return "events";
    }

    @PostMapping("/book")
    public String bookEvent(@RequestParam("eventId") Long eventId, 
                            @RequestParam(value = "applyCoupon", required = false) boolean applyCoupon,
                            RedirectAttributes redirectAttributes) {
        
        Event event = eventService.getEventById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
        
        if (event.isPaid()) {
            return "redirect:/register-paid/" + eventId;
        } else {
            User user = getAuthenticatedUser();
            try {
                Registration reg = registrationService.registerForEvent(user, event, false);
                userService.incrementScore(user, 10);
                redirectAttributes.addFlashAttribute("successMessage", "Successfully registered for " + event.getTitle() + "!");
                return "redirect:/participation-email/" + reg.getId();
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/events";
            }
        }
    }

    @GetMapping("/register-paid/{eventId}")
    public String showPaidRegistrationForm(@PathVariable Long eventId, Model model) {
        Event event = eventService.getEventById(eventId).orElseThrow();
        User user = getAuthenticatedUser();
        
        model.addAttribute("event", event);
        model.addAttribute("user", user);
        return "paid-registration";
    }

    @PostMapping("/initiate-payment")
    public String initiatePayment(@RequestParam("eventId") Long eventId,
                                  @RequestParam("mobileNumber") String mobileNumber,
                                  @RequestParam("collegeIdNumber") String collegeIdNumber,
                                  HttpSession session) {
        session.setAttribute("pendingMobile", mobileNumber);
        session.setAttribute("pendingCollegeId", collegeIdNumber);
        return "redirect:/payment/" + eventId;
    }

    @GetMapping("/payment/{eventId}")
    public String showPaymentGateway(@PathVariable Long eventId, Model model) {
        Event event = eventService.getEventById(eventId).orElseThrow();
        model.addAttribute("event", event);
        return "payment";
    }

    @PostMapping("/process-payment")
    public String processPayment(@RequestParam("eventId") Long eventId, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser();
        Event event = eventService.getEventById(eventId).orElseThrow();
        
        String mobile = (String) session.getAttribute("pendingMobile");
        String collegeId = (String) session.getAttribute("pendingCollegeId");
        
        try {
            Registration reg = registrationService.registerForEvent(user, event, false);
            reg.setMobileNumber(mobile);
            reg.setCollegeIdNumber(collegeId);
            
            userService.incrementScore(user, 10);
            
            // Clean up session
            session.removeAttribute("pendingMobile");
            session.removeAttribute("pendingCollegeId");
            
            return "redirect:/payment-success/" + reg.getId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/events";
        }
    }

    @GetMapping("/payment-success/{regId}")
    public String paymentSuccess(@PathVariable Long regId, Model model) {
        User user = getAuthenticatedUser();
        List<Registration> regs = registrationService.getRegistrationsByUser(user);
        Optional<Registration> registration = regs.stream().filter(r -> r.getId().equals(regId)).findFirst();
        
        if (registration.isPresent()) {
            model.addAttribute("registration", registration.get());
            model.addAttribute("successMessage", "Payment Successful & Registration Confirmed");
            return "payment-success";
        }
        return "redirect:/events";
    }

    @GetMapping("/my-events")
    public String myEvents(Model model) {
        User user = getAuthenticatedUser();
        List<Registration> registrations = registrationService.getRegistrationsByUser(user);
        model.addAttribute("registrations", registrations);
        return "my-events";
    }

    @GetMapping("/ticket/{regId}")
    public String viewTicket(@PathVariable Long regId, Model model) {
        User user = getAuthenticatedUser();
        List<Registration> regs = registrationService.getRegistrationsByUser(user);
        Optional<Registration> registration = regs.stream().filter(r -> r.getId().equals(regId)).findFirst();
        
        if (registration.isPresent()) {
            model.addAttribute("registration", registration.get());
            return "ticket";
        }
        return "redirect:/my-events";
    }

    @GetMapping("/participation-email/{regId}")
    public String viewParticipationEmail(@PathVariable Long regId, Model model) {
        User user = getAuthenticatedUser();
        List<Registration> regs = registrationService.getRegistrationsByUser(user);
        Optional<Registration> registration = regs.stream().filter(r -> r.getId().equals(regId)).findFirst();
        
        if (registration.isPresent()) {
            model.addAttribute("registration", registration.get());
            return "participation-email";
        }
        return "redirect:/my-events";
    }

    @GetMapping("/helpdesk")
    public String helpDesk(Model model) {
        model.addAttribute("user", getAuthenticatedUser());
        return "helpdesk";
    }

    @PostMapping("/support")
    public String submitSupportTicket(@RequestParam("message") String message, RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser();
        supportService.submitTicket(user, message);
        redirectAttributes.addFlashAttribute("successMessage", "Your message has been submitted successfully");
        return "redirect:/helpdesk";
    }
    
    @GetMapping("/score-details")
    public String checkScore(Model model) {
        User user = getAuthenticatedUser();
        model.addAttribute("user", user);
        
        List<Registration> registrations = registrationService.getRegistrationsByUser(user);
        model.addAttribute("registrations", registrations);
        model.addAttribute("totalCredit", registrations.size() * 10);
        return "score-details";
    }
}
