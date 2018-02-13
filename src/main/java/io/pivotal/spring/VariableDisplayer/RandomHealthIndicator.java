package io.pivotal.spring.VariableDisplayer;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class RandomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up().withDetail("condition", "good").build();
    }

}
