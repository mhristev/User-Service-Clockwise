package com.clockwise.userservice

import com.clockwise.userservice.config.TestKafkaConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@Import(TestcontainersConfiguration::class, TestKafkaConfig::class)
@SpringBootTest
@ActiveProfiles("test")
class UserserviceApplicationTests {

	@Test
	fun contextLoads() {
	}

}
