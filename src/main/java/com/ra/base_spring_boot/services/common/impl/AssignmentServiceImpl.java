package com.ra.base_spring_boot.services.common.impl;

import com.ra.base_spring_boot.model.Assignment;
import com.ra.base_spring_boot.repository.common.IAssignmentRepository;
import com.ra.base_spring_boot.services.common.IAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements IAssignmentService {

    private final IAssignmentRepository assignmentRepository;

    @Override
    public Assignment create(Assignment assignment) {
        return assignmentRepository.save(Objects.requireNonNull(assignment, "assignment must not be null"));
    }

    @Override
    public List<Assignment> findByCourse(Long courseId) {
        Long safeCourseId = Objects.requireNonNull(courseId, "courseId must not be null");
        return assignmentRepository.findAll()
                .stream()
                .filter(a -> a.getCourse() != null && a.getCourse().getId() != null && a.getCourse().getId().equals(safeCourseId))
                .toList();
    }
}

