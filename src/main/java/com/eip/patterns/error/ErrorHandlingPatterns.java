// ===================================================================
// Error Handling Patterns
// ===================================================================
package com.eip.patterns.error;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ErrorHandlingPatterns extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Global Error Handler
        errorHandler(deadLetterChannel("direct:globalErrorHandler")
                .maximumRedeliveries(3)
                .redeliveryDelay(2000)
                .retryAttemptedLogLevel(org.apache.camel.LoggingLevel.WARN)
                .onExceptionOccurred(exchange -> {
                    Exception cause = exchange.getProperty(
                            org.apache.camel.Exchange.EXCEPTION_CAUGHT, Exception.class);
                    exchange.getIn().setHeader("ErrorMessage", cause.getMessage());
                }));

        from("direct:globalErrorHandler")
                .routeId("globalErrorHandler")
                .log("Global Error Handler: ${header.ErrorMessage}")
                .to("file:data/errors");

        // Exception-specific handling
        from("direct:exceptionHandling")
                .routeId("exceptionHandling")
                .onException(IllegalArgumentException.class)
                .handled(true)
                .log("Handling IllegalArgumentException: ${exception.message}")
                .setBody(constant("Invalid argument error handled"))
                .to("direct:validationError")
                .end()
                .onException(NullPointerException.class)
                .handled(true)
                .log("Handling NullPointerException: ${exception.message}")
                .to("direct:nullPointerError")
                .end()
                .log("Processing with exception handling")
                .to("direct:riskyOperation");

        // RETRY Pattern with exponential backoff
        from("direct:retryWithBackoff")
                .routeId("retryWithBackoff")
                .onException(Exception.class)
                .maximumRedeliveries(5)
                .redeliveryDelay(1000)
                .backOffMultiplier(2)
                .maximumRedeliveryDelay(60000)
                .retryAttemptedLogLevel(org.apache.camel.LoggingLevel.WARN)
                .log("Retry attempt ${header.CamelRedeliveryCounter}")
                .end()
                .log("Attempting operation with retry")
                .to("direct:unreliableService");

        // CIRCUIT BREAKER Pattern
        from("direct:circuitBreaker")
                .routeId("circuitBreaker")
                .circuitBreaker()
                .resilience4jConfiguration()
                .failureRateThreshold(50)
                .waitDurationInOpenState(5000)
                .slidingWindowSize(10)
                .end()
                .log("Circuit Breaker: Attempting call")
                .to("direct:externalService")
                .onFallback()
                .log("Circuit Breaker: Using fallback")
                .setBody(constant("Fallback response"))
                .end();
    }
}
