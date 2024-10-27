package com.hamza.fruitsappbackend.modules.address.service_impl;

import com.hamza.fruitsappbackend.exception.global.BadRequestException;
import com.hamza.fruitsappbackend.modules.address.dto.AddressDTO;
import com.hamza.fruitsappbackend.modules.address.entity.Address;
import com.hamza.fruitsappbackend.modules.user.entity.User;
import com.hamza.fruitsappbackend.modules.address.exception.AddressNotFoundException;
import com.hamza.fruitsappbackend.modules.user.exception.UserNotFoundException;
import com.hamza.fruitsappbackend.modules.address.repository.AddressRepository;
import com.hamza.fruitsappbackend.modules.user.repository.UserRepository;
import com.hamza.fruitsappbackend.modules.address.service.AddressService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final AuthorizationUtils authorizationUtils;

    @Autowired
    public AddressServiceImpl(AddressRepository addressRepository, UserRepository userRepository,
                              ModelMapper modelMapper, AuthorizationUtils authorizationUtils, JwtTokenProvider jwtTokenProvider) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.authorizationUtils = authorizationUtils;
    }

    @Override
    @Transactional
    public AddressDTO addAddress(AddressDTO addressDTO, String token) {
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
    @Transactional
    public AddressDTO updateAddressByUserTokenAndAddressId(Long addressId, AddressDTO addressDTO, String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        Address addressToUpdate = findAddressById(addressId);
        if (!addressToUpdate.getUser().getId().equals(userId)) {
            throw new AddressNotFoundException("id", addressDTO.getId().toString());
        }

        updateAddressFields(addressToUpdate, addressDTO);

        handleDefaultAddressLogic(userId, addressToUpdate);

        return convertToDto(addressRepository.save(addressToUpdate));
    }

    private void updateAddressFields(Address addressToUpdate, AddressDTO addressDTO) {
        updateIfPresent(addressDTO.getCity(), addressToUpdate::setCity);
        updateIfPresent(addressDTO.getStreetAddress(), addressToUpdate::setStreetAddress);
        updateIfPresent(addressDTO.getFullName(), addressToUpdate::setFullName);
        updateIfPresent(addressDTO.getApartmentNumber(), addressToUpdate::setApartmentNumber);
        updateIfPresent(addressDTO.getFloorNumber(), addressToUpdate::setFloorNumber);
        updateIfPresent(addressDTO.getPhoneNumber(), addressToUpdate::setPhoneNumber);
        updateIfPresent(addressDTO.isDefault(), addressToUpdate::setDefault);
        updateIfPresent(addressDTO.getZipCode(), addressToUpdate::setZipCode);
    }

    private <T> void updateIfPresent(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }


    @Override
    public void deleteAddressById(Long id, String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);
        Address address = findAddressById(id);
        if (!address.getUser().getId().equals(userId)) {
            throw new AddressNotFoundException("id", id.toString());
        }

        if (address.isDefault()) {
            List<Address> userAddresses = addressRepository.findByUserId(userId);
            if (!userAddresses.isEmpty()) {
                Address newDefault = userAddresses.stream()
                        .filter(a -> !a.getId().equals(id))
                        .findFirst()
                        .orElse(null);

                if (newDefault != null) {
                    newDefault.setDefault(true);
                    addressRepository.save(newDefault);
                }
            }
        }

        addressRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAddressByUserId(String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);
        if (!addressRepository.existsAddressesByUser(userRepository.getUserById(userId))) {
            throw new AddressNotFoundException("userId", userId.toString());
        }
        addressRepository.deleteAddressByUser(findUserById(userId));
    }

    @Override
    @Transactional
    public AddressDTO updateAddressByUserId(AddressDTO addressDTO, String token) {
        Long userId = getUserIdFromToken(token);
        authorizationUtils.checkUserOrAdminRole(token, userId);

        Address existingAddress = findAddressByUserId(userId);
        Address updatedAddress = convertToEntity(addressDTO);
        updatedAddress.setId(existingAddress.getId());
        updatedAddress.setCreatedAt(existingAddress.getCreatedAt());
        handleDefaultAddressLogic(userId, updatedAddress);

        return convertToDto(addressRepository.save(updatedAddress));
    }

    private Long getUserIdFromToken(String token) {
        return authorizationUtils.getUserFromToken(token).getId();
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
                .stream()
                .findFirst()
                .orElseThrow(() -> new AddressNotFoundException("userId", userId.toString()));
    }

    private AddressDTO convertToDto(Address address) {
        return modelMapper.map(address, AddressDTO.class);
    }

    private Address convertToEntity(AddressDTO addressDTO) {
        return modelMapper.map(addressDTO, Address.class);
    }

    private Address prepareAddressForSaving(AddressDTO addressDTO, Long userId) {
        Address address = convertToEntity(addressDTO);
        User user = findUserById(userId);
        address.setUser(user);
        return address;
    }

    private void handleDefaultAddressLogic(Long userId, Address address) {

        List<Address> userAddresses = addressRepository.findByUserId(userId);

        if(userAddresses.size() == 1 && !address.isDefault()){
             throw new BadRequestException("You can't change default address if you have only one address.");
        }
        if (userAddresses.isEmpty()) {
            address.setDefault(true);
        } else if (address.isDefault()) {
            userAddresses.stream()
                    .filter(Address::isDefault)
                    .filter(existingAddress -> !existingAddress.getId().equals(address.getId()))  // Unset other default addresses
                    .forEach(existingAddress -> {
                        existingAddress.setDefault(false);
                        addressRepository.save(existingAddress);
                    });
        }
    }

}
