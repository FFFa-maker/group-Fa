package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class CourseTeacherController {

    @Autowired
    private CourseTeacherService courseTeacherService;

    @ApiOperation("查询教师信息")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacherDto> getCourseTeacher(@PathVariable long courseId){
        return courseTeacherService.getCourseTeacherByCourseId(courseId);
    }

    @ApiOperation("添加教师信息")
    @PostMapping("/courseTeacher")
    public CourseTeacherDto insertCourseTeacher(@RequestBody CourseTeacherDto dto){
        Long companyId = 1232141425L;
        return courseTeacherService.insertCourseTeacher(companyId, dto);
    }

    @ApiOperation("修改教师信息")
    @PutMapping("/courseTeacher")
    public CourseTeacherDto updateCourseTeacher(@RequestBody CourseTeacherDto dto){
        Long companyId = 1232141425L;
        return courseTeacherService.insertCourseTeacher(companyId, dto);
    }

    @DeleteMapping("/courseTeacher/course/{courseId}/{id}")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long id){
        Long companyId = 1232141425L;
        courseTeacherService.deleteCourseTeacher(companyId, courseId, id);
    }
}
