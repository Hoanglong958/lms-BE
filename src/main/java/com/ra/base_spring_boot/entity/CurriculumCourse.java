package com.ra.base_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Curriculum_Course")
public class CurriculumCourse {
    @EmbeddedId
    private CurriculumCourseId id;

    @ManyToOne(optional = false)
    @MapsId("curriculumId")
    @JoinColumn(name = "curriculum_id", referencedColumnName = "curriculum_id")
    private Curriculum curriculum;

    @ManyToOne(optional = false)
    @MapsId("courseId")
    @JoinColumn(name = "course_id", referencedColumnName = "course_id")
    private Course course;
}
