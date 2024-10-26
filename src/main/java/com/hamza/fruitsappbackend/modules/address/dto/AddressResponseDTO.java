package com.hamza.fruitsappbackend.modules.address.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponseDTO {

    private Integer totalAddresses = 0;

    private List<AddressDTO> addresses = new ArrayList<>();
}
