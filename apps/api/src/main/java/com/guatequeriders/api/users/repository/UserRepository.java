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

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    boolean existsByMemberNumber(String memberNumber);

    boolean existsByMemberNumberAndIdNot(String memberNumber, Long id);

    List<User> findAllByOrderByIdAsc();

    long countByRole(Role role);
}
