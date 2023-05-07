package com.xuecheng.learning.service.impl;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LearningServiceImpl implements LearningService {
    @Autowired
    ContentServiceClient contentServiceClient;
    @Autowired
    MediaServiceClient mediaServiceClient;
    @Autowired
    MyCourseTablesService myCourseTablesService;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish==null) {
            return RestResponse.validfail("课程不存在");
        }
        //已经登录
        if(StringUtils.isNotEmpty(userId)){
            //获取学习资格
            XcCourseTablesDto learningStatus = myCourseTablesService.getLearningStatus(userId, courseId);
            String learnStatus = learningStatus.getLearnStatus();
            if ("702001".equals(learnStatus)) {
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            }else if ("702002".equals(learnStatus)) {
                return RestResponse.validfail("无法观看，由于没有选课或选课后没有支付");
            } else if ("702003".equals(learnStatus)) {
                return RestResponse.validfail("您的选课已过期需要申请续期或重新支付");
            }
        }
        String charge = coursepublish.getCharge();
        if ("201000".equals(charge)) {
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }
        return RestResponse.validfail("请购买后继续学习");
    }
}
