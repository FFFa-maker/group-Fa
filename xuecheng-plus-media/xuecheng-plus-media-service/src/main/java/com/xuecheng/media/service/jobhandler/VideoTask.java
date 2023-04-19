package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class VideoTask {

    @Autowired
    MediaFileService mediaFileService;
    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegPath;

    @XxlJob("videoJobHandler")
    public void shardingJobHandler() throws Exception{
        //分片参数
        int shardTotal = XxlJobHelper.getShardTotal();
        int shardIndex = XxlJobHelper.getShardIndex();
        //确定CPU核心数
        int processors = Runtime.getRuntime().availableProcessors();
        //查询
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
        //任务数量
        int size = mediaProcessList.size();
        log.debug("任务数量: {}",size);
        if(size<=0){
            return;
        }
        //创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        //使用计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess -> {
            executorService.execute(()->{
                try{//抢占
                    Long taskId = mediaProcess.getId();
                    boolean b = mediaFileProcessService.startTask(taskId);
                    if (!b) {
                        log.debug("抢占任务失败， 任务id{}", taskId);
                        return;
                    }
                    log.debug("开始执行任务{}", taskId);

                    //下载
                    String bucket = mediaProcess.getBucket();
                    String objectName = mediaProcess.getFilePath();
                    String fileId = mediaProcess.getFileId();
                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                    if (file == null) {
                        log.debug("下载视频出错,id：{},bucket：{},objectName：{}", taskId, bucket, objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频到本地失败");
                        return;
                    }
                    //处理
                    String video_path = file.getAbsolutePath();
                    String mp4_name = fileId + ".mp4";
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.debug("创建临时文件异常, {}", e.getMessage());
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件异常");
                    }

                    String mp4_path = mp4File.getAbsolutePath();
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegPath, video_path, mp4_name, mp4_path);
                    //视频转码
                    String result = videoUtil.generateMp4();
                    if (!result.equals("success")) {
                        log.debug("视频转码失败, 原因: {}, bucket: {}, objectName: {}", result, bucket, objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "视频转码失败");
                        return;
                    }
                    //上传
                    String newObjectName = getFilePathByMd5(fileId, ".mp4");
                    boolean b1 = mediaFileService.addMediaFilesToMinIO(mp4_path, "video/mp4", bucket, newObjectName);
                    if (!b1) {
                        log.debug("上传MP4到minio失败, taskId: {}", taskId);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传视频到minio失败");
                        return;
                    }
                    //mp4的url
                    String url = "/" + bucket+"/"+newObjectName;
                    //保存
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, null);
                }finally {
                    countDownLatch.countDown();
                }
            });
        });
        //阻塞,最多等待30min
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }
}
