package com.shestikpetr.taskservice.metrics

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HttpRequestMetricsFilterTest {
    private val registry = SimpleMeterRegistry()
    private val filter = HttpRequestMetricsFilter(registry)

    private fun request(
        uri: String,
        status: Int,
    ) {
        val response = MockHttpServletResponse().apply { this.status = status }
        filter.doFilter(MockHttpServletRequest("GET", uri), response, MockFilterChain())
    }

    @Test
    fun `counts responses by status code`() {
        request("/health", 200)
        request("/health", 200)
        request("/health", 500)

        assertEquals(2.0, registry.counter(HttpRequestMetricsFilter.METRIC_NAME, "status_code", "200").count())
        assertEquals(1.0, registry.counter(HttpRequestMetricsFilter.METRIC_NAME, "status_code", "500").count())
    }

    @Test
    fun `does not count actuator endpoints`() {
        request("/actuator/prometheus", 200)

        assertNull(registry.find(HttpRequestMetricsFilter.METRIC_NAME).counter())
    }
}
