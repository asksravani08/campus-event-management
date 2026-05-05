package com.campusevent.repository;

import com.campusevent.model.Registration;
import com.campusevent.model.User;
import com.campusevent.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByUser(User user);
    List<Registration> findByEvent(Event event);
    boolean existsByUserAndEvent(User user, Event event);
}
