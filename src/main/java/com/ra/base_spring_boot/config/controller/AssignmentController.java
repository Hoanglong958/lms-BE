package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.model.Assignment;
import com.ra.base_spring_boot.services.IAssignmentService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Tag(name = "29 - Assignments", description = "Quản lý bài tập")
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
