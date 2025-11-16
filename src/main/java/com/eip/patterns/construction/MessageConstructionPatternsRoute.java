// ===================================================================
// 3. MESSAGE CONSTRUCTION PATTERNS
// ===================================================================
package com.eip.patterns.construction;

import com.eip.model.Order;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class MessageConstructionPatternsRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // COMMAND MESSAGE - Instruction to perform action
        from("direct:commandMessage")
                .routeId("commandMessage")
                .setHeader("CommandType", constant("CREATE_ORDER"))
                .setHeader("Timestamp", constant(LocalDateTime.now()))
                .log("Command Message: ${header.CommandType} - ${body}")
                .to("direct:executeCommand");

        // DOCUMENT MESSAGE - Data transfer
        from("direct:documentMessage")
                .routeId("documentMessage")
                .log("Document Message: Transferring order data")
                .marshal().json()
                .to("direct:documentStore");

        // EVENT MESSAGE - Notification of occurrence
        from("direct:eventMessage")
                .routeId("eventMessage")
                .setHeader("EventType", constant("ORDER_CREATED"))
                .setHeader("EventTime", constant(LocalDateTime.now()))
                .log("Event Message: ${header.EventType} at ${header.EventTime}")
                .to("direct:eventProcessor");

        // REQUEST-REPLY - Synchronous communication
        from("direct:requestReply")
                .routeId("requestReply")
                .log("Request-Reply: Processing request")
                .process(exchange -> {
                    Order order = exchange.getIn().getBody(Order.class);
                    order.setStatus("PROCESSED");
                    exchange.getIn().setBody(order);
                })
                .log("Request-Reply: Sending response");

        // RETURN ADDRESS - Reply destination
        from("direct:returnAddress")
                .routeId("returnAddress")
                .setHeader("ReplyTo", constant("direct:replyDestination"))
                .log("Return Address: Set to ${header.ReplyTo}")
                .to("direct:asyncProcessor");

        // CORRELATION IDENTIFIER - Match requests with replies
        from("direct:correlationIdentifier")
                .routeId("correlationIdentifier")
                .setHeader("CorrelationId", method(UUID.class, "randomUUID"))
                .log("Correlation ID: ${header.CorrelationId}")
                .to("direct:correlatedProcessor");

        // MESSAGE SEQUENCE - Ordered message series
        from("direct:messageSequence")
                .routeId("messageSequence")
                .split(body())
                .setHeader("SequenceNumber", simple("${property.CamelSplitIndex}"))
                .setHeader("SequenceSize", simple("${property.CamelSplitSize}"))
                .log("Message Sequence: ${header.SequenceNumber} of ${header.SequenceSize}")
                .to("direct:sequenceProcessor")
                .end();

        // MESSAGE EXPIRATION - Time-to-live
        from("direct:messageExpiration")
                .routeId("messageExpiration")
                .setHeader("ExpiresAt", constant(LocalDateTime.now().plusMinutes(5)))
                .log("Message Expiration: Expires at ${header.ExpiresAt}")
                .to("direct:timeoutProcessor");

        // FORMAT INDICATOR - Message format metadata
        from("direct:formatIndicator")
                .routeId("formatIndicator")
                .setHeader("MessageFormat", constant("JSON"))
                .setHeader("Version", constant("1.0"))
                .log("Format Indicator: ${header.MessageFormat} v${header.Version}")
                .to("direct:formatProcessor");
    }
}
