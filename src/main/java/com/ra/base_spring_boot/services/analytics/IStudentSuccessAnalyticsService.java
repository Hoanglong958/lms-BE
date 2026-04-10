package com.ra.base_spring_boot.services.analytics;

import com.ra.base_spring_boot.dto.Classroom.ClassroomResponseDTO;
import com.ra.base_spring_boot.dto.analytics.StudentSuccessAnalyticsResponse;
import com.ra.base_spring_boot.model.User;

import java.util.List;

public interface IStudentSuccessAnalyticsService {

    StudentSuccessAnalyticsResponse getByClass(Long classId, User currentUser);

    List<ClassroomResponseDTO> getAccessibleClasses(User currentUser);
}
