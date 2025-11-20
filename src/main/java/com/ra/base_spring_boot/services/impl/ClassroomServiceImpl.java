package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.model.Classroom;
import com.ra.base_spring_boot.repository.IClassroomRepository;
import com.ra.base_spring_boot.services.IClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassroomServiceImpl implements IClassroomService {

    private final IClassroomRepository classroomRepository;

    @Override
    public Classroom create(Classroom classroom) {
        return classroomRepository.save(classroom);
    }

    @Override
    public List<Classroom> findAll() {
        return classroomRepository.findAll();
    }
}

