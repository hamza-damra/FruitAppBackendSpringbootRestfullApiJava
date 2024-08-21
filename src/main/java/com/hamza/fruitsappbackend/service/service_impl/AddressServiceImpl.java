package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.AddressDTO;
import com.hamza.fruitsappbackend.entity.Address;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.exception.AddressNotFoundException;
import com.hamza.fruitsappbackend.exception.UserNotFoundException;
import com.hamza.fruitsappbackend.repository.AddressRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.service.AddressService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
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
    private final AuthorizationUtils authorizationUtils;

    @Autowired
    public AddressServiceImpl(AddressRepository addressRepository, UserRepository userRepository,
                              ModelMapper modelMapper, AuthorizationUtils authorizationUtils) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.authorizationUtils = authorizationUtils;
    }

    @Override
    public AddressDTO saveAddress(AddressDTO addressDTO, String token) {
        authorizationUtils.checkUserOrAdminRole(token, addressDTO.getUserId());
        Address address = convertToEntity(addressDTO);
        address.setUser(findUserById(addressDTO.getUserId()));
        return convertToDto(addressRepository.save(address));
    }

    @Override
    public Optional<AddressDTO> getAddressById(Long id, String token) {
        Address address = findAddressById(id);
        authorizationUtils.checkUserOrAdminRole(token, address.getUser().getId());
        return Optional.of(convertToDto(address));
    }

    @Override
    public List<AddressDTO> getAddressesByUserId(Long userId, String token) {
        authorizationUtils.checkUserOrAdminRole(token, userId);
        return addressRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AddressDTO> getAllAddresses(String token) {
        authorizationUtils.checkAdminRole(token);
        return addressRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO updateAddress(AddressDTO addressDTO, String token) {
        authorizationUtils.checkUserOrAdminRole(token, addressDTO.getUserId());
        Address addressToUpdate = findAddressById(addressDTO.getId());
        Address address = convertToEntity(addressDTO);
        address.setUser(addressToUpdate.getUser());
        address.setCreatedAt(addressToUpdate.getCreatedAt());
        return convertToDto(addressRepository.save(address));
    }

    @Override
    public void deleteAddressById(Long id, String token) {
        Address address = findAddressById(id);
        authorizationUtils.checkUserOrAdminRole(token, address.getUser().getId());
        addressRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAddressByUserId(Long userId, String token) {
        authorizationUtils.checkUserOrAdminRole(token, userId);
        addressRepository.deleteAddressByUser(findUserById(userId));
    }

    @Override
    @Transactional
    public AddressDTO updateAddressByUserId(AddressDTO addressDTO, String token) {
        authorizationUtils.checkUserOrAdminRole(token, addressDTO.getUserId());
        Address newAddress = convertToEntity(addressDTO);
        Address existingAddress = findAddressByUserId(addressDTO.getUserId());
        newAddress.setId(existingAddress.getId());
        newAddress.setCreatedAt(existingAddress.getCreatedAt());
        return convertToDto(addressRepository.save(newAddress));
    }

    private Address findAddressById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException("id", id.toString()));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("id", id.toString()));
    }

    private Address findAddressByUserId(Long userId) {
        return addressRepository.findByUserId(userId)
                .orElseThrow(() -> new AddressNotFoundException("userId", userId.toString()));
    }

    private AddressDTO convertToDto(Address address) {
        return modelMapper.map(address, AddressDTO.class);
    }

    private Address convertToEntity(AddressDTO addressDTO) {
        return modelMapper.map(addressDTO, Address.class);
    }
}
