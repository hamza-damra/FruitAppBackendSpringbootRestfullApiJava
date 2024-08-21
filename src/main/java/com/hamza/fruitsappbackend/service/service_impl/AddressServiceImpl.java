package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.AddressDTO;
import com.hamza.fruitsappbackend.entity.Address;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.exception.AddressNotFoundException;
import com.hamza.fruitsappbackend.exception.UserNotFoundException;
import com.hamza.fruitsappbackend.repository.AddressRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
import com.hamza.fruitsappbackend.service.AddressService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ModelMapper modelMapper;

    @Autowired
    public AddressServiceImpl(AddressRepository addressRepository, UserRepository userRepository, JwtTokenProvider jwtTokenProvider, ModelMapper modelMapper) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.modelMapper = modelMapper;
    }

    private void checkUserRole(User user) {
        if (user.getRoles().stream()
                .noneMatch(role -> role.getName().equals("ROLE_USER") || role.getName().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have the necessary permissions to perform this operation");
        }
    }

    private void verifyUserOrAdmin(String token, Long userId) {
        String username = jwtTokenProvider.getUserNameFromToken(token);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        checkUserRole(user);

        if (!user.getId().equals(userId) && user.getRoles().stream()
                .noneMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to perform this operation");
        }
    }

    @Override
    public AddressDTO saveAddress(AddressDTO addressDTO, String token) {
        verifyUserOrAdmin(token, addressDTO.getUserId());

        Address address = modelMapper.map(addressDTO, Address.class);
        User user = userRepository.findById(address.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException("id", address.getUser().getId().toString()));
        address.setUser(user);
        Address savedAddress = addressRepository.save(address);
        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public Optional<AddressDTO> getAddressById(Long id, String token) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException("id", id.toString()));
        verifyUserOrAdmin(token, address.getUser().getId());
        return Optional.of(modelMapper.map(address, AddressDTO.class));
    }

    @Override
    public List<AddressDTO> getAddressesByUserId(Long userId, String token) {
        verifyUserOrAdmin(token, userId);
        return addressRepository.findByUserId(userId).stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AddressDTO> getAllAddresses(String token) {
        String username = jwtTokenProvider.getUserNameFromToken(token);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        if (user.getRoles().stream().noneMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have the necessary permissions to perform this operation");
        }

        return addressRepository.findAll().stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO updateAddress(AddressDTO addressDTO, String token) {
        verifyUserOrAdmin(token, addressDTO.getUserId());

        Address address = modelMapper.map(addressDTO, Address.class);
        Address addressToUpdate = addressRepository.findById(address.getId())
                .orElseThrow(() -> new AddressNotFoundException("id", address.getId().toString()));
        address.setCreatedAt(addressToUpdate.getCreatedAt());
        address.setUser(addressToUpdate.getUser());
        Address updatedAddress = addressRepository.save(address);
        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public void deleteAddressById(Long id, String token) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException("id", id.toString()));
        verifyUserOrAdmin(token, address.getUser().getId());
        addressRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAddressByUserId(Long userId, String token) {
        verifyUserOrAdmin(token, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("id", userId.toString()));
        addressRepository.deleteAddressByUser(user);
    }

    @Override
    @Transactional
    public AddressDTO updateAddressByUserId(AddressDTO addressDTO, String token) {
        verifyUserOrAdmin(token, addressDTO.getUserId());

        Address newAddress = modelMapper.map(addressDTO, Address.class);
        User user = userRepository.findById(newAddress.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException("id", newAddress.getUser().getId().toString()));
        newAddress.setUser(user);

        Address existingAddress = addressRepository.findByUserId(newAddress.getUser().getId())
                .orElseThrow(() -> new AddressNotFoundException("userId", newAddress.getUser().getId().toString()));

        newAddress.setId(existingAddress.getId());
        newAddress.setCreatedAt(existingAddress.getCreatedAt());

        Address updatedAddress = addressRepository.save(newAddress);
        return modelMapper.map(updatedAddress, AddressDTO.class);
    }
}
