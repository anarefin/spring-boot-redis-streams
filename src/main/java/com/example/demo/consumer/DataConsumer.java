package com.example.demo.consumer;

import com.example.demo.model.DummyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class DataConsumer {

    private static final Logger log = LoggerFactory.getLogger(DataConsumer.class);

    @Value("${redis.stream.key}")
    private String streamKey;

    @Value("${redis.stream.group}")
    private String groupName;

    public void handleMessage(MapRecord<String, String, String> message) {
        log.info("Received message ID: {}", message.getId());
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
        
        log.info("Consumed Message: {}", data);

        log.debug("Message {} processed, acknowledgment handled by container.", message.getId());
    }
} 