package com.shestikpetr.taskservice.metrics

import io.micrometer.core.instrument.MeterRegistry
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Counts responses as http_requests_total{status_code="..."} — the metric name and label
 * the canary AnalysisTemplate queries. Actuator endpoints (Prometheus scrapes) are not counted.
 */
@Component
class HttpRequestMetricsFilter(
    private val registry: MeterRegistry,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            filterChain.doFilter(request, response)
        } finally {
            registry.counter(METRIC_NAME, "status_code", response.status.toString()).increment()
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean = request.requestURI.startsWith("/actuator")

    companion object {
        // Micrometer renders the counter "http.requests" as http_requests_total in Prometheus format
        const val METRIC_NAME = "http.requests"
    }
}
