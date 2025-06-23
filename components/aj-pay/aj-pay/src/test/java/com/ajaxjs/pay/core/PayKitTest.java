package com.ajaxjs.pay.core;

import com.ajaxjs.pay.core.kit.PayKit;
import com.ajaxjs.pay.core.model.CertificateModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class PayKitTest {
	@Test
	public void checkCertificateIsValid() {
		CertificateModel model = new CertificateModel();
		model.setNotAfter(new Date());
		boolean isValid = PayKit.checkCertificateIsValid(model, "", -1);
		Assertions.assertFalse(isValid);
	}

	@Test
	public void checkCertificateIsValidByPath() {
		String path = "";
		boolean isValid = PayKit.checkCertificateIsValid(path, "", -2);
		Assertions.assertFalse(isValid);
	}
}
