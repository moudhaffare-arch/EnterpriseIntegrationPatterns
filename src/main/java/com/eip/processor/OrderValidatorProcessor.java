// ===================================================================
// Custom Processors
// ===================================================================
package com.eip.processor;

import com.eip.model.Order;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("orderValidator")
public class OrderValidatorProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Order order = exchange.getIn().getBody(Order.class);

        log.info("Validating order: {}", order.getOrderId());

        if (order.getQuantity() <= 0) {
            throw new IllegalArgumentException("Invalid quantity");
        }

        if (order.getTotalPrice() <= 0) {
            throw new IllegalArgumentException("Invalid price");
        }

        order.setStatus("VALIDATED");
        exchange.getIn().setBody(order);
    }
}
