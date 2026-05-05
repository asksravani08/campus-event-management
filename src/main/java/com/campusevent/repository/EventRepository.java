package com.campusevent.repository;

import com.campusevent.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    @Query("SELECT e FROM Event e WHERE " +
           "LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.department) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.type) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> searchEvents(String keyword);
    
    List<Event> findByDepartment(String department);
    
    List<Event> findByType(String type);
}
