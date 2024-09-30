package com.hamza.fruitsappbackend.modulus.wishlist.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WishlistResponse {

    private Integer totalItems;

    private List<WishlistDTO> items;

}
