package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.model.Classroom;
import com.ra.base_spring_boot.services.IClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final IClassroomService classroomService;

    @PostMapping
    public Classroom create(@RequestBody Classroom classroom) {
        return classroomService.create(classroom);
    }

    @GetMapping
    public List<Classroom> getAll() {
        return classroomService.findAll();
    }
}
