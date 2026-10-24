package com.shestikpetr.taskservice

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

// GenericContainer is self-typed (GenericContainer<SELF>); Kotlin needs a concrete subclass to use it
class RedisContainer : GenericContainer<RedisContainer>("redis:7") {
    init {
        withExposedPorts(PORT)
        // An open port is not enough: Docker Desktop's port proxy accepts connections before Redis is ready
        waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1))
    }

    companion object {
        const val PORT = 6379
    }
}
