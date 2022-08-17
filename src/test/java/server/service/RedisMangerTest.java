package server.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisMangerTest {
    @Autowired
    RedisManger redisManger;

    @Test
    void setKey() {
        redisManger.setKey("123","", Duration.ofSeconds(30)).block();
    }
}