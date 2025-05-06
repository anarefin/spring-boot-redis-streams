package com.example.demo.config;

import com.example.demo.consumer.DataConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;

@Configuration
public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${redis.stream.key}")
    private String streamKey;

    @Value("${redis.stream.group}")
    private String groupName;

    @Value("${redis.stream.consumer}")
    private String consumerName;
    
    @Value("${redis.stream.consumer.prefix:consumer-}")
    private String consumerPrefix;

    @Bean(name = "streamListenerExecutor")
    public TaskExecutor streamListenerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix(consumerPrefix);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> streamMessageListenerContainerOptions(
             TaskExecutor streamListenerExecutor
    ) {
        return StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofSeconds(1))
                .executor(streamListenerExecutor)
                .build();
    }

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options) {

        return StreamMessageListenerContainer.create(connectionFactory, options);
    }

    @Bean
    @ConditionalOnProperty(name = "redis.stream.consumer.count", havingValue = "0", matchIfMissing = false)
    public Subscription singleConsumerSubscription(RedisConnectionFactory factory,
                                   StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer,
                                   DataConsumer dataConsumer,
                                   StringRedisTemplate redisTemplate) {

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

        Subscription subscription = listenerContainer.receive(
                Consumer.from(groupName, consumerName),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                dataConsumer::handleMessage);

        log.info("Subscription created for consumer '{}' on group '{}', stream '{}'", consumerName, groupName, streamKey);
        
        listenerContainer.start();
        log.info("StreamMessageListenerContainer started.");

        return subscription;
    }
} 