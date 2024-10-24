package com.hamza.fruitsappbackend.modules.product.entity;

import com.hamza.fruitsappbackend.modules.cart.entity.CartItem;
import com.hamza.fruitsappbackend.modules.order.entity.OrderItem;
import com.hamza.fruitsappbackend.modules.review.entity.Review;
import com.hamza.fruitsappbackend.modules.wishlist.entity.Wishlist;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "products", indexes = {
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_price", columnList = "price"),
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_created_at", columnList = "created_at"),
        @Index(name = "idx_product_total_rating", columnList = "total_rating")
})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Column(name = "order_count", nullable = false)
    private Long orderCount = 0L;

    @Column(name = "product_weight", nullable = false)
    private Double productWeight;

    @Column(name = "calories_per100grams", nullable = false)
    private Integer caloriesPer100Grams;

    @Transient
    private boolean isFavorite;

    @Transient
    private boolean isInCart;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

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

    @Transient
    private Integer quantityInCart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wishlist> wishlists;

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
