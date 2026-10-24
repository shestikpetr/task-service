package com.shestikpetr.taskservice.visits

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VisitCacheIT {
    private lateinit var env: VisitTestEnvironment

    @BeforeEach
    fun setUp() {
        env = VisitTestEnvironment()
    }

    @AfterEach
    fun tearDown() {
        env.close()
    }

    @Test
    fun `second request within TTL is served from cache`() {
        // The test warms the cache itself instead of relying on another test
        env.service.visit()

        val result = env.service.visit()

        assertEquals("definitely-not-cache", result.source)
        assertEquals(1, result.count)
        assertNull(result.lastId)
    }

    @Test
    fun `cached response does not insert new records`() {
        env.service.visit()
        repeat(3) { env.service.visit() }
        env.clearCache()

        assertEquals(2, env.service.visit().count)
    }
}
