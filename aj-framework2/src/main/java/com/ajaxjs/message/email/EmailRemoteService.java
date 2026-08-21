package com.ajaxjs.message.email;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EmailRemoteService extends Remote {
    boolean sendEmail(Email email) throws RemoteException;
}
