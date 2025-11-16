// ===================================================================
// Application Configuration
// ===================================================================
package com.eip.config;

import org.apache.camel.CamelContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Configuration
public class CamelConfiguration {

    @Bean
    public org.apache.camel.component.properties.PropertiesComponent properties() {
        org.apache.camel.component.properties.PropertiesComponent pc =
                new org.apache.camel.component.properties.PropertiesComponent();
        pc.setLocation("classpath:application.properties");
        return pc;
    }
}