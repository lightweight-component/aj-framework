package com.ajaxjs.rag.parser;

import org.apache.poi.xslf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PPTParser extends BaseFileParser {
    /**
     * 解析PowerPoint文件并返回其文本内容
     *
     * @param file PowerPoint文件
     * @return 解析后的字符串
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public String parse(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XMLSlideShow ppt = new XMLSlideShow(fis)) {
            StringBuilder text = new StringBuilder();

            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        text.append(textShape.getText()).append("\n");
                    }
                }

                text.append("\n");
            }

            return text.toString();
        }
    }
}