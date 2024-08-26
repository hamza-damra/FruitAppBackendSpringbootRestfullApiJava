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
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
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
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AddressServiceImpl(AddressRepository addressRepository, UserRepository userRepository,
                              ModelMapper modelMapper, AuthorizationUtils authorizationUtils, JwtTokenProvider jwtTokenProvider) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.authorizationUtils = authorizationUtils;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AddressDTO saveAddress(AddressDTO addressDTO, String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        Address address = prepareAddressForSaving(addressDTO, userId);

        handleDefaultAddressLogic(userId, address);

        return convertToDto(addressRepository.save(address));
    }

    @Override
    public Optional<AddressDTO> getAddressById(Long id, String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        Address address = findAddressById(id);
        return Optional.of(convertToDto(address));
    }

    @Override
    public List<AddressDTO> getAddressesByUserId(String token) {
        Long userId = getUserIdFromToken(token);
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
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        Address addressToUpdate = findAddressById(addressDTO.getId());
        Address address = convertToEntity(addressDTO);
        address.setUser(addressToUpdate.getUser());
        address.setCreatedAt(addressToUpdate.getCreatedAt());

        handleDefaultAddressLogic(userId, address);

        return convertToDto(addressRepository.save(address));
    }

    @Override
    public void deleteAddressById(Long id, String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        Address address = findAddressById(id);
        addressRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAddressByUserId(String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        addressRepository.deleteAddressByUser(findUserById(userId));
    }

    @Override
    @Transactional
    public AddressDTO updateAddressByUserId(AddressDTO addressDTO, String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        Address newAddress = convertToEntity(addressDTO);
        Address existingAddress = findAddressByUserId(userId);
        newAddress.setId(existingAddress.getId());
        newAddress.setCreatedAt(existingAddress.getCreatedAt());

        handleDefaultAddressLogic(userId, newAddress);

        return convertToDto(addressRepository.save(newAddress));
    }

    private Long getUserIdFromToken(String token) {
        return Long.valueOf(jwtTokenProvider.getUserIdFromToken(token));
    }

    // Helper method to find Address by ID
    private Address findAddressById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException("id", id.toString()));
    }

    // Helper method to find User by ID
    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("id", id.toString()));
    }

    // Helper method to find Address by User ID
    private Address findAddressByUserId(Long userId) {
        return addressRepository.findByUserId(userId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new AddressNotFoundException("userId", userId.toString()));
    }

    // Helper method to convert Address entity to DTO
    private AddressDTO convertToDto(Address address) {
        return modelMapper.map(address, AddressDTO.class);
    }

    // Helper method to convert AddressDTO to entity
    private Address convertToEntity(AddressDTO addressDTO) {
        return modelMapper.map(addressDTO, Address.class);
    }

    // Helper method to prepare Address for saving
    private Address prepareAddressForSaving(AddressDTO addressDTO, Long userId) {
        Address address = convertToEntity(addressDTO);
        User user = findUserById(userId);
        address.setUser(user);
        return address;
    }

    // Helper method to handle default address logic
    private void handleDefaultAddressLogic(Long userId, Address address) {
        List<Address> userAddresses = addressRepository.findByUserId(userId);

        if (userAddresses.isEmpty()) {
            address.setDefault(true);
        } else if (address.isDefault()) {
            userAddresses.stream()
                    .filter(Address::isDefault)
                    .forEach(existingAddress -> {
                        existingAddress.setDefault(false);
                        addressRepository.save(existingAddress);
                    });
        }
    }
}
