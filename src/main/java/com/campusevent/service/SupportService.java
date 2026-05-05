package com.campusevent.service;

import com.campusevent.model.SupportTicket;
import com.campusevent.model.User;
import com.campusevent.repository.SupportTicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupportService {

    private final SupportTicketRepository supportTicketRepository;

    @Autowired
    public SupportService(SupportTicketRepository supportTicketRepository) {
        this.supportTicketRepository = supportTicketRepository;
    }

    public void submitTicket(User user, String message) {
        SupportTicket ticket = new SupportTicket();
        ticket.setUser(user);
        ticket.setMessage(message);
        supportTicketRepository.save(ticket);
    }

    public List<SupportTicket> getAllPendingTickets() {
        return supportTicketRepository.findByStatus("PENDING");
    }

    public void processTicket(Long id) {
        Optional<SupportTicket> ticketOpt = supportTicketRepository.findById(id);
        if (ticketOpt.isPresent()) {
            SupportTicket ticket = ticketOpt.get();
            ticket.setStatus("PROCESSED");
            supportTicketRepository.save(ticket);
        }
    }
}
