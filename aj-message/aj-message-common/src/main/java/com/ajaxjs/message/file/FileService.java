package com.ajaxjs.message.file;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileService extends Remote {
    /**
     * 远程方法，用于下载文件（以字节数组形式返回文件内容）
     * @param fileName
     * @return
     * @throws RemoteException
     * @throws IOException
     */
    byte[] downloadFile(String fileName) throws RemoteException, IOException;

    // 远程方法，用于上传文件（发送文件名和字节数组内容）

    /**
     * 远程方法，用于上传文件（发送文件名和字节数组内容）
     *
     * @param fileName
     * @param fileData
     * @throws RemoteException
     * @throws IOException
     */
    void uploadFile(String fileName, byte[] fileData) throws RemoteException, IOException;
}
