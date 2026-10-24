package com.shestikpetr.taskservice.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.random.Random

/**
 * Health endpoint used by Kubernetes probes and by the canary demo.
 * A build with CHAOS_MODE=true answers 500 to a share of the requests,
 * which is what the AnalysisTemplate is expected to catch.
 */
@RestController
class HealthController(
    @Value("\${app.version}") private val version: String,
    @Value("\${app.chaos.enabled}") private val chaosEnabled: Boolean,
    @Value("\${app.chaos.failure-rate}") private val failureRate: Double,
) {
    internal var random: Random = Random.Default

    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> =
        if (chaosEnabled && random.nextDouble() < failureRate) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("status" to "error", "version" to version))
        } else {
            ResponseEntity.ok(mapOf("status" to "ok", "version" to version))
        }
}
