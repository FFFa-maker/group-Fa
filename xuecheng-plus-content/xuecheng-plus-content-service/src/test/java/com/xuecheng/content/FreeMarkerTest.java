package com.xuecheng.content;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SpringBootTest
public class FreeMarkerTest {
    @Autowired
    CoursePublishService coursePublishService;

    @Test
    public void testGenerateHtmlByTemplate() throws IOException, TemplateException {
        Configuration configuration = new Configuration(Configuration.getVersion());
        //路径
        String path = this.getClass().getResource("/").getPath();
        //指定目录
        configuration.setDirectoryForTemplateLoading(new File("D:\\JetBrains\\work\\group-Fa\\xuecheng-plus-content\\xuecheng-plus-content-service\\src\\test\\resources\\templates\\"));
        //encoding
        configuration.setDefaultEncoding("utf-8");
        //指定template
        Template template = configuration.getTemplate("course_template.ftl");

        //准备数据
        CoursePreviewDto coursePreviewDto = coursePublishService.getCoursePreviewInfo(2L);
        Map<String, Object> map = new HashMap<>();
        map.put("model", coursePreviewDto);

        //template, model
        String s = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

        InputStream inputStream = IOUtils.toInputStream(s);
        FileOutputStream outputStream = new FileOutputStream(new File("D:\\develop\\upload\\2.html"));
        IOUtils.copy(inputStream, outputStream);
    }
}
