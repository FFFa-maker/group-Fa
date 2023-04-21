package com.xuecheng.content.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class CoursePreviewDto {
//    基本信息、营销信息
    private CourseBaseInfoDto courseBase;
//        计划信息
    private List<TeachplanDto> teachplans;

//    师资信息

}