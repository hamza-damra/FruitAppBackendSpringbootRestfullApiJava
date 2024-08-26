package com.hamza.fruitsappbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "product_weight", nullable = false)
    private double productWeight;

    @Column(nullable = false)
    private int calories;

    @Column(name = "expiration_date", nullable = true)
    private LocalDateTime expirationDate;

    @Column(name = "total_rating", nullable = false)
    private Double totalRating = 0.0;

    @Column(name = "counter_five_stars", nullable = false)
    private int counterFiveStars = 0;

    @Column(name = "counter_four_stars", nullable = false)
    private int counterFourStars = 0;

    @Column(name = "counter_three_stars", nullable = false)
    private int counterThreeStars = 0;

    @Column(name = "counter_two_stars", nullable = false)
    private int counterTwoStars = 0;

    @Column(name = "counter_one_stars", nullable = false)
    private int counterOneStars = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
