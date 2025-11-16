// ===================================================================
// Transaction Patterns
// ===================================================================
package com.eip.patterns.transaction;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class TransactionPatterns extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // TRANSACTIONAL CLIENT Pattern
        from("direct:transactionalClient")
                .routeId("transactionalClient")
                .transacted()
                .log("Transactional Client: Starting transaction")
                .to("direct:dbOperation1")
                .to("direct:dbOperation2")
                .log("Transactional Client: Transaction completed");

        // SAGA Pattern (Compensation-based)
        from("direct:saga")
                .routeId("saga")
                .log("Saga: Starting distributed transaction")
                .to("direct:bookFlight")
                .to("direct:bookHotel")
                .to("direct:bookCar")
                .choice()
                .when(simple("${body.failed} == true"))
                .log("Saga: Compensating transactions")
                .to("direct:cancelCar")
                .to("direct:cancelHotel")
                .to("direct:cancelFlight")
                .otherwise()
                .log("Saga: All bookings confirmed")
                .end();

        // OUTBOX Pattern for reliable publishing
        from("direct:outbox")
                .routeId("outbox")
                .log("Outbox: Storing event in outbox table")
                .to("sql:INSERT INTO outbox (event_type, payload) VALUES (:#eventType, :#payload)")
                .log("Outbox: Event stored for reliable delivery");

        from("scheduler:outboxProcessor?delay=5000")
                .routeId("outboxProcessor")
                .log("Outbox Processor: Processing pending events")
                .to("sql:SELECT * FROM outbox WHERE processed = false")
                .split(body())
                .to("direct:publishEvent")
                .to("sql:UPDATE outbox SET processed = true WHERE id = :#id")
                .end();
    }
}