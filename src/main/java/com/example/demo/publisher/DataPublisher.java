package com.example.demo.publisher;

import com.example.demo.model.DummyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.connection.stream.RecordId;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@EnableScheduling
public class DataPublisher {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${redis.stream.key}")
    private String streamKey;

    private int counter = 0;

    @Scheduled(fixedRate = 5000)
    public void publishData() {
        counter++;
        DummyData data = new DummyData(UUID.randomUUID().toString(), "Message " + counter);

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("id", data.getId());
        dataMap.put("message", data.getMessage());
        dataMap.put("timestamp", data.getTimestamp().toString());

        MapRecord<String, String, String> record = StreamRecords.mapBacked(dataMap)
                .withStreamKey(streamKey);

        try {
            RecordId recordId = stringRedisTemplate.opsForStream().add(record);
            if (recordId != null) {
                 System.out.println("Published data with ID: " + recordId.getValue());
            } else {
                 System.err.println("Failed to publish data, received null recordId.");
            }
        } catch (Exception e) {
            System.err.println("Error publishing data: " + e.getMessage());
        }
    }
} 