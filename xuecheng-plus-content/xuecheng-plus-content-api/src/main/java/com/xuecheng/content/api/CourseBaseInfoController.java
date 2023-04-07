package com.xuecheng.content.api;

import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CourseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Api(value = "课程信息编辑接口", tags = "课程信息编辑接口")
@RestController
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseMapper mapper;

    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    public PageResult list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParams){
        CourseBase courseBase = new CourseBase();
        courseBase.setName("测试");
        courseBase.setCreateDate(LocalDateTime.now());
        List<CourseBase> list = new ArrayList();
        list.add(courseBase);
        return new PageResult(list, 10, 1, 5);
    }
}
