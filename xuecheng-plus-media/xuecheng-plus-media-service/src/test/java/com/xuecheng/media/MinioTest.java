package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.*;

public class MinioTest {
    static MinioClient minioClient = MinioClient.builder()
            .endpoint("http://192.168.101.65:9000")
            .credentials("minioadmin", "minioadmin")
            .build();
    @Test
    public void upload(){
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }
        try {
            UploadObjectArgs testBucket = UploadObjectArgs.builder()
                    .bucket("test-bucket")
                    .object("test001.mp4")
                    //.object("001/test001.mp4")
                    .filename("D:\\develop\\upload\\1.mp4")
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(testBucket);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }
    }

    @Test
    public void delete(){
        try{
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket("test-bucket")
                            .object("test001.mp4")
                            .build()
            );
            System.out.println("删除成功");
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("删除失败");
        }
    }

    @Test
    public void getFile(){
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket("test-bucket")
                .object("test001.mp4")
                .build();
        try(FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            FileOutputStream outputStream = new FileOutputStream(new File("D:\\develop\\upload\\1_2.mp4"));
            ) {
            IOUtils.copy(inputStream, outputStream);
            assert check("D:\\develop\\upload\\1.mp4", "D:\\develop\\upload\\1_2.mp4");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean check(String f1, String f2) throws Exception{
        FileInputStream inputStream1 = new FileInputStream(new File(f1));
        String f1_md5 = DigestUtils.md5Hex(inputStream1);
        FileInputStream inputStream2 = new FileInputStream(new File(f2));
        String f2_md5 = DigestUtils.md5Hex(inputStream2);
        return f1_md5.equals(f2_md5);
    }
}
