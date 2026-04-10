package com.ra.base_spring_boot.services.common;

public interface IReportService {
    byte[] exportUsers(String type);

    byte[] exportCourses(String type);

    byte[] exportStudentProgress(String type);

    byte[] exportQuizReports(String type);

    byte[] exportRevenue(String type);
}
