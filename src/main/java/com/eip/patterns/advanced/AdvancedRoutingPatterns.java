// ===================================================================
// Advanced Routing Patterns
// ===================================================================
package com.eip.patterns.advanced;

import com.eip.model.Order;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.springframework.stereotype.Component;

@Component
public class AdvancedRoutingPatterns extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // LOAD BALANCER Pattern
        from("direct:loadBalancer")
                .routeId("loadBalancer")
                .log("Load Balancer: Distributing load")
                .loadBalance().roundRobin()
                .to("direct:server1")
                .to("direct:server2")
                .to("direct:server3")
                .end();

        from("direct:server1")
                .log("Server 1: Processing ${body}");

        from("direct:server2")
                .log("Server 2: Processing ${body}");

        from("direct:server3")
                .log("Server 3: Processing ${body}");

        // RECIPIENT LIST with Dynamic Destinations
        from("direct:dynamicRecipientList")
                .routeId("dynamicRecipientList")
                .log("Dynamic Recipient List: Computing recipients")
                .process(exchange -> {
                    Order order = exchange.getIn().getBody(Order.class);
                    String recipients = "direct:warehouse";
                    if (order.getTotalPrice() > 500) {
                        recipients += ",direct:premiumShipping";
                    }
                    if ("HIGH".equals(order.getPriority())) {
                        recipients += ",direct:managerNotification";
                    }
                    exchange.getIn().setHeader("recipients", recipients);
                })
                .recipientList(header("recipients"))
                .log("Sent to dynamic recipients");

        // ROUTING SLIP Pattern
        from("direct:routingSlip")
                .routeId("routingSlip")
                .setHeader("routingSlip", constant("direct:step1,direct:step2,direct:step3"))
                .routingSlip(header("routingSlip"))
                .log("Routing Slip: Completed all steps");

        from("direct:step1")
                .log("Step 1: Validation")
                .to("direct:validate");

        from("direct:step2")
                .log("Step 2: Enrichment")
                .to("direct:enrich");

        from("direct:step3")
                .log("Step 3: Finalization")
                .to("direct:finalize");

        // THROTTLER Pattern - Rate limiting
        from("direct:throttler")
                .routeId("throttler")
                .throttle(10).timePeriodMillis(1000)
                .log("Throttler: Processing at controlled rate")
                .to("direct:rateLimitedService");

        // DELAYER Pattern - Introduce delay
        from("direct:delayer")
                .routeId("delayer")
                .delay(1000)
                .log("Delayer: Processing after delay")
                .to("direct:delayedProcessor");

        // SAMPLING Pattern - Process subset of messages
        from("direct:sampling")
                .routeId("sampling")
                .sample(10) // Every 10th message
                .log("Sampling: Processing sampled message")
                .to("direct:analyticsProcessor");

        // MULTICAST with aggregation
        from("direct:multicastAggregate")
                .routeId("multicastAggregate")
                .multicast(new GroupedBodyAggregationStrategy())
                .parallelProcessing()
                .timeout(5000)
                .to("direct:service1", "direct:service2", "direct:service3")
                .end()
                .log("Multicast Aggregate: Combined results from ${body.size()} services");
    }
}
