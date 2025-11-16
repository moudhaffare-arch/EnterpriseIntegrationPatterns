// ===================================================================
// 1. ROOT MESSAGING PATTERNS
// ===================================================================
package com.eip.patterns.root;

import com.eip.model.Order;
import com.eip.model.Invoice;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * Root Messaging Patterns:
 * 1. Message Channel - Channels for communication
 * 2. Message - Data structure for communication
 * 3. Pipes and Filters - Processing pipeline
 * 4. Message Router - Route based on content
 * 5. Message Translator - Transform message format
 * 6. Message Endpoint - Connection point
 */

@Component
public class RootMessagingPatternsRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // 1. MESSAGE CHANNEL - Point-to-point communication
        from("direct:messageChannel")
                .routeId("messageChannel")
                .log("Message Channel: Received ${body}")
                .to("direct:processOrder");

        // 2. MESSAGE - Structured data container
        from("direct:messageDemo")
                .routeId("messageDemo")
                .log("Message: OrderId=${body.orderId}, CustomerId=${body.customerId}")
                .to("direct:nextStep");

        // 3. PIPES AND FILTERS - Sequential processing
        from("direct:pipesAndFilters")
                .routeId("pipesAndFilters")
                .log("Pipes and Filters: Starting pipeline")
                .filter(simple("${body.quantity} > 0"))
                .log("Filter 1: Quantity validation passed")
                .process(exchange -> {
                    Order order = exchange.getIn().getBody(Order.class);
                    order.setStatus("VALIDATED");
                })
                .log("Filter 2: Status updated")
                .to("direct:enrichOrder");

        // 4. MESSAGE ROUTER - Content-based routing
        from("direct:messageRouter")
                .routeId("messageRouter")
                .log("Message Router: Routing based on priority")
                .choice()
                .when(simple("${body.priority} == 'HIGH'"))
                .log("Routing to high priority queue")
                .to("direct:highPriority")
                .when(simple("${body.priority} == 'MEDIUM'"))
                .log("Routing to medium priority queue")
                .to("direct:mediumPriority")
                .otherwise()
                .log("Routing to standard queue")
                .to("direct:standardPriority")
                .end();

        // 5. MESSAGE TRANSLATOR - Format transformation
        from("direct:messageTranslator")
                .routeId("messageTranslator")
                .log("Message Translator: Transforming order to invoice")
                .process(exchange -> {
                    Order order = exchange.getIn().getBody(Order.class);
                    Invoice invoice = Invoice.builder()
                            .invoiceId("INV-" + order.getOrderId())
                            .orderId(order.getOrderId())
                            .customerId(order.getCustomerId())
                            .amount(order.getTotalPrice())
                            .invoiceDate(LocalDateTime.now())
                            .status("GENERATED")
                            .build();
                    exchange.getIn().setBody(invoice);
                })
                .log("Translation complete: ${body}")
                .to("direct:invoiceProcessor");

        // 6. MESSAGE ENDPOINT - Entry/exit points
        from("direct:messageEndpoint")
                .routeId("messageEndpoint")
                .log("Message Endpoint: Entry point for external systems")
                .to("direct:internalProcessing");
    }
}
