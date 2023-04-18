package com.xuecheng.media;

import io.minio.Digest;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

public class BigFileTest {

    @Test
    public void testChunk() throws IOException {
        File sourceFile = new File("d:\\develop\\upload\\1.avi");
        String chunkPath = "d:\\develop\\upload\\chunk\\";
        File chunkFolder = new File(chunkPath);
        if(!chunkFolder.exists()){
            chunkFolder.mkdirs();
        }
        long chunkSize = 1024*1024*8;
        long chunkNum = (long)Math.ceil(sourceFile.length()*1.0/chunkSize);
        System.out.println("分块总数："+chunkNum);
        byte[] b = new byte[1024];
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
        for (long i = 0; i < chunkNum; i++) {
            File file = new File(chunkPath+i);
            if(file.exists()){
                file.delete();
            }
            boolean newFile = file.createNewFile();
            if(newFile){
                RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
                int len = -1;
                while((len = raf_read.read(b)) != -1){
                    raf_write.write(b, 0, len);
                    if(file.length() >= chunkSize){
                        break;
                    }
                }
                raf_write.close();
                System.out.println("完成分块"+i);
            }
        }
        raf_read.close();
    }


    @Test
    public void testMerge() throws IOException{
        File chunkFolder = new File("D:\\develop\\upload\\chunk\\");
        File originalFile = new File("D:\\develop\\upload\\1.mp4");
        File mergeFile = new File("d:\\develop\\upload\\2.mp4");
        if(mergeFile.exists()){
            mergeFile.delete();
        }
        mergeFile.createNewFile();
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
        byte[] b = new byte[1024];
        File[] files = chunkFolder.listFiles();
        List<File> fileList = Arrays.asList(files);
        fileList.sort((f1, f2)->Integer.parseInt(f1.getName())-Integer.parseInt(f2.getName()));
        for(File chunkFile: fileList){
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");
            int len = -1;
            while((len = raf_read.read(b))!=-1){
                raf_write.write(b, 0, len);
            }
            raf_read.close();
        }
        raf_write.close();
        FileInputStream fileInputStream = new FileInputStream(originalFile);
        FileInputStream inputStream = new FileInputStream(mergeFile);
        String oMd5 = DigestUtils.md5Hex(fileInputStream);
        String mMd5 = DigestUtils.md5Hex(inputStream);
        assert oMd5.equals(mMd5);
        fileInputStream.close();
        inputStream.close();
    }
}
