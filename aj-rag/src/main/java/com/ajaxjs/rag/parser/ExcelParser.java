package com.ajaxjs.rag.parser;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ExcelParser extends BaseFileParser {
    /**
     * 解析Excel文件并返回其文本内容
     *
     * @param file Excel文件
     * @return 解析后的字符串
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public String parse(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {
            StringBuilder text = new StringBuilder();

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (Row row : sheet) {
                    for (Cell cell : row)
                        text.append(cell.getStringCellValue()).append("\t");

                    text.append("\n");
                }

                text.append("\n");
            }

            return text.toString();
        }
    }
}