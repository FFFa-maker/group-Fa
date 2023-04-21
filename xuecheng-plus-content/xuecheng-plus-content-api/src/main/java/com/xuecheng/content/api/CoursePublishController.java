package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CoursePublishController {

    @Autowired
    CoursePublishService coursePublishService;


    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){
        ModelAndView modelAndView = new ModelAndView();
        //查询信息拼装
        CoursePreviewDto coursePreviewDto = coursePublishService.getCoursePreviewInfo(courseId);
        modelAndView.addObject("model", coursePreviewDto);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }
}
