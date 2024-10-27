package com.hamza.fruitsappbackend.modules.address.service;

import com.hamza.fruitsappbackend.modules.address.dto.AddressDTO;

import java.util.List;
import java.util.Optional;

public interface AddressService {

    AddressDTO addAddress(AddressDTO addressDTO, String token);

    Optional<AddressDTO> getAddressById(Long id, String token);

    List<AddressDTO> getAddressesByUserId(String token);

    List<AddressDTO> getAllAddresses(String token);

    AddressDTO updateAddressByUserTokenAndAddressId(Long addressId, AddressDTO addressDTO, String token);

    void deleteAddressById(Long id, String token);

    void deleteAddressByUserId(String token);

    AddressDTO updateAddressByUserId(AddressDTO addressDTO, String token);
}
