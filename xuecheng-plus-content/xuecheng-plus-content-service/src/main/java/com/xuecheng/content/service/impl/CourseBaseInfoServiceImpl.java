package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        //查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                CourseBase::getName, queryCourseParamsDto.getCourseName());
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),
                CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());

        //页面参数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //查询结果
        Page<CourseBase> result = courseBaseMapper.selectPage(page, queryWrapper);
        //
        List<CourseBase> items = result.getRecords();
        long total = result.getTotal();
        //构造结果
        PageResult<CourseBase> pageResult = PageResult.<CourseBase>builder()
                .items(items)
                .page(pageParams.getPageNo())
                .pageSize(pageParams.getPageSize())
                .counts(total)
                .build();
        return pageResult;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        //合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            throw new RuntimeException("课程名称为空");
        }
        if (StringUtils.isBlank(dto.getMt())) {
            throw new RuntimeException("课程分类为空");
        }
        if (StringUtils.isBlank(dto.getSt())) {
            throw new RuntimeException("课程分类为空");
        }
        if (StringUtils.isBlank(dto.getGrade())) {
            throw new RuntimeException("课程等级为空");
        }
        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new RuntimeException("教育模式为空");
        }
        if (StringUtils.isBlank(dto.getUsers())) {
            throw new RuntimeException("适应人群为空");
        }
        if (StringUtils.isBlank(dto.getCharge())) {
            throw new RuntimeException("收费规则为空");
        }
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(dto, courseBase);
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        int insert = courseBaseMapper.insert(courseBase);
        if(insert <= 0){
            throw new RuntimeException("新增课程基本信息失败");
        }

        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);
        Long id = courseBase.getId();
        courseMarket.setId(id);
        saveCourseMarket(courseMarket);
        CourseBaseInfoDto courseBaseInfoDto=getCourseBaseInfo(id);
        return courseBaseInfoDto;
    }

    @Override
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId){
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase==null){
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if(courseMarket!=null){
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());
        return courseBaseInfoDto;
    }

    @Override
    public CourseBaseInfoDto updateCourseBaseInfo(Long companyId, EditCourseDto dto){
        Long id = dto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if(courseBase==null){
            XueChengPlusException.cast("课程不存在");
        }
        if(!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }
        BeanUtils.copyProperties(dto, courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        int i = courseBaseMapper.updateById(courseBase);

        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        BeanUtils.copyProperties(dto, courseMarket);
        saveCourseMarket(courseMarket);
        CourseBaseInfoDto courseBaseInfoDto = getCourseBaseInfo(id);
        return courseBaseInfoDto;
    }

    @Override
    public void deleteCourseBaseInfo(Long companyId, Long courseId){
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase==null){
            XueChengPlusException.cast("课程不存在");
        }
        if(!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("本机构只能删除本机构的课程");
        }
        if(!courseBase.getAuditStatus().equals("202002")){
            XueChengPlusException.cast("只能删除审核状态未提交的课程");
        }
        //删除课程基本信息、营销信息、课程计划、课程计划媒体信息以及课程教师信息
        courseBaseMapper.deleteById(courseId);
        courseMarketMapper.deleteById(courseId);
        LambdaQueryWrapper<Teachplan> teachplanQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<TeachplanMedia> teachplanMediaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<CourseTeacher> teacherQueryWrapper = new LambdaQueryWrapper<>();
        teachplanQueryWrapper.eq(Teachplan::getCourseId, courseId);
        teachplanMediaQueryWrapper.eq(TeachplanMedia::getCourseId, courseId);
        teacherQueryWrapper.eq(CourseTeacher::getCourseId, courseId);
        teachplanMapper.delete(teachplanQueryWrapper);
        teachplanMediaMapper.delete(teachplanMediaQueryWrapper);
        courseTeacherMapper.delete(teacherQueryWrapper);
    }

    private int saveCourseMarket(CourseMarket courseMarket){
        String charge = courseMarket.getCharge();
        if(StringUtils.isEmpty(charge)){
            throw new RuntimeException("收费规则为空");
        }
        if(charge.equals("201001")){
            if(courseMarket.getPrice()==null||courseMarket.getPrice().floatValue()<=0){
                throw new XueChengPlusException("课程价格不能为空并且必须大于0");
            }
        }
        Long id = courseMarket.getId();
        CourseMarket courseMerketOther = courseMarketMapper.selectById(id);
        if(courseMerketOther==null){
            int insert = courseMarketMapper.insert(courseMarket);
            return insert;
        }else{
            BeanUtils.copyProperties(courseMarket, courseMerketOther);
            courseMerketOther.setId(courseMarket.getId());
            int i = courseMarketMapper.updateById(courseMerketOther);
            return i;
        }
    }
}
