package com.shestikpetr.taskservice.visits

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class VisitController(
    private val visitService: VisitService,
) {
    @GetMapping("/visits")
    fun visit(): VisitResult = visitService.visit()
}
