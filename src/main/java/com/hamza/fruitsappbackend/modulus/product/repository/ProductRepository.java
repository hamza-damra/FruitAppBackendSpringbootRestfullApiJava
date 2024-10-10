package com.hamza.fruitsappbackend.modulus.product.repository;

import com.hamza.fruitsappbackend.modulus.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryId(Long categoryId);

    @Query("SELECT product FROM Product product WHERE LOWER(product.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByName(String keyword);

    @Query("SELECT product FROM Product product WHERE LOWER(product.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByDescription(String keyword);


    @Query("SELECT product FROM Product product WHERE product.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(@Param("minPrice") double minPrice,
                                   @Param("maxPrice") double maxPrice,
                                   Pageable pageable);

    void deleteAllByCategoryId(Long id);
}
