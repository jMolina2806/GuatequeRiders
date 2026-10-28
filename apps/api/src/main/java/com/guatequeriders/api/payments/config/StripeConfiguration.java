package com.guatequeriders.api.payments.config;

import com.stripe.StripeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StripeProperties.class)
public class StripeConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "stripe", name = "enabled", havingValue = "true")
    public StripeClient stripeClient(StripeProperties properties,
                                     @Value("${app.environment:production}") String applicationEnvironment) {
        properties.validate(applicationEnvironment);
        return new StripeClient(properties.getSecretKey());
    }
}
