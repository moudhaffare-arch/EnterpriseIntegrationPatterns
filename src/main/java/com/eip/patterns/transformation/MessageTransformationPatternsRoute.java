// ===================================================================
// 5. MESSAGE TRANSFORMATION PATTERNS
// ===================================================================
package com.eip.patterns.transformation;

import com.eip.model.Order;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MessageTransformationPatternsRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // ENVELOPE WRAPPER - Add routing information
        from("direct:envelopeWrapper")
                .routeId("envelopeWrapper")
                .setHeader("Sender", constant("OrderService"))
                .setHeader("Timestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss}"))
                .setHeader("Priority", constant("NORMAL"))
                .log("Envelope Wrapper: Added headers")
                .to("direct:wrappedProcessor");

        // CONTENT ENRICHER - Add additional data
        from("direct:contentEnricher")
                .routeId("contentEnricher")
                .log("Content Enricher: Enriching order with customer data")
                .enrich("direct:customerService", (original, resource) -> {
                    Order order = original.getIn().getBody(Order.class);
                    // Enrich with customer data
                    order.setStatus("ENRICHED");
                    return original;
                })
                .log("Content Enricher: Enrichment complete");

        // CONTENT FILTER - Remove unnecessary data
        from("direct:contentFilter")
                .routeId("contentFilter")
                .log("Content Filter: Filtering sensitive data")
                .process(exchange -> {
                    Order order = exchange.getIn().getBody(Order.class);
                    // Create filtered version
                    Order filtered = Order.builder()
                            .orderId(order.getOrderId())
                            .productId(order.getProductId())
                            .quantity(order.getQuantity())
                            .build();
                    exchange.getIn().setBody(filtered);
                })
                .log("Content Filter: Filtered ${body}");

        // CLAIM CHECK - Store and retrieve payload
        from("direct:claimCheck")
                .routeId("claimCheck")
                .log("Claim Check: Storing payload")
                .setProperty("claimCheckId", simple("${body.orderId}"))
                .setProperty("storedBody", body())
                .setBody(simple("${exchangeProperty.claimCheckId}"))
                .log("Claim Check: Stored with ID ${body}")
                .to("direct:lightweightProcessor");

        from("direct:claimCheckRetrieve")
                .routeId("claimCheckRetrieve")
                .log("Claim Check: Retrieving payload for ${body}")
                .setBody(exchangeProperty("storedBody"))
                .log("Claim Check: Retrieved ${body}");

        // NORMALIZER - Convert to common format
        from("direct:normalizer")
                .routeId("normalizer")
                .log("Normalizer: Converting to canonical format")
                .choice()
                .when(header("SourceSystem").isEqualTo("SystemA"))
                .to("direct:convertFromSystemA")
                .when(header("SourceSystem").isEqualTo("SystemB"))
                .to("direct:convertFromSystemB")
                .otherwise()
                .log("Already in canonical format")
                .end()
                .to("direct:canonicalProcessor");

        // CANONICAL DATA MODEL - Standard format
        from("direct:canonicalDataModel")
                .routeId("canonicalDataModel")
                .log("Canonical Data Model: Using standard format")
                .marshal().json()
                .to("direct:standardProcessor");
    }
}
