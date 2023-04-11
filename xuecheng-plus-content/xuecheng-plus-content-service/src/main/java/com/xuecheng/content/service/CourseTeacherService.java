package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseTeacherDto;

import java.util.List;

public interface CourseTeacherService {
    List<CourseTeacherDto> getCourseTeacherByCourseId(Long courseId);
    CourseTeacherDto insertCourseTeacher(Long companyId, CourseTeacherDto courseTeacherDto);
    CourseTeacherDto updateCourseTeacher(Long companyId, CourseTeacherDto courseTeacherDto);
    void deleteCourseTeacher(Long companyId, Long courseId, Long id);
}
