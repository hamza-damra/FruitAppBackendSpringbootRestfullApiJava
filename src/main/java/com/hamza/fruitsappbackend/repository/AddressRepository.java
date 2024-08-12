package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Object> findByUserId(Long userId);
}
