package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.utils.ListUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;
    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Override
    public List<CourseTeacherDto> getCourseTeacherByCourseId(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        List<CourseTeacher> teachers = courseTeacherMapper.selectList(queryWrapper);
        CourseTeacherDto dto = new CourseTeacherDto();
        List<CourseTeacherDto> dtos = new ArrayList<>();
        if (teachers == null || teachers.size() == 0) {
            XueChengPlusException.cast("未查询到该课程教师信息");
        }
        try {
            ListUtils.copyProperties(teachers, dtos, CourseTeacherDto.class);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return dtos;
    }

    @Override
    public CourseTeacherDto insertCourseTeacher(Long companyId, CourseTeacherDto courseTeacherDto) {
        Long courseId = courseTeacherDto.getCourseId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        Long id = courseTeacherDto.getId();
        if (!companyId.equals(courseBase.getCompanyId())) {
            XueChengPlusException.cast("只允许向对应机构中添加教师信息");
        }
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseTeacherDto.getCourseId());
        queryWrapper.eq(CourseTeacher::getTeacherName, courseTeacherDto.getTeacherName());
        //courseId和name相同的个数
        int count = courseTeacherMapper.selectCount(queryWrapper);
        //id不空则修改
        if (id != null) {
            updateCourseTeacher(companyId, courseTeacherDto);
        }
        //id空则添加
        else {
            if (count > 0) {
                XueChengPlusException.cast("无法添加同一位教师信息");
            }
            //courseId和name不同则insert
            CourseTeacher courseTeacher = new CourseTeacher();
            BeanUtils.copyProperties(courseTeacherDto, courseTeacher);
            courseTeacherMapper.insert(courseTeacher);
            id = courseTeacher.getId();

        }

        CourseTeacherDto dto = new CourseTeacherDto();
        CourseTeacher teacher = courseTeacherMapper.selectById(id);
        BeanUtils.copyProperties(teacher, dto);
        return dto;
    }

    @Override
    public CourseTeacherDto updateCourseTeacher(Long companyId, CourseTeacherDto courseTeacherDto) {
        Long courseId = courseTeacherDto.getCourseId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        Long id = courseTeacherDto.getId();
        if (!companyId.equals(courseBase.getCompanyId())) {
            XueChengPlusException.cast("只允许向对应机构中修改教师信息");
        }
        CourseTeacher courseTeacher = courseTeacherMapper.selectById(id);
        if (courseTeacher == null) {
            XueChengPlusException.cast("该教师信息不存在");
        }
        //修改
        else {
            //判断dto是否与其他数据冲突
            LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CourseTeacher::getCourseId, courseTeacherDto.getCourseId());
            queryWrapper.eq(CourseTeacher::getTeacherName, courseTeacherDto.getTeacherName());
            queryWrapper.ne(CourseTeacher::getId, courseTeacherDto.getId());
            //courseId和name相同的个数
            int count = courseTeacherMapper.selectCount(queryWrapper);
            if (count > 0) {
                XueChengPlusException.cast("该教师信息与其他教师信息冲突，同一课程内教师名称不能相同");
            }
            BeanUtils.copyProperties(courseTeacherDto, courseTeacher);
            courseTeacherMapper.updateById(courseTeacher);
        }

        CourseTeacherDto dto = new CourseTeacherDto();
        CourseTeacher teacher = courseTeacherMapper.selectById(id);
        BeanUtils.copyProperties(teacher, dto);
        return dto;
    }

    @Override
    public void deleteCourseTeacher(Long companyId, Long courseId, Long id) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!companyId.equals(courseBase.getCompanyId())) {
            XueChengPlusException.cast("只允许向对应机构中删除教师信息");
        }
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        queryWrapper.eq(CourseTeacher::getId, id);
        CourseTeacher teacher = courseTeacherMapper.selectOne(queryWrapper);
        if (teacher != null) {
            courseTeacherMapper.delete(queryWrapper);
        }

    }
}
