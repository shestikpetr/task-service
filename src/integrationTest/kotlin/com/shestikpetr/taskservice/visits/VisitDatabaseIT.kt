package com.shestikpetr.taskservice.visits

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VisitDatabaseIT {
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
    fun `first request creates a record via database`() {
        val result = env.service.visit()

        assertEquals("db", result.source)
        assertEquals(1, result.count)
        assertEquals(1, result.lastId)
    }

    @Test
    fun `cache miss inserts a new record`() {
        env.service.visit()
        env.clearCache()

        val result = env.service.visit()

        assertEquals("db", result.source)
        assertEquals(2, result.count)
        assertEquals(2, result.lastId)
    }
}
