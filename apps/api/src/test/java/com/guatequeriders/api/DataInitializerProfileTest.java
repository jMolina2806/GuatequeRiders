package com.guatequeriders.api;

import com.guatequeriders.api.config.DataInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:productionprofile;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "app.environment=production",
        "app.allowed-origins=https://app.example.com",
        "jwt.secret=test-only-production-profile-secret-2026"
})
@ActiveProfiles("production")
class DataInitializerProfileTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void dataInitializerIsNotRegisteredInProduction() {
        assertThat(applicationContext.getBeansOfType(DataInitializer.class)).isEmpty();
    }
}
