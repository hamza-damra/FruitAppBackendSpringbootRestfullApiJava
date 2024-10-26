package com.hamza.fruitsappbackend.modules.address.repository;

import com.hamza.fruitsappbackend.modules.address.entity.Address;
import com.hamza.fruitsappbackend.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserId(Long userId);

    void deleteAddressByUser(User user);

    boolean existsAddressesByUser(User user);
}
