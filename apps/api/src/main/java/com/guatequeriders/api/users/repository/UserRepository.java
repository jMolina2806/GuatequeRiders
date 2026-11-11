package com.guatequeriders.api.users.repository;

import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    boolean existsByMemberNumber(String memberNumber);

    List<User> findAllByOrderByIdAsc();

    long countByRole(Role role);
}
