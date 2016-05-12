package com.ajaxjs.util.websecurity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.Part;


import com.ajaxjs.core.Util;
import com.ajaxjs.util.websecurity.util.XssUtil;
import com.ajaxjs.util.websecurity.util.XssUtil.XssFilterTypeEnum;

public class SecurityHttpServletRequest extends HttpServletRequestWrapper {

	public SecurityHttpServletRequest(HttpServletRequest request) {
		super(request);
	}

	@Override
	public String getParameter(String name) {
		return XssUtil.xssFilter(super.getParameter(XssUtil.xssFilter(name, XssFilterTypeEnum.DELETE.getValue())), null);
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> paramsMap = super.getParameterMap();
		if (!Util.isNotNull(paramsMap)) return paramsMap;

		Map<String, String[]> resParamsMap = new HashMap<>();
		Iterator<Entry<String, String[]>> iter = paramsMap.entrySet().iterator();
		
		while (iter.hasNext()) {
			Entry<String, String[]> entry = iter.next();
			resParamsMap.put( (XssUtil.xssFilter(entry.getKey(), XssFilterTypeEnum.DELETE.getValue())), filterList(entry.getValue()));
		}
		
		return resParamsMap;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		Enumeration<String> enums = super.getParameterNames();
		Vector<String> vec = new Vector<>();
		
		while (enums.hasMoreElements()) {
			String value = enums.nextElement();
			vec.add(XssUtil.xssFilter(value, null));
		}
		
		return vec.elements();
	}

	@Override
	public String[] getParameterValues(String name) {
		return filterList(super.getParameterValues(name));
	}

	private String[] filterList(String[] value) {
		if (!Util.isNotNull(value)) return value;
		
		List<String> resValueList = new ArrayList<>();
		
		for (String val : value) 
			resValueList.add(XssUtil.xssFilter(val, null));
		
		return resValueList.toArray(new String[resValueList.size()]);
	}

	/**
	 * 文件上传安全过滤
	 */
	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		Collection<Part> parts = super.getParts();
		
		if (parts == null || parts.isEmpty()
				|| SecurityConstant.whitefilePostFixList == null
				|| SecurityConstant.whitefilePostFixList.isEmpty()) {
			return parts;
		}
		
		List<Part> resParts = new ArrayList<>();
		for (Part part : parts) {
			for (String extension : SecurityConstant.whitefilePostFixList) {
				if (part.getName().toUpperCase().endsWith(extension)) 
					resParts.add(part);
				
			}
		}
		
		return resParts;
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		Part part = super.getPart(name);
		if (SecurityConstant.whitefilePostFixList == null
				|| SecurityConstant.whitefilePostFixList.isEmpty()) {
			return part;
		}
		String value = part.getHeader("content-disposition");
		String filename = value.substring(value.lastIndexOf("=") + 2, value.length() - 1);
		for (String extension : SecurityConstant.whitefilePostFixList) {
			if (filename.toUpperCase().endsWith(extension.toUpperCase()))
				return part;
		}
		
		return null;
	}

}
