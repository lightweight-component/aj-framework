//package com.ajaxjs.message.sms.ali;
//
//
//import com.ajaxjs.framework.ISendSMS;
//import com.ajaxjs.util.logger.LogHelper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import java.util.Random;
//import java.util.regex.Pattern;
//
//@Component
//public class SendSMS implements ISendSMS {
//
//	@Autowired
//	private AliyunSmsEntity sendSMS;
//
//	@Override
//	public boolean send(String phone, String code) {
//		sendSMS.setTemplateParam(String.format("{\"code\":%s}", code));
//		sendSMS.setPhoneNumbers(phone);
//
//		String result = AliyunSMS.send(sendSMS);
//
//		if (result.contains("触发分钟级流控"))
//			throw new IllegalAccessError("发送短信过于频繁，请稍后再试。");
//
//		return "OK".equals(result);
//	}
//



//}
