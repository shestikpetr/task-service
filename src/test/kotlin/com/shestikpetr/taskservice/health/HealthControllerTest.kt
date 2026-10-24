package com.shestikpetr.taskservice.health

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.random.Random
import kotlin.test.assertEquals

class HealthControllerTest {
    @Test
    fun `healthy build always answers ok`() {
        val controller = HealthController(version = "v1", chaosEnabled = false, failureRate = 1.0)

        val response = controller.health()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(mapOf("status" to "ok", "version" to "v1"), response.body)
    }

    @Test
    fun `chaos build fails when the random value is below the failure rate`() {
        val controller = HealthController(version = "broken", chaosEnabled = true, failureRate = 0.3)
        controller.random = FixedRandom(0.1)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.health().statusCode)
    }

    @Test
    fun `chaos build succeeds when the random value is above the failure rate`() {
        val controller = HealthController(version = "broken", chaosEnabled = true, failureRate = 0.3)
        controller.random = FixedRandom(0.9)

        assertEquals(HttpStatus.OK, controller.health().statusCode)
    }

    private class FixedRandom(
        private val value: Double,
    ) : Random() {
        override fun nextBits(bitCount: Int): Int = 0

        override fun nextDouble(): Double = value
    }
}
