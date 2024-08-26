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
    public ResponseEntity<AddressDTO> createAddress(@RequestHeader("Authorization") String token, @RequestBody @Valid AddressDTO addressDTO) {
        AddressDTO savedAddress = addressService.saveAddress(addressDTO, token);
        return ResponseEntity.ok(savedAddress);
    }

    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<AddressDTO> getAddressById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Optional<AddressDTO> addressDTO = addressService.getAddressById(id, token);
        return addressDTO.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user")
    public ResponseEntity<List<AddressDTO>> getAddressesByUserId(@RequestHeader("Authorization") String token) {
        List<AddressDTO> addresses = addressService.getAddressesByUserId(token); // No userId parameter needed
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/all")
    public ResponseEntity<List<AddressDTO>> getAllAddresses(@RequestHeader("Authorization") String token) {
        List<AddressDTO> addresses = addressService.getAllAddresses(token);
        return ResponseEntity.ok(addresses);
    }

    @PutMapping("/update-by-id/{id}")
    public ResponseEntity<AddressDTO> updateAddress(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody @Valid AddressDTO addressDTO) {
        addressDTO.setId(id);
        AddressDTO updatedAddress = addressService.updateAddress(addressDTO, token);
        return ResponseEntity.ok(updatedAddress);
    }

    @PutMapping("/update-by-user-id")
    public ResponseEntity<AddressDTO> updateAddressByUserId(@RequestHeader("Authorization") String token, @RequestBody @Valid AddressDTO addressDTO) {
        AddressDTO updatedAddress = addressService.updateAddressByUserId(addressDTO, token);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/delete-by-id/{id}")
    public ResponseEntity<Void> deleteAddressById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        addressService.deleteAddressById(id, token);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete-by-user-id")
    public ResponseEntity<String> deleteAddressesByUserId(@RequestHeader("Authorization") String token) {
        addressService.deleteAddressByUserId(token);
        return ResponseEntity.ok("Address has been deleted successfully");
    }
}
