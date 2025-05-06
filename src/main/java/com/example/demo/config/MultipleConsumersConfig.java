package com.example.demo.config;

import com.example.demo.consumer.DataConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MultipleConsumersConfig {

    private static final Logger log = LoggerFactory.getLogger(MultipleConsumersConfig.class);

    @Value("${redis.stream.key}")
    private String streamKey;

    @Value("${redis.stream.group}")
    private String groupName;

    @Value("${redis.stream.consumer.count:3}")
    private int consumerCount;

    @Value("${redis.stream.consumer.prefix:consumer-}")
    private String consumerPrefix;

    @Bean
    public List<Subscription> multipleConsumersSubscription(
            RedisConnectionFactory factory,
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer,
            DataConsumer dataConsumer,
            StringRedisTemplate redisTemplate) {

        // Ensure the consumer group exists
        try {
            redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("$"), groupName);
            log.info("Consumer group '{}' created for stream '{}'", groupName, streamKey);
        } catch (RedisSystemException e) {
            if (e.getRootCause() != null && e.getRootCause().getMessage() != null && e.getRootCause().getMessage().contains("BUSYGROUP")) {
                log.warn("Consumer group '{}' already exists for stream '{}'", groupName, streamKey);
            } else {
                log.error("Error creating consumer group '{}' for stream '{}': {}", groupName, streamKey, e.getMessage());
            }
        } catch (Exception e) {
            log.error("Unexpected error creating consumer group '{}' for stream '{}': {}", groupName, streamKey, e.getMessage(), e);
        }

        // Create multiple consumers
        List<Subscription> subscriptions = new ArrayList<>();
        for (int i = 1; i <= consumerCount; i++) {
            String consumerName = consumerPrefix + i;
            
            Subscription subscription = listenerContainer.receive(
                    Consumer.from(groupName, consumerName),
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                    dataConsumer::handleMessage);

            log.info("Subscription created for consumer '{}' on group '{}', stream '{}'", consumerName, groupName, streamKey);
            subscriptions.add(subscription);
        }

        listenerContainer.start();
        log.info("StreamMessageListenerContainer started with {} consumers", consumerCount);

        return subscriptions;
    }
} 