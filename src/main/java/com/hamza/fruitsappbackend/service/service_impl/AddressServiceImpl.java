package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.AddressDTO;
import com.hamza.fruitsappbackend.entity.Address;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.repository.AddressRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.service.AddressService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public AddressServiceImpl(AddressRepository addressRepository, UserRepository userRepository, ModelMapper modelMapper) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public AddressDTO saveAddress(AddressDTO addressDTO) {
        Address address = modelMapper.map(addressDTO, Address.class);
        Optional<User> user = userRepository.findUserById(address.getUser().getId());
        if(user.isEmpty())
        {
            throw new RuntimeException("User not found");
        }
        Address savedAddress = addressRepository.save(address);
        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public Optional<AddressDTO> getAddressById(Long id) {
        return addressRepository.findById(id)
                .map(address -> modelMapper.map(address, AddressDTO.class));
    }

    @Override
    public List<AddressDTO> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AddressDTO> getAllAddresses() {
        return addressRepository.findAll().stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO updateAddress(AddressDTO addressDTO) {
        Address address = modelMapper.map(addressDTO, Address.class);
        Address addressToUpdate = addressRepository.getReferenceById(address.getId());
        address.setCreatedAt(addressToUpdate.getCreatedAt());
        Address updatedAddress = addressRepository.save(address);
        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public void deleteAddressById(Long id) {
        addressRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAddressByUserId(Long userId) {
        Optional<User> userOptional = userRepository.findUserById(userId);
        if(userOptional.isPresent())
        {
            addressRepository.deleteAddressByUser(userOptional.get());
        }else{
            throw new RuntimeException("User not found");
        }

    }

    @Override
    @Transactional
    public AddressDTO updateAddressByUserId(AddressDTO addressDTO) {
        Address newAddress = modelMapper.map(addressDTO, Address.class);

        Optional<Object> existingAddressOpt = addressRepository.findByUserId(newAddress.getUser().getId());

        if (existingAddressOpt.isPresent()) {
            Address existingAddress = (Address) existingAddressOpt.get();

            newAddress.setId(existingAddress.getId());
            newAddress.setCreatedAt(existingAddress.getCreatedAt());

            Address updatedAddress = addressRepository.save(newAddress);

            return modelMapper.map(updatedAddress, AddressDTO.class);
        } else {
            throw new EntityNotFoundException("Address not found for user ID: " + newAddress.getUser().getId());
        }
    }

}
