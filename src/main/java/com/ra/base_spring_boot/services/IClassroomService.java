package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.model.Classroom;
import java.util.List;

public interface IClassroomService {
    Classroom create(Classroom classroom);
    List<Classroom> findAll();
}
