package com.xuecheng.content.api;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.utils.StringUtil;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Api(value = "课程信息编辑接口", tags = "课程信息编辑接口")
@RestController
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParams){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        if(StringUtils.isNotEmpty(user.getCompanyId())){
            companyId = Long.parseLong(user.getCompanyId());
        }
        PageResult<CourseBase> pageResult = courseBaseInfoService.queryCourseBaseList(companyId, pageParams, queryCourseParams);
        return pageResult;
    }

    @ApiOperation("新增课程基础信息")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated({
            ValidationGroups.Insert.class}) AddCourseDto addCourseDto){
        Long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBase(companyId,addCourseDto);
    }

    @ApiOperation("根据课程id查询课程基础信息")
    @GetMapping("/course/{id}")
    public CourseBaseInfoDto getCourseBase(@PathVariable Long id){
        //获取当前用户身份
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println(user);
        return courseBaseInfoService.getCourseBaseInfo(id);
    }

    @ApiOperation("修改课程基础信息")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated(ValidationGroups.Update.class) EditCourseDto editCourseDto){
        Long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBaseInfo(companyId, editCourseDto);
    }

    @ApiOperation("/删除课程信息")
    @DeleteMapping("/course/{id}")
    public void deleteCourseBase(@PathVariable Long id){
        Long companyId = 1232141425L;
        courseBaseInfoService.deleteCourseBaseInfo(companyId, id);
    }
}
