package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.AddressDTO;

import java.util.List;
import java.util.Optional;

public interface AddressService {

    AddressDTO saveAddress(AddressDTO addressDTO, String token);

    Optional<AddressDTO> getAddressById(Long id, String token);

    List<AddressDTO> getAddressesByUserId(Long userId, String token);

    List<AddressDTO> getAllAddresses(String token);

    AddressDTO updateAddress(AddressDTO addressDTO, String token);

    void deleteAddressById(Long id, String token);

    void deleteAddressByUserId(Long userId, String token);

    AddressDTO updateAddressByUserId(AddressDTO addressDTO, String token);
}
