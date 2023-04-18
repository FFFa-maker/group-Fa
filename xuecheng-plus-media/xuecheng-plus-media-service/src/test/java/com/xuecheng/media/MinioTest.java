package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Test
    public void uploadChunk(){
        String chunkFolderPath = "D:\\develop\\upload\\chunk\\";
        File chunkFolder = new File(chunkFolderPath);
        File[] files = chunkFolder.listFiles();
        for (int i = 0; i < files.length; i++) {
            try {
                UploadObjectArgs args = UploadObjectArgs.builder()
                        .bucket("test-bucket")
                        .object("chunk/"+i)
                        .filename(files[i].getAbsolutePath())
                        .build();
                minioClient.uploadObject(args);
                System.out.println("上传分块"+i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void merge() throws Exception{
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(2)
                .map(i->ComposeSource.builder()
                        .bucket("test-bucket")
                        .object("chunk/"+i)
                        .build())
                .collect(Collectors.toList());
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("test-bucket")
                .object("merge01.mp4")
                .sources(sources)
                .build();
        minioClient.composeObject(composeObjectArgs);
    }

    @Test
    public void deleteChunks(){
        List<DeleteObject> deleteObjects = Stream.iterate(0, i->++i)
                .limit(2)
                .map(i->new DeleteObject("/1/8/1847fb6aae7e4b8916dc52c3e1106c49/chunk/"+i))
                .collect(Collectors.toList());
        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder()
                .bucket("video")
                .objects(deleteObjects)
                .build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        results.forEach(r->{
            DeleteError deleteError = null;
            try{
                deleteError = r.get();
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }
}
