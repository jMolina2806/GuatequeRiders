package com.guatequeriders.api.users.repository;

import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final List<User> users = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final PasswordEncoder passwordEncoder;

    public InMemoryUserRepository(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        users.add(new User(
                idCounter.getAndIncrement(),
                "admin",
                "admin@guatequeriders.com",
                passwordEncoder.encode("admin123"),
                Role.ADMIN,
                LocalDateTime.now()
        ));
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idCounter.getAndIncrement());
        }
        users.removeIf(u -> u.getId().equals(user.getId()));
        users.add(user);
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users);
    }
}
