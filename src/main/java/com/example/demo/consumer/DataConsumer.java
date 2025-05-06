package com.example.demo.consumer;

import com.example.demo.model.DummyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DataConsumer {

    private static final Logger log = LoggerFactory.getLogger(DataConsumer.class);
    private static final Pattern CONSUMER_PATTERN = Pattern.compile(".*?-(\\d+)$");

    @Value("${redis.stream.key}")
    private String streamKey;

    @Value("${redis.stream.group}")
    private String groupName;

    @Value("${redis.stream.consumer.prefix:consumer-}")
    private String consumerPrefix;

    public void handleMessage(MapRecord<String, String, String> message) {
        String threadName = Thread.currentThread().getName();
        String consumerId = extractConsumerId(threadName);
        String consumerName = consumerId != null ? consumerPrefix + consumerId : threadName;
        
        log.info("Consumer: {} - Received message ID: {}", 
                consumerName, 
                message.getId());
        log.info("Stream: {}", message.getStream());
        
        Map<String, String> valueMap = message.getValue();
        DummyData data = new DummyData();
        data.setId(valueMap.get("id"));
        data.setMessage(valueMap.get("message"));
        String timestampStr = valueMap.get("timestamp");
        if (timestampStr != null) {
            try {
                data.setTimestamp(Instant.parse(timestampStr));
            } catch (Exception e) {
                log.error("Error parsing timestamp: {}", timestampStr, e);
            }
        }
        
        // Simulate some processing time to show concurrent processing benefits
        try {
            int processingTime = ThreadLocalRandom.current().nextInt(100, 1000);
            Thread.sleep(processingTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Consumer: {} - Processed Message: {}", 
                consumerName, 
                data);

        log.debug("Message {} processed by consumer {}, acknowledgment handled by container.", 
                message.getId(), consumerName);
    }
    
    /**
     * Extracts the consumer ID from the thread name.
     * Thread names follow the pattern 'redis-stream-listener-X' where X is the thread number.
     * We use this to identify which consumer instance is processing the message.
     */
    private String extractConsumerId(String threadName) {
        Matcher matcher = CONSUMER_PATTERN.matcher(threadName);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
} 