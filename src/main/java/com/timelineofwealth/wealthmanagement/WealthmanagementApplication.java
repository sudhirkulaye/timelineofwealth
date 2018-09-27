package com.timelineofwealth.wealthmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.timelineofwealth"}) //for separating controllers and entities
@ComponentScan({"com.timelineofwealth"})
@EntityScan("com.timelineofwealth.entities")
@EnableJpaRepositories("com.timelineofwealth.repositories")
public class WealthmanagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(WealthmanagementApplication.class, args);
    }
}
