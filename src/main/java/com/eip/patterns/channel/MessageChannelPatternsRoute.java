// ===================================================================
// 2. MESSAGE CHANNEL PATTERNS
// ===================================================================
package com.eip.patterns.channel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MessageChannelPatternsRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // POINT-TO-POINT CHANNEL - One sender, one receiver
        from("direct:pointToPointChannel")
                .routeId("pointToPointChannel")
                .log("Point-to-Point: Sending to single consumer")
                .to("seda:orderQueue?waitForTaskToComplete=Never");

        from("seda:orderQueue")
                .routeId("orderQueueConsumer")
                .log("Point-to-Point Consumer: Processing ${body}");

        // PUBLISH-SUBSCRIBE CHANNEL - One sender, multiple receivers
        from("direct:publishSubscribeChannel")
                .routeId("publishSubscribeChannel")
                .log("Publish-Subscribe: Broadcasting message")
                .multicast()
                .to("direct:subscriber1", "direct:subscriber2", "direct:subscriber3");

        from("direct:subscriber1")
                .routeId("subscriber1")
                .log("Subscriber 1: Received ${body}");

        from("direct:subscriber2")
                .routeId("subscriber2")
                .log("Subscriber 2: Received ${body}");

        from("direct:subscriber3")
                .routeId("subscriber3")
                .log("Subscriber 3: Received ${body}");

        // DEAD LETTER CHANNEL - Error handling
        errorHandler(deadLetterChannel("direct:deadLetterQueue")
                .maximumRedeliveries(3)
                .redeliveryDelay(1000));

        from("direct:deadLetterQueue")
                .routeId("deadLetterQueue")
                .log("Dead Letter Queue: Failed message ${body}")
                .to("mock:errorStore");

        // INVALID MESSAGE CHANNEL
        from("direct:invalidMessageChannel")
                .routeId("invalidMessageChannel")
                .choice()
                .when(simple("${body.orderId} == null"))
                .log("Invalid Message: Missing orderId")
                .to("direct:invalidMessages")
                .otherwise()
                .to("direct:validMessages")
                .end();

        // GUARANTEED DELIVERY - Persistence
        from("direct:guaranteedDelivery")
                .routeId("guaranteedDelivery")
                .log("Guaranteed Delivery: Persisting message")
                .to("file:data/backup?fileName=${header.orderId}.json");

        // CHANNEL ADAPTER - External system integration
        from("direct:channelAdapter")
                .routeId("channelAdapter")
                .log("Channel Adapter: Adapting to external format")
                .marshal().json()
                .to("direct:externalSystem");

        // MESSAGING BRIDGE - Connect different messaging systems
        from("direct:messagingBridge")
                .routeId("messagingBridge")
                .log("Messaging Bridge: Bridging systems")
                .to("direct:systemA")
                .to("direct:systemB");
    }
}
