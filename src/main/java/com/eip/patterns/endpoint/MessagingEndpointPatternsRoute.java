// ===================================================================
// 6. MESSAGING ENDPOINT PATTERNS
// ===================================================================
package com.eip.patterns.endpoint;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MessagingEndpointPatternsRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // POLLING CONSUMER - Pull messages at intervals
        from("timer:pollingConsumer?period=10000")
                .routeId("pollingConsumer")
                .log("Polling Consumer: Checking for new orders")
                .to("direct:checkOrderQueue");

        // EVENT-DRIVEN CONSUMER - React to messages
        from("direct:eventDrivenConsumer")
                .routeId("eventDrivenConsumer")
                .log("Event-Driven Consumer: Processing incoming message")
                .to("direct:immediateProcessor");

        // COMPETING CONSUMERS - Multiple consumers, one processes
        from("seda:competingConsumers?concurrentConsumers=3")
                .routeId("competingConsumers")
                .log("Competing Consumer [${threadName}]: Processing ${body}")
                .to("direct:sharedProcessor");

        // MESSAGE DISPATCHER - Distribute to performers
        from("direct:messageDispatcher")
                .routeId("messageDispatcher")
                .log("Message Dispatcher: Distributing work")
                .multicast()
                .to("direct:worker1", "direct:worker2", "direct:worker3");

        // SELECTIVE CONSUMER - Filter by criteria
        from("direct:selectiveConsumer")
                .routeId("selectiveConsumer")
                .filter(simple("${body.priority} == 'HIGH'"))
                .log("Selective Consumer: High priority message accepted")
                .to("direct:priorityProcessor")
                .end();

        // DURABLE SUBSCRIBER - Persistent subscription
        from("direct:durableSubscriber")
                .routeId("durableSubscriber")
                .log("Durable Subscriber: Persisting subscription")
                .to("file:data/subscriptions");

        // IDEMPOTENT RECEIVER - Prevent duplicate processing
        from("direct:idempotentReceiver")
                .routeId("idempotentReceiver")
                .idempotentConsumer(simple("${body.orderId}"))
                .messageIdRepository(new org.apache.camel.processor.idempotent.MemoryIdempotentRepository())
                .log("Idempotent Receiver: Processing unique message ${body.orderId}")
                .to("direct:uniqueProcessor")
                .end();

        // SERVICE ACTIVATOR - Invoke service methods
        from("direct:serviceActivator")
                .routeId("serviceActivator")
                .log("Service Activator: Invoking order service")
                .bean("orderService", "processOrder")
                .log("Service Activator: Service invoked");
    }
}