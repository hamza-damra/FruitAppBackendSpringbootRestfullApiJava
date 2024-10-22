package com.hamza.fruitsappbackend.modulus.cart.entity;

import com.hamza.fruitsappbackend.constant.CartStatus;
import com.hamza.fruitsappbackend.modulus.order.entity.Order;
import com.hamza.fruitsappbackend.modulus.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_updated_at", columnList = "updated_at")
}, uniqueConstraints = {
        // Ensure only one active cart per user
        @UniqueConstraint(columnNames = {"user_id", "active_cart_flag"}, name = "unique_active_cart_per_user")
})
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CartStatus status;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column(name = "total_quantity")
    private Integer totalQuantity = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    // The generated column to track active cart status
    @Transient
    @Column(name = "active_cart_flag", insertable = false, updatable = false)
    private Integer activeCartFlag;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = CartStatus.ACTIVE;
        }
        setActiveCartFlag();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        setActiveCartFlag();
    }

    // Method to calculate active cart flag
    private void setActiveCartFlag() {
        this.activeCartFlag = (this.status == CartStatus.ACTIVE) ? 1 : null;
    }

    public void addItem(CartItem item) {
        if (this.status == CartStatus.COMPLETED) {
            throw new IllegalStateException("Cannot modify a completed cart.");
        }
        this.cartItems.add(item);
        updateTotal();
    }

    public void reopenCart() {
        if (this.status != CartStatus.COMPLETED) {
            throw new IllegalStateException("Only completed carts can be reopened.");
        }
        this.status = CartStatus.ACTIVE;
        clearCartItems();
    }

    public void completeCart() {
        if (this.status == CartStatus.COMPLETED) {
            throw new IllegalStateException("Cart is already completed.");
        }
        this.status = CartStatus.COMPLETED;
        clearCartItems();
    }

    public void clearCartItems() {
        this.cartItems.clear();
        updateTotal();
    }

    private void updateTotal() {
        this.totalPrice = this.cartItems.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalQuantity = this.cartItems.size();
    }
}
