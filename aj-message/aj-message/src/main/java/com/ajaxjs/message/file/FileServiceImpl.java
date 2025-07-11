package com.ajaxjs.message.file;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

@Slf4j
public class FileServiceImpl extends UnicastRemoteObject implements FileService {
    // 构造函数，必须抛出 RemoteException
    public FileServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public byte[] downloadFile(String fileName) throws RemoteException, IOException {
        File file = new File(fileName);

        if (!file.exists())
            throw new IOException("File not found: " + fileName);

        byte[] fileData = null;

        // 读取文件内容并返回为字节数组
        try (FileInputStream fis = new FileInputStream(file);) {
            fileData = new byte[(int) file.length()];
            fis.read(fileData);
        }

        return fileData;
    }

    @Override
    public void uploadFile(String fileName, byte[] fileData) throws RemoteException, IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(fileData);
        }

        log.info("File uploaded successfully: " + fileName);
    }
}
