package com.shestikpetr.taskservice.task

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaskServiceTest {
    private val service = TaskService()

    @Test
    fun `create assigns sequential ids`() {
        val first = service.create("first")
        val second = service.create("second")

        assertEquals(1, first.id)
        assertEquals(2, second.id)
        assertFalse(first.done)
        assertEquals(listOf(first, second), service.findAll())
    }

    @Test
    fun `complete marks task as done`() {
        val task = service.create("task")

        assertTrue(service.complete(task.id)!!.done)
        assertTrue(service.findById(task.id)!!.done)
    }

    @Test
    fun `complete returns null for unknown task`() {
        assertNull(service.complete(42))
    }

    @Test
    fun `delete removes task`() {
        val task = service.create("task")

        assertTrue(service.delete(task.id))
        assertNull(service.findById(task.id))
        assertFalse(service.delete(task.id))
    }
}
