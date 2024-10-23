package com.hamza.fruitsappbackend.modules.product.controller;
import com.hamza.fruitsappbackend.constant.Strings;
import com.hamza.fruitsappbackend.modules.product.dto.ProductDTO;
import com.hamza.fruitsappbackend.modules.product.dto.ProductResponse;
import com.hamza.fruitsappbackend.modules.product.service.ProductService;
import com.hamza.fruitsappbackend.validation.markers.OnCreate;
import com.hamza.fruitsappbackend.validation.markers.OnUpdate;
import com.hamza.fruitsappbackend.websocket.WebSocketProductHandler;
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
    private final WebSocketProductHandler webSocketProductHandler;


    @Autowired
    public ProductController(ProductService productService, WebSocketProductHandler webSocketProductHandler) {
        this.productService = productService;
        this.webSocketProductHandler = webSocketProductHandler;
    }

    @PostMapping("/add")
    public ResponseEntity<ProductDTO> createProduct(@RequestHeader("Authorization") String token,
                                                    @RequestBody @Validated(OnCreate.class) ProductDTO productDTO) {
        ProductDTO savedProduct = productService.addProduct(productDTO, token);
        sendProductUpdate(savedProduct);
        webSocketProductHandler.sendProductUpdate(savedProduct);
        return ResponseEntity.ok(savedProduct);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Optional<ProductDTO> productDTO = productService.getProductById(token, id);
        return productDTO.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/filtered")
    public ResponseEntity<ProductResponse> getProductsByPriceRange(
            @RequestHeader("Authorization") String token,
            @RequestParam(name = "minPrice", defaultValue = "0", required = false) double minPrice,
            @RequestParam(name = "maxPrice", defaultValue = "10000", required = false) double maxPrice,
            @RequestParam(name = "itemsPerPage", defaultValue = Strings.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "currentPage", defaultValue = Strings.DEFAULT_PAGE_NUMBER, required = false) int pageNumber) {
        ProductResponse productResponse = productService.getProductsByPriceRange(token, minPrice, maxPrice, pageSize, pageNumber);
        return ResponseEntity.ok(productResponse);
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
            @RequestParam(name = "itemsPerPage", defaultValue = Strings.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "currentPage", defaultValue = Strings.DEFAULT_PAGE_NUMBER, required = false) int pageNumber,
            @RequestParam(name = "sortBy", defaultValue = Strings.DEFAULT_SORT_FIELD, required = false) String sortBy,
            @RequestParam(name = "sortDir", defaultValue = Strings.DEFAULT_SORT_DIRECTION, required = false) String sortDirection) {
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
        webSocketProductHandler.sendProductUpdate(updatedProduct);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        productService.deleteProductById(id, token);
        sendProductUpdate(null);
        webSocketProductHandler.sendProductUpdate(null);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllProducts(@RequestHeader("Authorization") String token) {
        productService.deleteAllProducts(token);
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

    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(
            @RequestParam("keyword") String keyword,
            @RequestHeader("Authorization") String token) {

        List<ProductDTO> productDTOs = productService.searchProducts(keyword, token);
        return ResponseEntity.ok(productDTOs);
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
