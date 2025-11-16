// ===================================================================
// Service Classes
// ===================================================================
package com.eip.service;

import com.eip.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("orderService")
public class OrderService {

    public Order processOrder(Order order) {
        log.info("Processing order: {}", order.getOrderId());
        order.setStatus("PROCESSED");
        return order;
    }

    public Order validateOrder(Order order) {
        log.info("Validating order: {}", order.getOrderId());
        order.setStatus("VALIDATED");
        return order;
    }
}