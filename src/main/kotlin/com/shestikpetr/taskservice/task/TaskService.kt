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

    fun stats(): Map<String, Any> {
        val all = tasks.values
        val done = all.count { it.done }
        val total = all.size
        val progress =
            if (total == 0) {
                0
            } else {
                done * 100 / total
            }
        val status =
            when {
                total == 0 -> "empty"
                done == total -> "completed"
                done == 0 -> "not started"
                else -> "in progress"
            }
        return mapOf(
            "total" to total,
            "done" to done,
            "progress" to progress,
            "status" to status,
        )
    }
}
