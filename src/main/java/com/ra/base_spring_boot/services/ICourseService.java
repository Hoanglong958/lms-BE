package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.CourseCreateReq;
import com.ra.base_spring_boot.dto.req.CourseUpdateReq;
import com.ra.base_spring_boot.dto.resp.CourseResp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICourseService {
    CourseResp create(CourseCreateReq req);
    CourseResp get(Integer id);
    Page<CourseResp> search(String keyword, Pageable pageable);
    CourseResp update(Integer id, CourseUpdateReq req);
    void delete(Integer id);
}
