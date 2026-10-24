package com.shestikpetr.taskservice.task

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tasks")
class TaskController(
    private val taskService: TaskService,
) {
    @GetMapping
    fun findAll(): List<Task> = taskService.findAll()

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: Long,
    ): ResponseEntity<Task> = ResponseEntity.ofNullable(taskService.findById(id))

    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateTaskRequest,
    ): ResponseEntity<Task> = ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request.title))

    @PostMapping("/{id}/complete")
    fun complete(
        @PathVariable id: Long,
    ): ResponseEntity<Task> = ResponseEntity.ofNullable(taskService.complete(id))

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Void> =
        if (taskService.delete(id)) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
}
