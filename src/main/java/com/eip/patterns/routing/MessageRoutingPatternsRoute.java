package com.eip.patterns.routing;

import com.eip.model.Order;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.ExchangeProperties; // Correct import for ExchangeProperties
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MessageRoutingPatternsRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // CONTENT-BASED ROUTER - Route based on message content
        from("direct:contentBasedRouter")
                .routeId("contentBasedRouter")
                .log("Content-Based Router: Analyzing content")
                .choice()
                .when(simple("${body.totalPrice} > 1000"))
                .log("High-value order: ${body.orderId}")
                .to("direct:highValueOrders")
                .when(simple("${body.totalPrice} > 500"))
                .log("Medium-value order: ${body.orderId}")
                .to("direct:mediumValueOrders")
                .otherwise()
                .log("Standard order: ${body.orderId}")
                .to("direct:standardOrders")
                .end();

        // MESSAGE FILTER - Filter unwanted messages
        from("direct:messageFilter")
                .routeId("messageFilter")
                .filter(simple("${body.status} == 'APPROVED'"))
                .log("Message Filter: Approved order ${body.orderId}")
                .to("direct:approvedOrders")
                .end();

        // DYNAMIC ROUTER - Runtime routing decisions
        from("direct:dynamicRouter")
                .routeId("dynamicRouter")
                .dynamicRouter(method(this, "computeRoute"));

        // RECIPIENT LIST - Multiple destinations
        from("direct:recipientList")
                .routeId("recipientList")
                .recipientList(simple("direct:warehouse,direct:accounting,direct:shipping"))
                .log("Recipient List: Sent to multiple destinations");

        // SPLITTER - Break message into parts
        from("direct:splitter")
                .routeId("splitter")
                .log("Splitter: Splitting order items")
                .split(simple("${body.items}"))
                .log("Split item: ${body}")
                .to("direct:processItem")
                .end();

        // AGGREGATOR - Combine related messages
        from("direct:aggregator")
                .routeId("aggregator")
                .aggregate(header("orderId"), new GroupedBodyAggregationStrategy())
                .completionSize(3)
                .completionTimeout(5000)
                .log("Aggregator: Combined ${body.size()} messages")
                .to("direct:aggregatedProcessor");

        // RESEQUENCER - Order messages
        from("direct:resequencer")
                .routeId("resequencer")
                .resequence(header("SequenceNumber"))
                .log("Resequencer: Ordered message ${header.SequenceNumber}")
                .to("direct:orderedProcessor");

        // COMPOSED MESSAGE PROCESSOR - Process and recombine
        from("direct:composedMessageProcessor")
                .routeId("composedMessageProcessor")
                .multicast()
                .to("direct:enrichWithCustomer", "direct:enrichWithInventory")
                .end()
                .log("Composed Message Processor: Enriched message");

        // SCATTER-GATHER - Broadcast and aggregate responses
        from("direct:scatterGather")
                .routeId("scatterGather")
                .multicast(new GroupedBodyAggregationStrategy())
                .parallelProcessing()
                .to("direct:priceService", "direct:inventoryService", "direct:shippingService")
                .end()
                .log("Scatter-Gather: Collected responses");

        // MESSAGE BROKER - Central routing hub
        from("direct:messageBroker")
                .routeId("messageBroker")
                .log("Message Broker: Central routing")
                .to("direct:routingLogic");
    }

    // 1. Replaced @org.apache.camel.Properties with @org.apache.camel.language.ExchangeProperties
    public String computeRoute(Order order, @ExchangeProperties Map<String, Object> properties) {
        if ("HIGH".equals(order.getPriority())) {
            return "direct:expressProcessing";
        }
        return null; // Stop routing
    }
}

