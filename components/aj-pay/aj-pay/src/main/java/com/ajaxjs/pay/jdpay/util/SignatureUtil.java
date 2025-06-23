package com.ajaxjs.pay.jdpay.util;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class SignatureUtil {
	public static void checkArguments(Map<String, String> merchantSignMap, List<String> signKeyList, String signData, String key) {
		if (merchantSignMap == null || merchantSignMap.isEmpty()) {
			throw new IllegalArgumentException("Argument 'merchantSignMap' is null or empty");
		}
		if (signKeyList == null || signKeyList.isEmpty()) {
			throw new IllegalArgumentException("Argument 'signKeyList' is null or empty");
		}
		if (signData == null || signData.isEmpty()) {
			throw new IllegalArgumentException("Argument 'signData' is null or empty");
		}
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Argument 'key' is null or empty");
		}
	}

	public static String generateSrcData(Map<String, String> merchantSignMap, List<String> signKeyList) {
		TreeMap<String, Object> signMap = new TreeMap<>();

		for (String str : signKeyList) {
			Object o = merchantSignMap.get(str);

			if (o != null) {
				signMap.put(str, o);
				continue;
			}

			signMap.put(str, "");
		}

		StringBuilder sb = new StringBuilder();

		for (Map.Entry<String, Object> stringObjectEntry : signMap.entrySet())
			sb.append(stringObjectEntry.getKey()).append("=").append((stringObjectEntry.getValue() == null) ? "" : stringObjectEntry.getValue()).append("&");

		String result = sb.toString();
		if (result.endsWith("&"))
			result = result.substring(0, result.length() - 1);

		return result;
	}
}
