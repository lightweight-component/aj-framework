package com.ajaxjs.message.sms;

import com.ajaxjs.message.email.Email;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SmsService extends Remote {
    boolean sendEmail(SmsMessage sms) throws RemoteException;
}
