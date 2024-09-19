package com.hamza.fruitsappbackend.modulus.role.repository;


import com.hamza.fruitsappbackend.modulus.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    boolean existsById(@NonNull Long id);
    boolean existsByName(@NonNull String name);
    Set<Role> findByNameIn(Set<String> names);
}