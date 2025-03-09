package com.ajaxjs.rag.parser;

import java.io.IOException;

public class FileParserFactory {

    public static FileParser getFileParser(String fileType) {
        switch (fileType.toLowerCase()) {
            case "html":
                return new HTMLParser();
            case "pdf":
                return new PDFParser();
            case "txt":
            case "md":
            case "py":
            case "java":
                return new PureTextParser();
            case "doc":
            case "docx":
                return new WordParser();
            case "ppt":
            case "pptx":
                return new PPTParser();
            case "xls":
            case "xlsx":
                return new ExcelParser();
            default:
                throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }
    }

    public static FileParser getFileParserByOriginalName(String originalFileName) {
        int lastIndex = originalFileName.lastIndexOf('.');
        if (lastIndex == -1) {
            throw new IllegalArgumentException("Invalid original file name: " + originalFileName);
        }
        String fileType = originalFileName.substring(lastIndex + 1);
        return getFileParser(fileType);
    }

    public static String easyParse(String filePath, String originalFileName) {
        FileParser parser = FileParserFactory.getFileParserByOriginalName(originalFileName);
        String content;

        try {
            content = parser.parse(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return content;
    }

    public static String easyParse(String filePath) {
        FileParser parser = FileParserFactory.getFileParser(filePath);
        String content = null;
        try {
            content = parser.parse(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content;
    }

    public static void main(String[] args) {
        try {
            // 解析PDF文件
            testParseFile("C:\\Users\\19664\\Desktop\\2311.12351v2.pdf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void testParseFile(String filePath) throws IOException {
//        FileParser parser = FileParserFactory.getFileParserByPath(filePath);
//        String content = parser.parse(filePath);
//        System.out.println("Content of " + filePath + ":\n" + content);
//        System.out.println("----------------------------------------\n");
    }
}