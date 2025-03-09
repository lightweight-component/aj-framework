package com.ajaxjs.rag.parser;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestParser {
    @Test
    public void testExcel() throws IOException {
        ExcelParser excelParser = new ExcelParser();
        String content = excelParser.parse("path/to/your/excel_file.xlsx");
        System.out.println(content);
    }

    @Test
    public void testPpt() throws IOException {
        PDFParser parser = new PDFParser();
        String content = parser.parse("path/to/your/ppt_file.pptx");
        System.out.println(content);
    }

    @Test
    public void testPdf() throws IOException {
        // 解析PDF文件
        String filePath = "C:\\Users\\19664\\Desktop\\2311.12351v2.pdf";

        PDFParser parser = new PDFParser();
        String content = parser.parse("C:\\Users\\19664\\Desktop\\2311.12351v2.pdf");
        System.out.println(content);

//        FileParser parser = FileParserFactory.getFileParserByPath(filePath);
//        String content = parser.parse(filePath);
//        System.out.println("Content of " + filePath + ":\n" + content);
//        System.out.println("----------------------------------------\n");
    }

    @Test
    public void testHtml() throws IOException {
        HTMLParser htmlParser = new HTMLParser();

        // 解析本地HTML文件
        String localContent = htmlParser.parse("path/to/your/html_file.html");
        System.out.println(localContent);

        // 解析在线HTML页面
        String onlineContent = HTMLParser.parseHTMLFromURL("https://example.com");
        System.out.println(onlineContent);
    }
}
