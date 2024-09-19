package com.hamza.fruitsappbackend.modulus.user.repository;

import com.hamza.fruitsappbackend.modulus.user.entity.Address;
import com.hamza.fruitsappbackend.modulus.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserId(Long userId);

    void deleteAddressByUser(User user);
}
