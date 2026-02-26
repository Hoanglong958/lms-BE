package com.ra.base_spring_boot.services.common;

import com.ra.base_spring_boot.model.Assignment;

import java.util.List;

public interface IAssignmentService {
    Assignment create(Assignment assignment);
    List<Assignment> findByCourse(Long courseId);
}
