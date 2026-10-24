package com.shestikpetr.taskservice.task

import jakarta.validation.constraints.NotBlank

data class Task(
    val id: Long,
    val title: String,
    val done: Boolean = false,
)

data class CreateTaskRequest(
    @field:NotBlank
    val title: String,
)
