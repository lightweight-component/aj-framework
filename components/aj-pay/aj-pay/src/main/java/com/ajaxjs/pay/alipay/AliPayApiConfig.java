package com.ajaxjs.pay.alipay;

import com.ajaxjs.util.StrUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;

import java.io.Serializable;

@Data
public class AliPayApiConfig implements Serializable {
	private static final long serialVersionUID = -4736760736935998953L;

	/**
	 * 应用私钥
	 */
	private String privateKey;

	/**
	 * 支付宝公钥
	 */
	private String aliPayPublicKey;

	/**
	 * 应用编号
	 */
	private String appId;

	/**
	 * 支付宝支付网关
	 */
	private String serviceUrl;

	/**
	 * 字符集，为空默认为 UTF-8
	 */
	private String charset;

	/**
	 * 为空默认为 RSA2
	 */
	private String signType;

	/**
	 * 为空默认为 JSON
	 */
	private String format;

	/**
	 * 是否为证书模式
	 */
	private boolean certModel;

	/**
	 * 应用公钥证书 (证书模式必须)
	 */
	private String appCertPath;

	/**
	 * 应用公钥证书文本内容
	 */
	private String appCertContent;

	/**
	 * 支付宝公钥证书 (证书模式必须)
	 */
	private String aliPayCertPath;

	/**
	 * 支付宝公钥证书文本内容
	 */
	private String aliPayCertContent;

	/**
	 * 支付宝根证书 (证书模式必须)
	 */
	private String aliPayRootCertPath;

	/**
	 * 支付宝根证书文本内容
	 */
	private String aliPayRootCertContent;

	/**
	 * 支付宝客户端
	 */
	private AlipayClient alipayClient;

	/**
	 * 其他附加参数
	 */
	private Object exParams;

	/**
	 * 域名
	 */
	private String domain;

	private AliPayApiConfig() {
	}

	public static AliPayApiConfig builder() {
		return new AliPayApiConfig();
	}

	/**
	 * 直接传入阿里客户端方式
	 *
	 * @param defaultAlipayClient 默认阿里客户端
	 * @return {@link AliPayApiConfig}  支付宝支付配置
	 */
	public AliPayApiConfig build(DefaultAlipayClient defaultAlipayClient) {
		this.alipayClient = defaultAlipayClient;
		return this;
	}

	/**
	 * 普通公钥方式
	 *
	 * @return AliPayApiConfig 支付宝配置
	 */
	public AliPayApiConfig build() {
		this.alipayClient = new DefaultAlipayClient(getServiceUrl(), getAppId(), getPrivateKey(), getFormat(),
			getCharset(), getAliPayPublicKey(), getSignType());
		return this;
	}

	/**
	 * 证书模式
	 *
	 * @return AliPayApiConfig 支付宝配置
	 * @throws AlipayApiException 支付宝 Api 异常
	 */
	public AliPayApiConfig buildByCert() throws AlipayApiException {
		return build(getAppCertPath(), getAliPayCertPath(), getAliPayRootCertPath());
	}

	/**
	 * 证书模式
	 *
	 * @return AliPayApiConfig 支付宝配置
	 * @throws AlipayApiException 支付宝 Api 异常
	 */
	public AliPayApiConfig buildByCertContent() throws AlipayApiException {
		return buildByCertContent(getAppCertContent(), getAliPayCertContent(), getAliPayRootCertContent());
	}

	/**
	 * @param appCertPath        应用公钥证书路径
	 * @param aliPayCertPath     支付宝公钥证书文件路径
	 * @param aliPayRootCertPath 支付宝CA根证书文件路径
	 * @return {@link AliPayApiConfig}  支付宝支付配置
	 * @throws AlipayApiException 支付宝 Api 异常
	 */
	public AliPayApiConfig build(String appCertPath, String aliPayCertPath, String aliPayRootCertPath) throws AlipayApiException {
		CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
		certAlipayRequest.setServerUrl(getServiceUrl());
		certAlipayRequest.setAppId(getAppId());
		certAlipayRequest.setPrivateKey(getPrivateKey());
		certAlipayRequest.setFormat(getFormat());
		certAlipayRequest.setCharset(getCharset());
		certAlipayRequest.setSignType(getSignType());
		certAlipayRequest.setCertPath(appCertPath);
		certAlipayRequest.setAlipayPublicCertPath(aliPayCertPath);
		certAlipayRequest.setRootCertPath(aliPayRootCertPath);
		this.alipayClient = new DefaultAlipayClient(certAlipayRequest);
		this.certModel = true;

		return this;
	}

