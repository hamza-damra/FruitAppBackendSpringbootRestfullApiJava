package com.hamza.fruitsappbackend.controller;

import com.hamza.fruitsappbackend.constant.Strings;
import com.hamza.fruitsappbackend.dto.ProductDTO;
import com.hamza.fruitsappbackend.payload.ProductResponse;
import com.hamza.fruitsappbackend.service.ProductService;
import com.hamza.fruitsappbackend.validators.markers.OnCreate;
import com.hamza.fruitsappbackend.validators.markers.OnUpdate;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final List<SseEmitter> emitters = new ArrayList<>();

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/add")
    public ResponseEntity<ProductDTO> createProduct(@RequestHeader("Authorization") String token,
                                                    @RequestBody @Validated(OnCreate.class) ProductDTO productDTO) {
        ProductDTO savedProduct = productService.saveProduct(productDTO, token);
        sendProductUpdate(savedProduct);
        return ResponseEntity.ok(savedProduct);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Optional<ProductDTO> productDTO = productService.getProductById(token, id);
        return productDTO.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategoryId(@RequestHeader("Authorization") String token,
                                                                    @PathVariable Long categoryId) {
        List<ProductDTO> products = productService.getProductsByCategoryId(token, categoryId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/all")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestHeader("Authorization") String token,
            @RequestParam(name = "pageSize", defaultValue = Strings.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "pageNumber", defaultValue = Strings.DEFAULT_PAGE_NUMBER, required = false) int pageNumber,
            @RequestParam(name = "sortBy", defaultValue = Strings.DEFAULT_SORT_FIELD, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = Strings.DEFAULT_SORT_DIRECTION, required = false) String sortDirection) {
        ProductResponse products = productService.getAllProducts(token, pageSize, pageNumber, sortBy, sortDirection);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@RequestHeader("Authorization") String token,
                                                    @PathVariable Long id,
                                                    @RequestBody @Validated(OnUpdate.class) ProductDTO productDTO) {
        productDTO.setId(id);
        ProductDTO updatedProduct = productService.updateProduct(productDTO, token);
        sendProductUpdate(updatedProduct);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        productService.deleteProductById(id, token);
        sendProductUpdate(null);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(3600000L);
        this.emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        return emitter;
    }

    private void sendProductUpdate(@Valid ProductDTO productDTO) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("product-update")
                        .data(productDTO, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }

        emitters.removeAll(deadEmitters);
    }
}
