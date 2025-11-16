// ===================================================================
// Integration Test Examples
// ===================================================================
package com.eip.test;

import com.eip.model.Order;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDateTime;

@SpringBootTest
@CamelSpringBootTest
public class EipIntegrationTest {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext;

    @Test
    public void testMessageChannel() {
        Order order = createTestOrder();
        producerTemplate.sendBody("direct:messageChannel", order);
        // Add assertions
    }

    @Test
    public void testContentBasedRouter() {
        Order highValueOrder = createTestOrder();
        highValueOrder.setTotalPrice(1500.0);
        producerTemplate.sendBody("direct:contentBasedRouter", highValueOrder);
        // Add assertions
    }

    @Test
    public void testPublishSubscribe() {
        Order order = createTestOrder();
        producerTemplate.sendBody("direct:publishSubscribeChannel", order);
        // Add assertions
    }

    @Test
    public void testIdempotentReceiver() {
        Order order = createTestOrder();
        // Send same message twice
        producerTemplate.sendBody("direct:idempotentReceiver", order);
        producerTemplate.sendBody("direct:idempotentReceiver", order);
        // Should process only once
    }

    private Order createTestOrder() {
        return Order.builder()
                .orderId("ORD-001")
                .customerId("CUST-001")
                .productId("PROD-001")
                .quantity(5)
                .totalPrice(250.0)
                .status("NEW")
                .priority("MEDIUM")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
