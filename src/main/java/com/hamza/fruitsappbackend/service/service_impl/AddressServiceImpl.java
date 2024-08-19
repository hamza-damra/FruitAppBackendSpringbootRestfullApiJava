package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.AddressDTO;
import com.hamza.fruitsappbackend.entity.Address;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.exception.AddressNotFoundException;
import com.hamza.fruitsappbackend.exception.UserNotFoundException;
import com.hamza.fruitsappbackend.repository.AddressRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.service.AddressService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        User user = userRepository.findById(address.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException("id", address.getUser().getId().toString()));
        address.setUser(user);
        Address savedAddress = addressRepository.save(address);
        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public Optional<AddressDTO> getAddressById(Long id) {
        return addressRepository.findById(id)
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .or(() -> {
                    throw new AddressNotFoundException("id", id.toString());
                });
    }

    @Override
    public List<AddressDTO> getAddressesByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("id", userId.toString());
        }
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
        Address addressToUpdate = addressRepository.findById(address.getId())
                .orElseThrow(() -> new AddressNotFoundException("id", address.getId().toString()));
        address.setCreatedAt(addressToUpdate.getCreatedAt());
        address.setUser(addressToUpdate.getUser());
        Address updatedAddress = addressRepository.save(address);
        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public void deleteAddressById(Long id) {
        if (!addressRepository.existsById(id)) {
            throw new AddressNotFoundException("id", id.toString());
        }
        addressRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAddressByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("id", userId.toString()));
        addressRepository.deleteAddressByUser(user);
    }

    @Override
    @Transactional
    public AddressDTO updateAddressByUserId(AddressDTO addressDTO) {
        Address newAddress = modelMapper.map(addressDTO, Address.class);
        User user = userRepository.findById(newAddress.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException("id", newAddress.getUser().getId().toString()));
        newAddress.setUser(user);

        Address existingAddress = (Address) addressRepository.findByUserId(newAddress.getUser().getId())
                .orElseThrow(() -> new AddressNotFoundException("userId", newAddress.getUser().getId().toString()));

        newAddress.setId(existingAddress.getId());
        newAddress.setCreatedAt(existingAddress.getCreatedAt());

        Address updatedAddress = addressRepository.save(newAddress);
        return modelMapper.map(updatedAddress, AddressDTO.class);
    }
}
