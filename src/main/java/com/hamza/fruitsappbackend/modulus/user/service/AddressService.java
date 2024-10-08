package com.hamza.fruitsappbackend.modulus.user.service;

import com.hamza.fruitsappbackend.modulus.user.dto.AddressDTO;

import java.util.List;
import java.util.Optional;

public interface AddressService {

    AddressDTO saveAddress(AddressDTO addressDTO, String token);

    Optional<AddressDTO> getAddressById(Long id, String token);

    List<AddressDTO> getAddressesByUserId(String token);

    List<AddressDTO> getAllAddresses(String token);

    AddressDTO updateAddress(AddressDTO addressDTO, String token);

    void deleteAddressById(Long id, String token);

    void deleteAddressByUserId(String token);

    AddressDTO updateAddressByUserId(AddressDTO addressDTO, String token);
}
