
package com.ajaxjs.pay.unionpay;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnionPayApiConfig implements Serializable {
	private static final long serialVersionUID = -9025648880068727445L;

	/**
	 * 商户平台分配的账号
	 */
	private String mchId;
	/**
	 * 连锁商户号
	 */
	private String groupMchId;
	/**
	 * 授权交易机构代码
	 */
	private String agentMchId;
	/**
	 * 商户平台分配的密钥
	 */
	private String apiKey;
	/**
	 * 商户平台网关
	 */
	private String serverUrl;
	/**
	 * 应用域名，回调中会使用此参数
	 */
	private String domain;
	/**
	 * 其他附加参数
	 */
	private Object exParams;
}
