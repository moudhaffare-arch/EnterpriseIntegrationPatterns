// ===================================================================
// Monitoring and Metrics
// ===================================================================
package com.eip.monitoring;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MonitoringRoutes extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Route for collecting metrics
        from("timer:metrics?period=30000")
                .routeId("metricsCollector")
                .log("Collecting metrics")
                .process(exchange -> {
                    // Collect route statistics
                    getContext().getRoutes().forEach(route -> {
                        String routeId = route.getId();
                        log.info("Route: {} - Status: {}",
                                routeId, route.getRouteContext().getStatus());
                    });
                });

        // Health check endpoint
        from("direct:healthCheck")
                .routeId("healthCheck")
                .setBody(constant("{\n" +
                        "  \"status\": \"UP\",\n" +
                        "  \"routes\": \"active\",\n" +
                        "  \"integrationPatterns\": \"operational\"\n" +
                        "}"))
                .setHeader("Content-Type", constant("application/json"));
    }
}