package com.hamza.fruitsappbackend.controller;

import com.hamza.fruitsappbackend.dto.AddressDTO;
import com.hamza.fruitsappbackend.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/addresses")
public class AddressController {

    private final AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/create")
    public ResponseEntity<AddressDTO> createAddress(@RequestBody @Valid AddressDTO addressDTO) {
        AddressDTO savedAddress = addressService.saveAddress(addressDTO);
        return ResponseEntity.ok(savedAddress);
    }

    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long id) {
        Optional<AddressDTO> addressDTO = addressService.getAddressById(id);
        return addressDTO.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressDTO>> getAddressesByUserId(@PathVariable Long userId) {
        List<AddressDTO> addresses = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("all")
    public ResponseEntity<List<AddressDTO>> getAllAddresses() {
        List<AddressDTO> addresses = addressService.getAllAddresses();
        return ResponseEntity.ok(addresses);
    }

    @PutMapping("/update-by-id/{id}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long id, @RequestBody @Valid AddressDTO addressDTO) {
        addressDTO.setId(id);
        AddressDTO updatedAddress = addressService.updateAddress(addressDTO);
        return ResponseEntity.ok(updatedAddress);
    }

    @PutMapping("/update-by-user-id/{id}")
    public ResponseEntity<AddressDTO> updateAddressByUserId(@PathVariable("id") Long userId, @RequestBody @Valid AddressDTO addressDTO) {
        addressDTO.setUserId(userId);
        AddressDTO updatedAddress = addressService.updateAddressByUserId(addressDTO);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/delete-by-id/{id}")
    public ResponseEntity<Void> deleteAddressById(@PathVariable Long id) {
        addressService.deleteAddressById(id);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/delete-by-user-id/{userId}")
    public ResponseEntity<String> deleteAddressesByUserId(@PathVariable Long userId) {
        addressService.deleteAddressByUserId(userId);
        return ResponseEntity.ok("Address has been deleted successfully");
    }

}
