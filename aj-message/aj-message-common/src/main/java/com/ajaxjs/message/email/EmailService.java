package com.ajaxjs.message.email;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EmailService extends Remote {
    boolean sendEmail(Email email) throws RemoteException;
}
