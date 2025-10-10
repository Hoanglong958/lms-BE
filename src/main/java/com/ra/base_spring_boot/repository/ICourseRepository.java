package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Course;
import com.sun.jdi.InterfaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICourseRepository extends JpaRepository<Course, Long> {
}