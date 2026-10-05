package com.securecms.config;

import com.securecms.entity.*;
import com.securecms.repository.*;
import com.securecms.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            log.info("Data already initialized. Skipping...");
            return;
        }

        log.info("=== Initializing sample data ===");

        Role adminRole = roleRepository.save(Role.builder().name("ADMIN").build());
        Role userRole = roleRepository.save(Role.builder().name("USER").build());

        User admin = userRepository.save(User.builder()
                .username("admin")
                .email("admin@securecms.com")
                .password(passwordEncoder.encode("admin123"))
                .encryptedPhone(encryptionService.encrypt("9999999999"))
                .role(adminRole)
                .enabled(true)
                .build());

        User user1 = userRepository.save(User.builder()
                .username("vineet")
                .email("vineet@example.com")
                .password(passwordEncoder.encode("user123"))
                .encryptedPhone(encryptionService.encrypt("9876543210"))
                .role(userRole)
                .enabled(true)
                .build());

        User user2 = userRepository.save(User.builder()
                .username("rahul")
                .email("rahul@example.com")
                .password(passwordEncoder.encode("user123"))
                .encryptedPhone(encryptionService.encrypt("9123456780"))
                .role(userRole)
                .enabled(true)
                .build());

        Category tech = categoryRepository.save(Category.builder()
                .name("Technology")
                .description("Articles about technology and programming")
                .build());

        Category lifestyle = categoryRepository.save(Category.builder()
                .name("Lifestyle")
                .description("Lifestyle and personal development")
                .build());

        Category education = categoryRepository.save(Category.builder()
                .name("Education")
                .description("Educational content and tutorials")
                .build());

        List<Post> posts = List.of(
                buildPost("Introduction to Spring Boot", "Spring Boot makes Java development easy...", admin, tech),
                buildPost("Understanding JWT Authentication", "JWT is a compact way to securely transmit information...", admin, tech),
                buildPost("What is N+1 Query Problem?", "The N+1 problem occurs when an ORM executes one query...", admin, tech),
                buildPost("Caching Strategies in Spring", "Spring Cache abstraction supports multiple providers...", admin, tech),
                buildPost("AES Encryption Explained", "AES is a symmetric encryption algorithm...", admin, tech),
                buildPost("Role Based Access Control", "RBAC restricts system access based on roles...", admin, tech),
                buildPost("Building REST APIs the Right Way", "Follow REST principles for clean APIs...", user1, tech),
                buildPost("Pagination and Sorting Best Practices", "Always limit page size and validate sort fields...", user1, tech),
                buildPost("Logging and Correlation IDs", "Correlation IDs help trace requests across services...", user1, tech),
                buildPost("Why DTOs Matter", "Never expose JPA entities directly from controllers...", user1, tech),
                buildPost("My First Day with Java", "Starting the journey of learning Java...", user2, education),
                buildPost("How to Prepare for Viva", "Focus on concepts rather than memorizing code...", user2, education),
                buildPost("Daily Habits for Developers", "Consistency beats intensity...", user2, lifestyle),
                buildPost("Work Life Balance in IT", "Taking breaks improves productivity...", user1, lifestyle),
                buildPost("Clean Code Principles", "Readable code is more important than clever code...", admin, tech),
                buildPost("Database Indexing Basics", "Indexes speed up SELECT but slow down INSERT...", admin, tech),
                buildPost("Exception Handling in Spring", "Use @ControllerAdvice for centralized handling...", user1, tech),
                buildPost("Security Best Practices", "Never log passwords or tokens...", admin, tech),
                buildPost("Understanding Hibernate Lazy Loading", "Lazy loading can cause LazyInitializationException...", user2, tech),
                buildPost("Final Thoughts on Spring Boot", "Spring Boot is a powerful framework for backend...", admin, education)
        );

        postRepository.saveAll(posts);

        log.info("=== Sample data created successfully ===");
        log.info("Admin  -> username: admin  | password: admin123");
        log.info("User1  -> username: vineet | password: user123");
        log.info("User2  -> username: rahul  | password: user123");
        log.info("Total posts created: {}", posts.size());
    }

    private Post buildPost(String title, String content, User author, Category category) {
        return Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .category(category)
                .build();
    }
}
