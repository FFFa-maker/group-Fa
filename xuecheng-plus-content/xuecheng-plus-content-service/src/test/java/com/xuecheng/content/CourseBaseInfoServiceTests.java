package com.xuecheng.content;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CourseBaseInfoServiceTests {
    @Autowired
    private CourseBaseInfoService service;
    @Test
    public void testCourseBaseInfoService(){
        QueryCourseParamsDto queryCourseParamsDto = QueryCourseParamsDto.builder()
                .courseName("java")
                .auditStatus("202004")
                .publishStatus("203001")
                .build();
        PageParams pageParams = PageParams.builder()
                .pageNo(1L)
                .pageSize(3L)
                .build();
        PageResult<CourseBase> courseBasePageResult = service.queryCourseBaseList(pageParams, queryCourseParamsDto);
        System.out.println(courseBasePageResult);
    }
}
