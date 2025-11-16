// ===================================================================
// REST Controller for Testing
// ===================================================================
package com.eip.controller;

import com.eip.model.Order;
import lombok.RequiredArgsConstructor;
import org.apache.camel.ProducerTemplate;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/eip")
@RequiredArgsConstructor
public class EipController {

    private final ProducerTemplate producerTemplate;

    @PostMapping("/root/message-channel")
    public String testMessageChannel(@RequestBody Order order) {
        producerTemplate.sendBody("direct:messageChannel", order);
        return "Message sent through channel";
    }

    @PostMapping("/root/pipes-filters")
    public String testPipesAndFilters(@RequestBody Order order) {
        producerTemplate.sendBody("direct:pipesAndFilters", order);
        return "Message processed through pipes and filters";
    }

    @PostMapping("/root/router")
    public String testRouter(@RequestBody Order order) {
        producerTemplate.sendBody("direct:messageRouter", order);
        return "Message routed based on content";
    }

    @PostMapping("/channel/publish-subscribe")
    public String testPublishSubscribe(@RequestBody Order order) {
        producerTemplate.sendBody("direct:publishSubscribeChannel", order);
        return "Message broadcast to subscribers";
    }

    @PostMapping("/routing/content-based-router")
    public String testContentBasedRouter(@RequestBody Order order) {
        producerTemplate.sendBody("direct:contentBasedRouter", order);
        return "Message routed based on content";
    }

    @PostMapping("/routing/splitter")
    public String testSplitter(@RequestBody Order order) {
        producerTemplate.sendBody("direct:splitter", order);
        return "Message split into parts";
    }

    @PostMapping("/transformation/content-enricher")
    public String testContentEnricher(@RequestBody Order order) {
        producerTemplate.sendBody("direct:contentEnricher", order);
        return "Message enriched with additional data";
    }

    @PostMapping("/endpoint/idempotent")
    public String testIdempotentReceiver(@RequestBody Order order) {
        producerTemplate.sendBody("direct:idempotentReceiver", order);
        return "Duplicate detection applied";
    }

    @PostMapping("/management/wire-tap")
    public String testWireTap(@RequestBody Order order) {
        producerTemplate.sendBody("direct:wireTap", order);
        return "Message monitored via wire tap";
    }

    @GetMapping("/health")
    public String health() {
        return "Enterprise Integration Patterns Application is running!";
    }
}

