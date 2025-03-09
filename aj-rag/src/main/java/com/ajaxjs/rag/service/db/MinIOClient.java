package com.ajaxjs.rag.service.db;

import io.minio.*;
import io.minio.errors.*;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MinIOClient {
    private static MinIOClient instance;
    private MinioClient minioClient;

    private MinIOClient(String endpoint, String accessKey, String secretKey) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public static synchronized MinIOClient getInstance(String endpoint, String accessKey, String secretKey) {
        if (instance == null) {
            instance = new MinIOClient(endpoint, accessKey, secretKey);
        }
        return instance;
    }

    public boolean uploadFile(String bucketName, String objectName, String filePath) {
        try {
            File file = new File(filePath);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(new FileInputStream(file), file.length(), -1)
                    .build());
            return true;
        } catch (Exception e) {
            System.out.println("Error uploading file: " + e.getMessage());
            return false;
        }
    }

    //    public boolean downloadFile(String bucketName, String objectName, String downloadPath) {
//        try {
//            minioClient.getObject(GetObjectArgs.builder()
//                    .bucket(bucketName)
//                    .object(objectName)
//                    .build());
//            return true;
//        } catch (Exception e) {
//            System.out.println("Error downloading file: " + e.getMessage());
//            return false;
//        }
//    }
    public boolean downloadFile(String bucketName, String objectName, String downloadPath) {
        try {
            // 创建目标文件的File对象
            File outputFile = new File(downloadPath + File.separator + objectName);
            // 创建输出流用于写入文件
            FileOutputStream outputStream = new FileOutputStream(outputFile);

            // 获取对象并读取数据
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            // 将数据从输入流复制到输出流
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // 关闭流
            outputStream.close();
            inputStream.close();

            return true;
        } catch (MinioException | IOException e) {
            System.out.println("Error downloading file: " + e.getMessage());
            return false;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        String minioEndpoint = "http://124.223.85.176:9000";
        String minioAccessKey = "ROOTNAME";
        String minioSecretKey = "CHANGEME123";

        MinIOClient minioClient = MinIOClient.getInstance(minioEndpoint, minioAccessKey, minioSecretKey);

        String file_path = "C:\\Users\\19664\\Desktop\\demo.png";
        String file_out_path = "C:\\Users\\19664\\Desktop\\tmp";
        String bucket_name = "documents";
        String object_name = "demo.png";

        // 上传文件
        boolean uploadSuccess = minioClient.uploadFile(bucket_name, object_name, file_path);
        if (uploadSuccess) {
            System.out.println("File uploaded successfully.");
        } else {
            System.out.println("Failed to upload file.");
        }

        // 下载文件
        boolean downloadSuccess = minioClient.downloadFile(bucket_name, object_name, file_out_path);
        if (downloadSuccess) {
            System.out.println("File downloaded successfully.");
        } else {
            System.out.println("Failed to download file.");
        }
    }
}