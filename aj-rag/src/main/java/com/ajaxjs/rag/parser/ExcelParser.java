package com.ajaxjs.rag.parser;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ExcelParser implements FileParser {

    /**
     * 解析Excel文件并返回其文本内容
     *
     * @param file Excel文件
     * @return 解析后的字符串
     * @throws IOException 如果发生I/O错误
     */
    public String parse(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {
            StringBuilder text = new StringBuilder();

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        text.append(cell.getStringCellValue()).append("\t");
                    }
                    text.append("\n");
                }
                text.append("\n");
            }

            return text.toString();
        }
    }

    /**
     * 根据文件路径解析Excel文件并返回其文本内容
     *
     * @param filePath Excel文件的路径
     * @return 解析后的字符串
     * @throws IOException 如果发生I/O错误
     */
    public String parse(String filePath) throws IOException {
        return parse(new File(filePath));
    }

    // 测试方法
    public static void main(String[] args) {
        try {
            ExcelParser excelParser = new ExcelParser();
            String content = excelParser.parse("path/to/your/excel_file.xlsx");
            System.out.println(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}