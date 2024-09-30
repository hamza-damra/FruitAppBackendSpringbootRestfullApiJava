package com.hamza.fruitsappbackend.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hamza.fruitsappbackend.modulus.product.dto.ProductDTO;

import java.io.IOException;

public class ProductDTOSerializer extends JsonSerializer<ProductDTO> {
    @Override
    public void serialize(ProductDTO productDTO, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("id", productDTO.getId());
        gen.writeStringField("name", productDTO.getName());
        gen.writeStringField("addedAt", productDTO.getAddedAt().toString());
        gen.writeStringField("description", productDTO.getDescription());
        gen.writeNumberField("price", productDTO.getPrice());
        gen.writeNumberField("stockQuantity", productDTO.getStockQuantity());
        gen.writeStringField("imageUrl", productDTO.getImageUrl());
        gen.writeNumberField("orderCount", productDTO.getOrderCount() == null? 0 : productDTO.getOrderCount());
        gen.writeNumberField("categoryId", productDTO.getCategoryId());
        gen.writeNumberField("quantityInCart", productDTO.getQuantityInCart() == null? 0 : productDTO.getQuantityInCart());
        gen.writeNumberField("productWeight", productDTO.getProductWeight());
        gen.writeNumberField("caloriesPer100Grams", productDTO.getCaloriesPer100Grams());
        gen.writeStringField("expirationDate", productDTO.getExpirationDate().toString());
        gen.writeBooleanField("isFavorite", productDTO.isFavorite());
        gen.writeBooleanField("isInCart", productDTO.isInCart());
        gen.writeNumberField("likeCount", productDTO.getLikeCount());
        gen.writeNumberField("totalRating", productDTO.getTotalRating());
        gen.writeNumberField("counterFiveStars", productDTO.getCounterFiveStars());
        gen.writeNumberField("counterFourStars", productDTO.getCounterFourStars());
        gen.writeNumberField("counterThreeStars", productDTO.getCounterThreeStars());
        gen.writeNumberField("counterTwoStars", productDTO.getCounterTwoStars());
        gen.writeNumberField("counterOneStars", productDTO.getCounterOneStars());
        gen.writeEndObject();
    }
}
