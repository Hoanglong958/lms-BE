package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.model.Assignment;
import com.ra.base_spring_boot.repository.IAssignmentRepository;
import com.ra.base_spring_boot.services.IAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements IAssignmentService {

    private final IAssignmentRepository assignmentRepository;

    @Override
    public Assignment create(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    @Override
    public List<Assignment> findByCourse(Long courseId) {
        return assignmentRepository.findAll()
                .stream()
                .filter(a -> a.getCourse().getId().equals(courseId))
                .toList();
    }
}

