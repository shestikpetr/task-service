package com.shestikpetr.taskservice.task

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Service
class TaskService {
    private val tasks = ConcurrentHashMap<Long, Task>()
    private val nextId = AtomicLong(1)

    fun findAll(): List<Task> = tasks.values.sortedBy { it.id }

    fun findById(id: Long): Task? = tasks[id]

    fun create(title: String): Task {
        val task = Task(id = nextId.getAndIncrement(), title = title)
        tasks[task.id] = task
        return task
    }

    fun complete(id: Long): Task? = tasks.computeIfPresent(id) { _, task -> task.copy(done = true) }

    fun delete(id: Long): Boolean = tasks.remove(id) != null
}
