package com.hamza.fruitsappbackend.modulus.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hamza.fruitsappbackend.validation.markers.OnCreate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private Long id;

    @NotBlank(message = "Full name is required", groups = OnCreate.class)
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "City is required", groups = OnCreate.class)
    @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
    private String city;

    @NotBlank(message = "Street address is required", groups = OnCreate.class)
    @Size(min = 5, max = 255, message = "Street address must be between 5 and 255 characters")
    private String streetAddress;

    @Size(max = 50, message = "Apartment number must be up to 50 characters", groups = OnCreate.class)
    private String apartmentNumber;

    @Size(max = 50, message = "Floor number must be up to 50 characters", groups = OnCreate.class)
    private String floorNumber;

    private boolean isDefault;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
