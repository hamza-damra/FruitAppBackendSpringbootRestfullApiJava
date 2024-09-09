package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.ProductDTO;
import com.hamza.fruitsappbackend.entity.Category;
import com.hamza.fruitsappbackend.entity.Product;
import com.hamza.fruitsappbackend.entity.Review;
import com.hamza.fruitsappbackend.exception.ProductNotFoundException;
import com.hamza.fruitsappbackend.exception.CategoryNotFoundException;
import com.hamza.fruitsappbackend.repository.ProductRepository;
import com.hamza.fruitsappbackend.repository.CategoryRepository;
import com.hamza.fruitsappbackend.repository.ReviewRepository;
import com.hamza.fruitsappbackend.service.ProductService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import com.hamza.fruitsappbackend.payload.ProductResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final ModelMapper modelMapper;
    private final AuthorizationUtils authorizationUtils;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository,
                              ReviewRepository reviewRepository, ModelMapper modelMapper,
                              AuthorizationUtils authorizationUtils) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.reviewRepository = reviewRepository;
        this.modelMapper = modelMapper;
        this.authorizationUtils = authorizationUtils;
    }

    @Override
    public ProductDTO saveProduct(ProductDTO productDTO, String token) {
        authorizationUtils.checkAdminRole(token);
        Product product = convertToEntity(productDTO);
        setCategory(productDTO, product);
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    @Override
    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    public List<ProductDTO> getProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse getAllProducts(int pageSize, int pageNumber, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> productPage = productRepository.findAll(pageable);
        List<ProductDTO> content = productPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new ProductResponse(
                productPage.getSize(),
                productPage.getNumber(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast(),
                content
        );
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, String token) {
        authorizationUtils.checkAdminRole(token);
        Product existingProduct = findProductById(productDTO.getId());

        updateProductDetails(productDTO, existingProduct);
        setCategory(productDTO, existingProduct);
        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDto(updatedProduct);
    }

    @Override
    public void deleteProductById(Long id, String token) {
        authorizationUtils.checkAdminRole(token);
        Product product = findProductById(id);
        productRepository.delete(product);
    }

    private void setCategory(ProductDTO productDTO, Product product) {
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("id", productDTO.getCategoryId().toString()));
            product.setCategory(category);
        }
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("id", id.toString()));
    }

    private void updateProductDetails(ProductDTO productDTO, Product product) {
        if (productDTO.getName() != null)
            product.setName(productDTO.getName());

        if (productDTO.getDescription() != null)
            product.setDescription(productDTO.getDescription());

        if (productDTO.getPrice() != null)
            product.setPrice(productDTO.getPrice());

        if (productDTO.getStockQuantity() != null)
            product.setStockQuantity(productDTO.getStockQuantity());

        if (productDTO.getCategoryId() != null) {
            Optional<Category> category = categoryRepository.findCategoryById(productDTO.getCategoryId());
            category.ifPresent(product::setCategory);
        }

        if (productDTO.getImageUrl() != null)
            product.setImageUrl(productDTO.getImageUrl());

        if (productDTO.getProductWeight() != null)
            product.setProductWeight(productDTO.getProductWeight());

        if (productDTO.getCalories() != null)
            product.setCalories(productDTO.getCalories());

        if (productDTO.getExpirationDate() != null)
            product.setExpirationDate(productDTO.getExpirationDate());

        if (productDTO.getTotalRating() != null)
            product.setTotalRating(productDTO.getTotalRating());

        if (productDTO.getCounterFiveStars() != null)
            product.setCounterFiveStars(productDTO.getCounterFiveStars());
        if (productDTO.getCounterFourStars() != null)
            product.setCounterFourStars(productDTO.getCounterFourStars());
        if (productDTO.getCounterThreeStars() != null)
            product.setCounterThreeStars(productDTO.getCounterThreeStars());
        if (productDTO.getCounterTwoStars() != null)
            product.setCounterTwoStars(productDTO.getCounterTwoStars());
        if (productDTO.getCounterOneStars() != null)
            product.setCounterOneStars(productDTO.getCounterOneStars());
    }


    private ProductDTO convertToDto(Product product) {
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
        productDTO.setTotalRating(product.getTotalRating());
        productDTO.setCounterFiveStars(product.getCounterFiveStars());
        productDTO.setCounterFourStars(product.getCounterFourStars());
        productDTO.setCounterThreeStars(product.getCounterThreeStars());
        productDTO.setCounterTwoStars(product.getCounterTwoStars());
        productDTO.setCounterOneStars(product.getCounterOneStars());
        return productDTO;
    }

    private Product convertToEntity(ProductDTO productDTO) {
        return modelMapper.map(productDTO, Product.class);
    }

    public void updateProductTotalRating(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);

        double totalRating = reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);

        int fiveStars = (int) reviews.stream().filter(r -> r.getRating() == 5).count();
        int fourStars = (int) reviews.stream().filter(r -> r.getRating() == 4).count();
        int threeStars = (int) reviews.stream().filter(r -> r.getRating() == 3).count();
        int twoStars = (int) reviews.stream().filter(r -> r.getRating() == 2).count();
        int oneStar = (int) reviews.stream().filter(r -> r.getRating() == 1).count();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("id", productId.toString()));
        product.setTotalRating(totalRating);
        product.setCounterFiveStars(fiveStars);
        product.setCounterFourStars(fourStars);
        product.setCounterThreeStars(threeStars);
        product.setCounterTwoStars(twoStars);
        product.setCounterOneStars(oneStar);
        productRepository.save(product);
    }
}
