package com.hamza.fruitsappbackend.modules.address.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hamza.fruitsappbackend.validation.markers.OnCreate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "Phone number is required", groups = OnCreate.class)
    @Pattern(regexp = "^\\+?[0-9]*$", message = "Phone number is invalid")
    @Size(min = 10, max = 15, message = "Phone number should be between 10 and 15 characters")
    private String phoneNumber;

    @NotBlank(message = "Zip code is required", groups = OnCreate.class)
    @Size(min = 5, max = 10, message = "Zip code should be between 5 and 10 characters")
    private String zipCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
