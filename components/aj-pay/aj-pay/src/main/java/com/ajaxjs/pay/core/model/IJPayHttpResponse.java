
package com.ajaxjs.pay.core.model;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.CaseInsensitiveMap;
import com.ajaxjs.util.StrUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


@Data
public class IJPayHttpResponse implements Serializable {
	private static final long serialVersionUID = 6089103955998013402L;
	private String body;
	private byte[] bodyByte;
	private int status;
	private Map<String, List<String>> headers;

	public String getHeader(String name) {
		List<String> values = this.headerList(name);
		return CollectionUtil.isEmpty(values) ? null : values.get(0);
	}

	private List<String> headerList(String name) {
		if (StrUtil.isEmptyText(name)) {
			return null;
		} else {
			CaseInsensitiveMap<String, List<String>> headersIgnoreCase = new CaseInsensitiveMap<>(getHeaders());
			return headersIgnoreCase.get(name.trim());
		}
	}

	@Override
	public String toString() {
		return "IJPayHttpResponse{" +
			"body='" + body + '\'' +
			", status=" + status +
			", headers=" + headers +
			'}';
	}
}
