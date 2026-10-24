package com.shestikpetr.taskservice.visits

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@JsonInclude(JsonInclude.Include.NON_NULL)
data class VisitResult(
    val count: Long,
    val source: String,
    val lastId: Long? = null,
)

@Service
class VisitService(
    private val jdbc: JdbcTemplate,
    private val redis: StringRedisTemplate,
) {
    fun visit(): VisitResult {
        redis.opsForValue().get(CACHE_KEY)?.let {
            return VisitResult(count = it.toLong(), source = "cache")
        }

        val id = jdbc.queryForObject("INSERT INTO visits (created_at) VALUES (NOW()) RETURNING id", Long::class.java)!!
        val count = jdbc.queryForObject("SELECT COUNT(*) FROM visits", Long::class.java)!!

        redis.opsForValue().set(CACHE_KEY, count.toString(), CACHE_TTL)
        return VisitResult(count = count, source = "db", lastId = id)
    }

    companion object {
        const val CACHE_KEY = "visits_count"
        val CACHE_TTL: Duration = Duration.ofSeconds(10)
    }
}
