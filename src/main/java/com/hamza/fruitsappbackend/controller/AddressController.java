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
        String jwtToken = token.replace("Bearer ", "");
        AddressDTO savedAddress = addressService.saveAddress(addressDTO, jwtToken);
        return ResponseEntity.ok(savedAddress);
    }

    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<AddressDTO> getAddressById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String jwtToken = token.replace("Bearer ", "");
        Optional<AddressDTO> addressDTO = addressService.getAddressById(id, jwtToken);
        return addressDTO.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressDTO>> getAddressesByUserId(@RequestHeader("Authorization") String token, @PathVariable Long userId) {
        String jwtToken = token.replace("Bearer ", "");
        List<AddressDTO> addresses = addressService.getAddressesByUserId(userId, jwtToken);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/all")
    public ResponseEntity<List<AddressDTO>> getAllAddresses(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        List<AddressDTO> addresses = addressService.getAllAddresses(jwtToken);
        return ResponseEntity.ok(addresses);
    }

    @PutMapping("/update-by-id/{id}")
    public ResponseEntity<AddressDTO> updateAddress(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody @Valid AddressDTO addressDTO) {
        String jwtToken = token.replace("Bearer ", "");
        addressDTO.setId(id);
        AddressDTO updatedAddress = addressService.updateAddress(addressDTO, jwtToken);
        return ResponseEntity.ok(updatedAddress);
    }

    @PutMapping("/update-by-user-id/{userId}")
    public ResponseEntity<AddressDTO> updateAddressByUserId(@RequestHeader("Authorization") String token, @PathVariable Long userId, @RequestBody @Valid AddressDTO addressDTO) {
        String jwtToken = token.replace("Bearer ", "");
        addressDTO.setUserId(userId);
        AddressDTO updatedAddress = addressService.updateAddressByUserId(addressDTO, jwtToken);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/delete-by-id/{id}")
    public ResponseEntity<Void> deleteAddressById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String jwtToken = token.replace("Bearer ", "");
        addressService.deleteAddressById(id, jwtToken);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete-by-user-id/{userId}")
    public ResponseEntity<String> deleteAddressesByUserId(@RequestHeader("Authorization") String token, @PathVariable Long userId) {
        String jwtToken = token.replace("Bearer ", "");
        addressService.deleteAddressByUserId(userId, jwtToken);
        return ResponseEntity.ok("Address has been deleted successfully");
    }
}
