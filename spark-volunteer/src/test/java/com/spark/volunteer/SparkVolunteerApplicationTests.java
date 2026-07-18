package com.spark.volunteer;

import com.spark.volunteer.config.TestRedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
class SparkVolunteerApplicationTests {

    @Test
    void contextLoads() {
    }

}
