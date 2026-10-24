package com.shestikpetr.taskservice.task

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

// Web layer only: no DataSource or Redis is needed, those are covered by integration tests
@WebMvcTest(TaskController::class)
// The web slice also picks up HttpRequestMetricsFilter, which needs a MeterRegistry
@Import(TaskService::class, SimpleMeterRegistry::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private fun createTask(title: String) =
        mockMvc.post("/tasks") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "$title"}"""
        }

    @Test
    fun `GET tasks returns empty list initially`() {
        mockMvc.get("/tasks").andExpect {
            status { isOk() }
            content { json("[]") }
        }
    }

    @Test
    fun `POST tasks creates task`() {
        createTask("Write CI pipeline").andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.title") { value("Write CI pipeline") }
            jsonPath("$.done") { value(false) }
        }

        mockMvc.get("/tasks/1").andExpect {
            status { isOk() }
            jsonPath("$.title") { value("Write CI pipeline") }
        }
    }

    @Test
    fun `POST tasks with blank title returns 400`() {
        createTask(" ").andExpect { status { isBadRequest() } }
    }

    @Test
    fun `GET unknown task returns 404`() {
        mockMvc.get("/tasks/42").andExpect { status { isNotFound() } }
    }

    @Test
    fun `POST complete marks task as done`() {
        createTask("task")

        mockMvc.post("/tasks/1/complete").andExpect {
            status { isOk() }
            jsonPath("$.done") { value(true) }
        }
        mockMvc.post("/tasks/42/complete").andExpect { status { isNotFound() } }
    }

    @Test
    fun `DELETE removes task`() {
        createTask("task")

        mockMvc.delete("/tasks/1").andExpect { status { isNoContent() } }
        mockMvc.delete("/tasks/1").andExpect { status { isNotFound() } }
    }
}
