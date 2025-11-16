package com.eip.processor;

import com.eip.model.Order;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("orderEnricher")
public class OrderEnricherProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Order order = exchange.getIn().getBody(Order.class);

        log.info("Enriching order: {}", order.getOrderId());

        // Simulate enrichment with customer data
        order.setStatus("ENRICHED");
        exchange.getIn().setHeader("CustomerTier", "GOLD");
        exchange.getIn().setHeader("DiscountApplied", true);

        exchange.getIn().setBody(order);
    }
}