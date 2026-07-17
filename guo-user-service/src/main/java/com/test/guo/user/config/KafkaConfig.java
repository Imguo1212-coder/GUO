package com.test.guo.user.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String USER_CREATED_TOPIC = "user.created";

    @Bean
    public NewTopic userCreatedTopic() {
        return TopicBuilder.name(USER_CREATED_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
