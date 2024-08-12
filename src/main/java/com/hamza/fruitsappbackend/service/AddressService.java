package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.AddressDTO;

import java.util.List;
import java.util.Optional;

public interface AddressService {

    AddressDTO saveAddress(AddressDTO addressDTO);

    Optional<AddressDTO> getAddressById(Long id);

    List<AddressDTO> getAddressesByUserId(Long userId);

    List<AddressDTO> getAllAddresses();

    AddressDTO updateAddress(AddressDTO addressDTO);

    void deleteAddressById(Long id);
}
