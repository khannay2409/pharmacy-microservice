package com.org.pharmacy.Entity;

import com.org.pharmacy.Enum.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "orders")
@Data @NoArgsConstructor
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // PLACED, PAID, CANCELLED
    private BigDecimal totalAmount;
    @CreationTimestamp
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
}
