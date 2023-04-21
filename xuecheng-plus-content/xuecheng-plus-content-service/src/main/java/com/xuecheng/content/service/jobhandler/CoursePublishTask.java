package com.xuecheng.content.service.jobhandler;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex,shardTotal,"course_publish",30,60);
    }


    @Override
    public boolean execute(MqMessage mqMessage) {
        //获取课程信息
        Long courseId = Long.parseLong(mqMessage.getBusinessKey1());
        //静态页面处理
        generateCourseHtml(mqMessage, courseId);
        //elasticsearch
        saveCourseIndex(mqMessage, courseId);
        //redis
        saveCourseCache(mqMessage, courseId);

        return true;
    }

    //静态化处理
    private void generateCourseHtml(MqMessage mqMessage, long courseId) {
        log.debug("开始进行课程静态化,课程id:{}", courseId);
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //任务幂等性处理
        //获取执行状态
        int stageOne = mqMessageService.getStageOne(id);
        if (stageOne > 0) {
            log.debug("课程静态化已处理直接返回，课程id:{}", courseId);
            return;
        }
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        mqMessageService.completedStageOne(id);
    }

    //保存课程索引信息
    public void saveCourseIndex(MqMessage mqMessage, long courseId) {
        log.debug("保存课程索引信息,课程id:{}", courseId);
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(id);
        if (stageTwo > 0) {
            log.debug("");
            return;
        }
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        mqMessageService.completedStageThree(id);
    }

    //课程信息缓存
    public void saveCourseCache(MqMessage mqMessage, long courseId) {
        log.debug("将课程信息缓存至redis,课程id:{}", courseId);
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageThree = mqMessageService.getStageThree(id);
        if (stageThree > 0) {
            log.debug("缓存已存入无需处理，课程id:{}", courseId);
            return;
        }
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        mqMessageService.completedStageTwo(id);
    }
}
