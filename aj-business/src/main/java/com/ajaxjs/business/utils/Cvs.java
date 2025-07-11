package com.ajaxjs.business.utils;


import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 解决导出CSV文件乱码的问题
 * <a href="https://www.kuangstudy.com/bbs/1354629238308761601">...</a>
 * 之前处理bug，在解决这个导出csv格式文件乱码的问题，几经周折才处理好，记录一下；
 * 主要问题点是：微软的excel文件需要通过文件头的bom来识别编码，所以写文件时，需要先写入bom头。
 */
public class Cvs {
    public static void exportList(String[] headers, String[] columns, List dto, String sheetName, HttpServletResponse response) throws Exception {
        List header = Arrays.asList(headers);
        List fids = Arrays.asList(columns);
        List list = genDtoExportData(dto, fids);
        File file = File.createTempFile("export", ".csv");
        StringBuilder sb = new StringBuilder();

        for (String h : headers)
            sb.append(transfer(h)).append(",");

        sb.append("\r\n");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        assert list != null;
        for (Object obj : list) {
            for (Object obj2 : (List) obj) {
                if (obj2 instanceof Date) {
                    sb.append(sdf.format((Date) obj2)).append(",");
                } else {
                    sb.append(transfer(obj2.toString())).append(",");
                }
            }
            sb.append("\r\n");
        }

        try (OutputStream out = Files.newOutputStream(file.toPath()); OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);) {
            writer.write(new String(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}));
            writer.append(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.setContentType("text/csv; charset=\"utf-8\"");
//        response.setHeader("Content-disposition", "attachment; filename=" + transferFilename(sheetName) + ".csv");
        byte[] buffer = new byte[1024];

        try (FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis); OutputStream os = response.getOutputStream()) {
            int i = bis.read(buffer);

            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            file.delete();
        }
    }

    private static List genDtoExportData(List dtos, List fids) {
        return null;
    }

    /**
     * 处理双引号和逗号的特殊转译
     */
    public static String transfer(String str) {
        String tempDescription = str;

        // 如果有逗号
        if (str.contains(",")) {
            // 如果还有双引号，先将双引号转义，避免两边加了双引号后转义错误
            if (str.contains("\"")) tempDescription = str.replace("\"", "\"\"");

            // 在将逗号转义
            tempDescription = "\"" + tempDescription + "\"";
        }

        return tempDescription;
    }
}
