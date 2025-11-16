package com.eip.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;
    private String customerId;
    private String productId;
    private int quantity;
    private double totalPrice;
    private String status;
    private LocalDateTime timestamp;
    private String priority;
}
