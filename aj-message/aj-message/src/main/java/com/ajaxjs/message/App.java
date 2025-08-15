package com.ajaxjs.message;

import com.ajaxjs.message.email.EmailRemoteService;
import com.ajaxjs.message.email.EmailServiceImpl;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class App {
    public static void main(String[] args) {
        ConfigReader.getConfigFromYml("application.yml");
        // 启动 RMI 注册表
        try {
            LocateRegistry.createRegistry(1099);

            // 创建远程服务对象并绑定到 RMI 注册表

            EmailRemoteService emailService = new EmailServiceImpl();
            Naming.rebind("EmailService", emailService);

            System.out.println("Server is running...");
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
