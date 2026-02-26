package com.ra.base_spring_boot.repository.classroom;

import com.ra.base_spring_boot.model.ScheduleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {

        // Lấy lịch theo class_course
        List<ScheduleItem> findByClassCourse_IdOrderBySessionNumber(Long classCourseId);

        // Xóa lịch theo class_course
        void deleteByClassCourse_Id(Long classCourseId);

        // Kiểm tra tồn tại lịch
        boolean existsByClassCourse_Id(Long classCourseId);

        List<ScheduleItem> findByClassCourse_Course_IdOrderByDateAscSessionNumberAsc(Long courseId);

        // Fetch đủ dữ liệu cho FE
        @Query("""
                            SELECT si
                            FROM ScheduleItem si
                            JOIN FETCH si.period
                            JOIN FETCH si.classCourse cc
                            JOIN FETCH cc.course
                            JOIN FETCH cc.clazz
                            WHERE cc.id = :classCourseId
                            ORDER BY si.sessionNumber
                        """)
        List<ScheduleItem> findScheduleDetailByClassCourse(
                        @Param("classCourseId") Long classCourseId);

        // Find schedule items by class ID and date
        @Query("""
                            SELECT si
                            FROM ScheduleItem si
                            JOIN FETCH si.period
                            JOIN FETCH si.classCourse cc
                            WHERE cc.clazz.id = :classId
                            AND si.date = :date
                        """)
        List<ScheduleItem> findByClassIdAndDate(
                        @Param("classId") Long classId,
                        @Param("date") java.time.LocalDate date);

        // Find all scheduled dates for a class within a month
        @Query("""
                            SELECT DISTINCT si.date
                            FROM ScheduleItem si
                            JOIN si.classCourse cc
                            WHERE cc.clazz.id = :classId
                            AND FUNCTION('YEAR', si.date) = :year
                            AND FUNCTION('MONTH', si.date) = :month
                            ORDER BY si.date ASC
                        """)
        List<java.time.LocalDate> findScheduledDatesByClassAndMonth(
                        @Param("classId") Long classId,
                        @Param("year") int year,
                        @Param("month") int month);
}