	/**
	 * @param appCertContent        应用公钥证书
	 * @param aliPayCertContent     支付宝公钥证书
	 * @param aliPayRootCertContent 支付宝CA根证书
	 * @return {@link AliPayApiConfig}  支付宝支付配置
	 * @throws AlipayApiException 支付宝 Api 异常
	 */
	public AliPayApiConfig buildByCertContent(String appCertContent, String aliPayCertContent, String aliPayRootCertContent) throws AlipayApiException {
		CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
		certAlipayRequest.setServerUrl(getServiceUrl());
		certAlipayRequest.setAppId(getAppId());
		certAlipayRequest.setPrivateKey(getPrivateKey());
		certAlipayRequest.setFormat(getFormat());
		certAlipayRequest.setCharset(getCharset());
		certAlipayRequest.setSignType(getSignType());
		certAlipayRequest.setCertContent(appCertContent);
		certAlipayRequest.setAlipayPublicCertContent(aliPayCertContent);
		certAlipayRequest.setRootCertContent(aliPayRootCertContent);
		this.alipayClient = new DefaultAlipayClient(certAlipayRequest);
		this.certModel = true;

		return this;
	}

	public String getPrivateKey() {
		if (StrUtil.isEmptyText(privateKey))
			throw new IllegalStateException("privateKey 未被赋值");

		return privateKey;
	}

	public AliPayApiConfig setPrivateKey(String privateKey) {
		if (StrUtil.isEmptyText(privateKey)) {
			throw new IllegalArgumentException("privateKey 值不能为 null");
		}
		this.privateKey = privateKey;
		return this;
	}

	public AliPayApiConfig setAliPayPublicKey(String aliPayPublicKey) {
		this.aliPayPublicKey = aliPayPublicKey;
		return this;
	}

	public String getAppId() {
		if (StrUtil.isEmptyText(appId)) {
			throw new IllegalStateException("appId 未被赋值");
		}
		return appId;
	}

	public AliPayApiConfig setAppId(String appId) {
		if (StrUtil.isEmptyText(appId)) {
			throw new IllegalArgumentException("appId 值不能为 null");
		}
		this.appId = appId;
		return this;
	}

	public String getServiceUrl() {
		if (StrUtil.isEmptyText(serviceUrl)) {
			throw new IllegalStateException("serviceUrl 未被赋值");
		}
		return serviceUrl;
	}

	public AliPayApiConfig setServiceUrl(String serviceUrl) {
		if (StrUtil.isEmptyText(serviceUrl)) {
			serviceUrl = "https://openapi.alipay.com/gateway.do";
		}
		this.serviceUrl = serviceUrl;
		return this;
	}

	public String getCharset() {
		if (StrUtil.isEmptyText(charset)) {
			charset = "UTF-8";
		}
		return charset;
	}

	public AliPayApiConfig setCharset(String charset) {
		if (StrUtil.isEmptyText(charset)) {
			charset = "UTF-8";
		}
		this.charset = charset;
		return this;
	}

	public String getSignType() {
		if (StrUtil.isEmptyText(signType)) {
			signType = "RSA2";
		}
		return signType;
	}

	public AliPayApiConfig setSignType(String signType) {
		if (StrUtil.isEmptyText(signType)) {
			signType = "RSA2";
		}
		this.signType = signType;
		return this;
	}

	public String getFormat() {
		if (StrUtil.isEmptyText(format)) {
			format = "json";
		}
		return format;
	}


	public AliPayApiConfig setAppCertPath(String appCertPath) {
		this.appCertPath = appCertPath;
		return this;
	}


	public AliPayApiConfig setAppCertContent(String appCertContent) {
		this.appCertContent = appCertContent;
		return this;
	}



	public AliPayApiConfig setAliPayCertPath(String aliPayCertPath) {
		this.aliPayCertPath = aliPayCertPath;
		return this;
	}


	public AliPayApiConfig setAliPayCertContent(String aliPayCertContent) {
		this.aliPayCertContent = aliPayCertContent;
		return this;
	}


	public AliPayApiConfig setAliPayRootCertPath(String aliPayRootCertPath) {
		this.aliPayRootCertPath = aliPayRootCertPath;
		return this;
	}


	public AliPayApiConfig setAliPayRootCertContent(String aliPayRootCertContent) {
		this.aliPayRootCertContent = aliPayRootCertContent;
		return this;
	}



	public AliPayApiConfig setCertModel(boolean certModel) {
		this.certModel = certModel;
		return this;
	}

	public AlipayClient getAliPayClient() {
		if (alipayClient == null) {
			throw new IllegalStateException("aliPayClient 未被初始化");
		}
		return alipayClient;
	}



	public AliPayApiConfig setExParams(Object exParams) {
		this.exParams = exParams;
		return this;
	}



	public AliPayApiConfig setDomain(String domain) {
		this.domain = domain;
		return this;
	}
}
