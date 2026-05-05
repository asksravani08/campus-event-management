package com.campusevent.security;

import com.campusevent.model.User;
import com.campusevent.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/", "/login", "/register", "/h2-console/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );

        // Fix for H2 console
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public CommandLineRunner initAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder, com.campusevent.repository.EventRepository eventRepository) {
        return args -> {
            if (!userRepository.existsByEmail("admin@campus.com")) {
                User admin = new User();
                admin.setName("Admin User");
                admin.setEmail("admin@campus.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ROLE_ADMIN");
                userRepository.save(admin);
            }
            if (!userRepository.existsByEmail("student@campus.com")) {
                User student = new User();
                student.setName("Test Student");
                student.setEmail("student@campus.com");
                student.setPassword(passwordEncoder.encode("student123"));
                student.setRole("ROLE_STUDENT");
                userRepository.save(student);
            }
            
            // Add dummy events if none exist
            if (eventRepository.count() == 0) {
                eventRepository.save(new com.campusevent.model.Event("AI Hackathon 2026", "A 48-hour coding marathon focused on Artificial Intelligence solutions.", java.time.LocalDate.now().plusDays(10), "Computer Science", "Competition", 15.0, 50));
                eventRepository.save(new com.campusevent.model.Event("Quantum Physics Lecture", "An introductory session to Quantum Mechanics by Dr. Smith.", java.time.LocalDate.now().plusDays(5), "Physics", "Seminar", 0.0, 100));
                eventRepository.save(new com.campusevent.model.Event("Annual Cultural Fest", "Music, dance, and art from across the globe.", java.time.LocalDate.now().plusDays(20), "Arts", "Cultural", 25.0, 200));
            }
        };
    }
}
