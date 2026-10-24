package com.shestikpetr.taskservice

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

/**
 * Starts the whole application against real PostgreSQL and Redis:
 * checks configuration, schema.sql on startup and the HTTP layer of /visits.
 * Containers are per class here because the Spring context is shared by its tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TaskServiceApplicationIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `visits endpoint works against real database and cache`() {
        mockMvc.get("/visits").andExpect {
            status { isOk() }
            jsonPath("$.source") { value("db") }
            jsonPath("$.count") { value(1) }
        }
        mockMvc.get("/visits").andExpect {
            status { isOk() }
            jsonPath("$.source") { value("cache") }
        }
    }

    @Test
    fun `health includes database and redis`() {
        mockMvc.get("/actuator/health").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("UP") }
        }
    }

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16")

        @Container
        @ServiceConnection(name = "redis")
        @JvmStatic
        val redis = RedisContainer()
    }
}
