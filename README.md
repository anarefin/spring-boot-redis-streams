# Redis Streams Demo with Spring Boot

This project demonstrates the implementation of Redis Streams within a Java Spring Boot application. It showcases a scheduled publisher automatically sending messages and a consumer processing messages from a Redis Stream using a consumer group.

## Features

*   **Scheduled Publisher:** Automatically publishes a message containing a UUID, counter, and timestamp to the stream `my-stream` every 5 seconds. (See `DataPublisher.java`)
*   **Stream Consumer:** Reads and logs messages from `my-stream` using the consumer group `my-group` and consumer name `consumer-1`. Acknowledgment is handled automatically by the container. (See `DataConsumer.java` and `RedisConfig.java`)
*   **Consumer Group Management:** Automatically attempts to create the `my-group` consumer group on startup if it doesn't exist.
*   **Spring Boot Integration:** Leverages Spring Data Redis (`StringRedisTemplate`, `StreamMessageListenerContainer`) for seamless integration with Redis.
*   **Gradle Build:** Uses Gradle for dependency management and building the project.
*   **Docker Support:** Includes `docker-compose.yml` to easily start a Redis instance and a `Dockerfile` to containerize the application.

## Prerequisites

*   Java Development Kit (JDK) 21 or later (as per `Dockerfile`)
*   Gradle 8.x or later (or use the included Gradle wrapper `./gradlew`)
*   Docker and Docker Compose (for running Redis easily)

## Getting Started

### 1. Start Redis

The easiest way to run Redis is using the provided Docker Compose file:

```bash
docker-compose up -d redis
```

This command will start a Redis container in the background, listening on port `6379`.

### 2. Run the Application

You can run the Spring Boot application directly using Gradle:

1.  **Clone the repository:**
    ```bash
    git clone <your-repository-url>
    cd redis-streams-demo
    ```
2.  **Run the application:**
    ```bash
    ./gradlew bootRun
    ```

The application will start, connect to the Redis instance (ensure it's running at `localhost:6379` as per `application.properties`), create the consumer group `my-group` on the stream `my-stream` if needed, and begin publishing/consuming messages. Check the application logs to see the published message IDs and the consumed message details.

### Alternative: Running the Application in Docker

1.  **Start Redis** (if not already running):
    ```bash
    docker-compose up -d redis
    ```
2.  **Build the application JAR:**
    ```bash
    ./gradlew bootJar
    ```
3.  **Build the Docker image:**
    ```bash
    docker build -t redis-streams-demo-app .
    ```
4.  **Run the application container:**
    ```bash
    # Make sure the app container can reach the redis container
    # This command connects it to the same network created by docker-compose
    docker run --rm --name redis-streams-app --network redis-streams-demo_redis-net -p 8080:8080 redis-streams-demo-app
    ```
    *Note: You might need to adjust the network name (`redis-streams-demo_redis-net`) based on your project directory name.*
    *You may also need to configure the application inside the container to connect to `redis` (the service name in `docker-compose.yml`) instead of `localhost`. This can be done via environment variables when running the container:*
    ```bash
    docker run --rm --name redis-streams-app --network redis-streams-demo_redis-net -p 8080:8080 -e SPRING_DATA_REDIS_HOST=redis redis-streams-demo-app
    ```

## Configuration

*   **Redis Connection:** Configure Redis host and port in `src/main/resources/application.properties`. Default is `localhost:6379`.
*   **Stream Details:** The stream key (`my-stream`), consumer group (`my-group`), and consumer name (`consumer-1`) are configured in `src/main/resources/application.properties`.
*   **Consumer Setup:** The `StreamMessageListenerContainer` and subscription logic are defined in `src/main/java/com/example/demo/config/RedisConfig.java`.
*   **Publisher Schedule:** The publishing interval (5000ms) is set via `@Scheduled` in `src/main/java/com/example/demo/publisher/DataPublisher.java`.

## Project Structure

```
redis-streams-demo/
├── build/                     # Build output
├── gradle/                    # Gradle wrapper files
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/ # Main application code
│   │   │   ├── config/          # RedisConfig.java (Stream listener setup)
│   │   │   ├── consumer/        # DataConsumer.java (Message handler)
│   │   │   ├── model/           # DummyData.java (Data model)
│   │   │   └── publisher/       # DataPublisher.java (Scheduled publisher)
│   │   └── resources/           # application.properties, static/, templates/
│   └── test/                    # Test code
├── .gitattributes
├── .gitignore
├── build.gradle               # Gradle build script
├── docker-compose.yml         # Docker Compose configuration for Redis ONLY
├── Dockerfile                 # Dockerfile for the Spring Boot application
├── gradlew                    # Gradle wrapper script (Linux/macOS)
├── gradlew.bat                # Gradle wrapper script (Windows)
├── HELP.md
├── README.md                  # This file
└── settings.gradle
``` 