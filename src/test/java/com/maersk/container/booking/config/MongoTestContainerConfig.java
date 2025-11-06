package com.maersk.container.booking.config;


import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class MongoTestContainerConfig {

    private static final MongoDBContainer MONGO_CONTAINER =
            new MongoDBContainer(DockerImageName.parse("mongo:7.0.5"));

    static {
        MONGO_CONTAINER.start();
    }

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_CONTAINER::getReplicaSetUrl);
    }
}
