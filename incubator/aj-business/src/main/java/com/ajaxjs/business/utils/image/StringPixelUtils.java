package com.ajaxjs.business.utils.image;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取字符串的像素点阵并在命令行下打印输出
 * <a href="https://my.oschina.net/drinkjava2/blog/3111327">...</a>
 */
public class StringPixelUtils {
    private static final Map<String, byte[][]> lettersMap = new HashMap<>();

    public static byte[][] getSanserif10Pixels(String s) {
        return getStringPixels(Font.SANS_SERIF, Font.PLAIN, 10, s);
    }

    public static byte[][] getSanserif12Pixels(String s) {
        return getStringPixels(Font.SANS_SERIF, Font.PLAIN, 12, s);
    }

    public static byte[][] getSanserifItalic10Pixels(String s) {
        return getStringPixels(Font.SANS_SERIF, Font.ITALIC, 10, s);
    }

    /**
     * 在内存 BufferedImage里输出文本并获取它的像素点
     *
     * @param fontName  字体名
     * @param fontStyle 字体风格
     * @param fontSize  字体大小
     * @param s         要获取像素点的字符串
     * @return 字符串
     */
    public static byte[][] getStringPixels(String fontName, int fontStyle, int fontSize, String s) {
        String key = fontName + "_" + fontStyle + "_" + fontSize + "_" + s;

        if (lettersMap.containsKey(key))
            return lettersMap.get(key);

        Font font = new Font(fontName, fontStyle, fontSize);

        BufferedImage bi = new BufferedImage(fontSize * 10, fontSize * 50, BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.getGraphics();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int strHeight = fm.getAscent() + fm.getDescent() - 1;
        int strWidth = fm.stringWidth(s);
        g2d.drawString(s, 0, fm.getAscent() - fm.getLeading() - 1);

        int ystart;//在命令行和eclipse下会有不同的空行，所以要用ystart和yend来获取有效象素行数
        loop1:
        for (ystart = 0; ystart < strHeight; ystart++)
            for (int x = 0; x < strWidth; x++) {
                if (bi.getRGB(x, ystart) == -1) break loop1;
            }

        int yend;
        loop2:
        for (yend = strHeight; yend >= 0; yend--)
            for (int x = 0; x < strWidth; x++)
                if (bi.getRGB(x, yend) == -1) break loop2;

        byte[][] b = new byte[strWidth][yend - ystart + 1];

        for (int y = ystart; y <= yend; y++)
            for (int x = 0; x < strWidth; x++)
                if (bi.getRGB(x, y) == -1)
                    b[x][yend - y] = 1;
                else
                    b[x][yend - y] = 0;

        lettersMap.put(key, b);

        return b;
    }

    public static void main(String[] args) {
        byte[][] c = getStringPixels(Font.SANS_SERIF, Font.PLAIN, 12, "Test点阵输出");
        int w = c.length;
        int h = c[0].length;

        for (int y = 0; y < h; y++) {
            for (byte[] bytes : c) {
                if (bytes[h - y - 1] > 0)
                    System.out.print("*");
                else
                    System.out.print(" ");
            }

            System.out.println("|");
        }
    }
}