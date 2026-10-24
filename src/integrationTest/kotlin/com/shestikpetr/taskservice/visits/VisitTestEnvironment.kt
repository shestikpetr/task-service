package com.shestikpetr.taskservice.visits

import com.shestikpetr.taskservice.RedisContainer
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import org.testcontainers.lifecycle.Startables
import org.testcontainers.postgresql.PostgreSQLContainer

/**
 * Fresh PostgreSQL and Redis containers plus a [VisitService] wired to them.
 * Created per test, so tests never share state and can run in parallel:
 * Testcontainers maps every container to a random free host port.
 */
class VisitTestEnvironment : AutoCloseable {
    private val postgres = PostgreSQLContainer("postgres:16")
    private val redis = RedisContainer()
    private val redisConnectionFactory: LettuceConnectionFactory
    private val containerIds: String
    val service: VisitService

    init {
        Startables.deepStart(postgres, redis).join()
        containerIds = "PostgreSQL ${postgres.containerId.take(12)} and Redis ${redis.containerId.take(12)}"
        log.info("Started {}", containerIds)

        val dataSource = DriverManagerDataSource(postgres.jdbcUrl, postgres.username, postgres.password)
        // Same schema.sql the application applies on startup
        ResourceDatabasePopulator(ClassPathResource("schema.sql")).execute(dataSource)

        redisConnectionFactory =
            LettuceConnectionFactory(redis.host, redis.getMappedPort(RedisContainer.PORT)).apply {
                afterPropertiesSet()
                start()
            }
        service = VisitService(JdbcTemplate(dataSource), StringRedisTemplate(redisConnectionFactory))
    }

    fun clearCache() {
        redisConnectionFactory.connection.use { it.serverCommands().flushAll() }
    }

    // Connections first, then containers
    override fun close() {
        redisConnectionFactory.destroy()
        redis.stop()
        postgres.stop()
        log.info("Stopped {}", containerIds)
    }

    companion object {
        private val log = LoggerFactory.getLogger(VisitTestEnvironment::class.java)
    }
}
