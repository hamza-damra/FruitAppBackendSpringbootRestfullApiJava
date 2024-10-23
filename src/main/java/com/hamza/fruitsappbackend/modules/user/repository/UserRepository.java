package com.hamza.fruitsappbackend.modules.user.repository;

import com.hamza.fruitsappbackend.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.accountVerification.otp = :otp")
    Optional<User> findByOtp(Integer otp);

    User getUserById(Long userId);

}
