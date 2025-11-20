package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.model.Assignment;
import com.ra.base_spring_boot.services.IAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final IAssignmentService assignmentService;

    @PostMapping
    public Assignment create(@RequestBody Assignment assignment) {
        return assignmentService.create(assignment);
    }

    @GetMapping("/course/{courseId}")
    public List<Assignment> getByCourse(@PathVariable Long courseId) {
        return assignmentService.findByCourse(courseId);
    }
}
