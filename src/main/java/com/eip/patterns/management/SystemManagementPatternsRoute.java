// ===================================================================
// 7. SYSTEM MANAGEMENT PATTERNS
// ===================================================================
package com.eip.patterns.management;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class SystemManagementPatternsRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // CONTROL BUS - Manage and monitor routes
        from("direct:controlBus")
                .routeId("controlBus")
                .log("Control Bus: Managing routes")
                .choice()
                .when(simple("${body} == 'START'"))
                .to("controlbus:route?routeId=targetRoute&action=start")
                .when(simple("${body} == 'STOP'"))
                .to("controlbus:route?routeId=targetRoute&action=stop")
                .when(simple("${body} == 'STATUS'"))
                .to("controlbus:route?routeId=targetRoute&action=status")
                .end();

        // DETOUR - Conditional routing for testing
        from("direct:detour")
                .routeId("detour")
                .choice()
                .when(simple("${header.TestMode} == true"))
                .log("Detour: Routing to test endpoint")
                .to("direct:testEndpoint")
                .otherwise()
                .log("Detour: Routing to production endpoint")
                .to("direct:productionEndpoint")
                .end();

        // WIRE TAP - Monitor message flow
        from("direct:wireTap")
                .routeId("wireTap")
                .wireTap("direct:monitoring")
                .log("Wire Tap: Processing message (monitored)")
                .to("direct:businessProcessor");

        from("direct:monitoring")
                .routeId("monitoring")
                .log("Wire Tap Monitor: Captured ${body}");

        // MESSAGE STORE - Archive messages
        from("direct:messageStore")
                .routeId("messageStore")
                .log("Message Store: Archiving message")
                .marshal().json()
                .to("file:data/archive?fileName=${header.orderId}-${date:now:yyyyMMdd-HHmmss}.json")
                .log("Message Store: Archived successfully");

        // SMART PROXY - Intelligent intermediary
        from("direct:smartProxy")
                .routeId("smartProxy")
                .log("Smart Proxy: Intercepting call")
                .choice()
                .when(simple("${body.cached} == true"))
                .log("Smart Proxy: Returning cached response")
                .to("direct:cache")
                .otherwise()
                .log("Smart Proxy: Forwarding to actual service")
                .to("direct:actualService")
                .to("direct:updateCache")
                .end();

        // TEST MESSAGE - Diagnostic messages
        from("direct:testMessage")
                .routeId("testMessage")
                .log("Test Message: Diagnostic flow")
                .setHeader("TestMode", constant(true))
                .setHeader("TestId", simple("TEST-${date:now:yyyyMMddHHmmss}"))
                .to("direct:systemDiagnostics");
    }
}