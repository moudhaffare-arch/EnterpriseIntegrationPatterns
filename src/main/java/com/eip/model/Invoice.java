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
public class Invoice {
    private String invoiceId;
    private String orderId;
    private String customerId;
    private double amount;
    private LocalDateTime invoiceDate;
    private String status;
}
