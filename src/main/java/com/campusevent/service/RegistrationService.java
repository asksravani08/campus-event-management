package com.campusevent.service;

import com.campusevent.model.Event;
import com.campusevent.model.Registration;
import com.campusevent.model.User;
import com.campusevent.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;

    @Autowired
    public RegistrationService(RegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    public Registration registerForEvent(User user, Event event, boolean applyCoupon) {
        if (registrationRepository.existsByUserAndEvent(user, event)) {
            throw new RuntimeException("You are already registered for this event.");
        }
        if (event.getMaxCapacity() != null && event.getCurrentRegistrations() >= event.getMaxCapacity()) {
            throw new RuntimeException("Sorry, this event is Sold Out!");
        }

        Registration registration = new Registration();
        registration.setUser(user);
        registration.setEvent(event);
        
        double finalPrice = event.getPrice();
        if (finalPrice == 0.0) {
            registration.setEventPassword(java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        } else if (applyCoupon) {
            finalPrice = finalPrice * 0.8; // 20% discount
            registration.setCouponApplied(true);
        } else {
            registration.setCouponApplied(false);
        }
        registration.setFinalPrice(finalPrice);
        
        event.setCurrentRegistrations(event.getCurrentRegistrations() + 1);

        return registrationRepository.save(registration);
    }

    public List<Registration> getRegistrationsByUser(User user) {
        return registrationRepository.findByUser(user);
    }

    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }
}
