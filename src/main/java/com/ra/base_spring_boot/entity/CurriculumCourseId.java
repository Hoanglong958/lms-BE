package com.ra.base_spring_boot.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CurriculumCourseId implements Serializable {
    private Integer curriculumId;
    private Integer courseId;
}
