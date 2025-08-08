package com.ajaxjs.business.net.ip;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * 获取 IP 地理位置
 * <a href="https://www.cnblogs.com/huanzi-qch/p/12979895.html">...</a>
 * <a href="http://whois.pconline.com.cn">...</a>
 */
public class IpUtils {
    /**
     * 调用太平洋网络IP地址查询Web接口（<a href="http://whois.pconline.com.cn/">...</a>），返回ip、地理位置
     */
    public static IpVo getIpVo(String ip) {
        String url = "http://whois.pconline.com.cn/ipJson.jsp?json=true"; // 查本机

        if (StringUtils.hasText(ip))  // 查指定ip
            url = "http://whois.pconline.com.cn/ipJson.jsp?json=true&ip=" + ip;

        StringBuilder inputLine = new StringBuilder();
        String read;

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestProperty("Charset", "GBK");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "GBK"))) {
                while ((read = in.readLine()) != null)
                    inputLine.append(read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //返回格式
        /*
        {
            ip: "58.63.47.115",
            pro: "广东省",
            proCode: "440000",
            city: "广州市",
            cityCode: "440100",
            region: "天河区",
            regionCode: "440106",
            addr: "广东省广州市天河区 电信",
            regionNames: "",
            err: ""
        }
         */

        IpVo ipVo = null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY); // 当属性的值为空（null或者""）时，不进行序列化，可以减少数据传输
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));// 设置日期格式

        try {
            // 转换成IpVo
            ipVo = mapper.readValue(new String(inputLine.toString().getBytes("GBK"), "GBK"), IpVo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ipVo;
    }

    /**
     * 直接根据访问者的Request，返回ip、地理位置
     */
//    public static IpVo getIpVoByRequest(HttpServletRequest request) {
//        return getIpVo(WebUtils.getClientIp(request));
//    }
}
