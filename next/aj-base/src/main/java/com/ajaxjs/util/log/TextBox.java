package com.ajaxjs.util.log;

import com.ajaxjs.util.CommonConstant;
import com.ajaxjs.util.ObjectHelper;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Make a box for text only
 */
@Data
public class TextBox {
    /**
     * Default width of the log box
     */
    private int boxWidth = 137;

    private StringBuffer sb = new StringBuffer();

    private String boxColor = ANSI_YELLOW;

    /**
     * 是否允许长文本自动换行，true 表示换行，false 表示截断
     */
    private boolean wrapLongLines = true;

    public TextBox boxStart(String title) {
        sb.append("\n").append(boxColor);
        sb.append(boxLine('┌', '─', '┐', title)).append('\n');

        return this;
    }

    public TextBox line(String key, Object value) {
        String _value;

        if (value instanceof String)
            _value = (String) value;
        else if (value instanceof Object[]) {
            Object[] arr = (Object[]) value;

            if (ObjectHelper.isEmpty(arr))
                _value = NONE;
            else
                _value = Arrays.toString(arr);
        } else if (value == null)
            _value = NONE;
        else
            _value = value.toString();

        sb.append(boxContent(key, _value)).append('\n');

        return this;
    }

    public String boxEnd() {
        sb.append(boxLine('└', '─', '┘', CommonConstant.EMPTY_STRING)).append(ANSI_RESET);

        return sb.toString();
    }

    /**
     * Creates a box borderline with a centered title
     *
     * @param left  the left border character
     * @param fill  the fill character for the border
     * @param right the right border character
     * @param title the title text to a center in the border
     * @return a formatted border line string
     */
    private String boxLine(char left, char fill, char right, String title) {
        int fillLen = boxWidth - 2 - title.length();
        int leftFill = fillLen / 2;
        int rightFill = fillLen - leftFill;

        return left + repeat(fill, leftFill) + title + repeat(fill, rightFill) + right;
    }

    /**
     * Repeats a character a specified number of times
     *
     * @param c the character to repeat
     * @param n the number of times to repeat
     * @return a string with the character repeated n times
     */
    private static String repeat(char c, int n) {
        if (n <= 0)
            return CommonConstant.EMPTY_STRING;

        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++)
            sb.append(c);

        return sb.toString();
    }

    /**
     * Creates a box content line with a key-value pair
     *
     * @param key   the key text to display
     * @param value the value text to display, will be truncated if too long
     * @return a formatted content line string
     */
    private String boxContent(String key, String value) {
        int maxLen = boxWidth - 2 - key.length();

        if (wrapLongLines) {
            // 换行模式：将长文本按最大宽度分割成多行显示
            List<String> lines = wrap(value, maxLen - 3);

            if (lines.size() == 1) {
                String val = truncate(value, maxLen);
                int pad = boxWidth - 2 - key.length() - getDisplayWidth(val);

                return "│ " + key + val + repeat(' ', pad - 1) + "│";
            } else {
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < lines.size(); i++) {
                    String prefix = (i == 0 ? key : repeat(' ', key.length()));
                    int pad = boxWidth - 2 - prefix.length() - getDisplayWidth(lines.get(i));
                    sb.append("│ ").append(prefix).append(lines.get(i)).append(repeat(' ', pad - 1)).append("│").append('\n');
                }

                sb.deleteCharAt(sb.length() - 1);

                return sb.toString();
            }
        } else {
            String val = truncate(value, maxLen);
            int pad = boxWidth - 2 - key.length() - getDisplayWidth(val);

            return "│ " + key + val + repeat(' ', pad - 1) + "│";
        }
    }

    /**
     * Calculates the display width of a string, accounting for wide characters
     * <p>
     * Wide characters (like CJK characters) count as two units, normal characters count as 1
     *
     * @param s the string to measure
     * @return the display width of the string
     */
    private static int getDisplayWidth(String s) {
        if (s == null)
            return 0;

        int width = 0;
        for (char c : s.toCharArray())
            width += isWideChar(c) ? 2 : 1;

        return width;
    }

    /**
     * Determines if a character is a wide character (takes double width in display)
     * <p>
     * Simplified implementation that checks for CJK characters, full-width characters,
     * and CJK symbols
     *
     * @param ch the character to check
     * @return true if the character is a wide character, false otherwise
     */
    private static boolean isWideChar(char ch) {
        return (ch >= 0x4E00 && ch <= 0x9FA5) || // CJK
                (ch >= 0xFF01 && ch <= 0xFF60) || // Full Width
                (ch >= 0x3000 && ch <= 0x303F);   // CJK Symbols
    }

    /**
     * Truncates a string to fit within a maximum display width, adding ellipsis if truncated
     * <p>
     * Properly handles wide characters (like CJK characters) which take double width in display
     *
     * @param s             the string to truncate
     * @param maxDisplayLen the maximum display width allowed
     * @return the truncated string with ellipsis if needed
     */
    private static String truncate(String s, int maxDisplayLen) {
        if (s == null)
            return CommonConstant.EMPTY_STRING;

        int dLen = getDisplayWidth(s);

        if (dLen <= maxDisplayLen)
            return s;

        String ellipsis = "...";
        int keep = maxDisplayLen - ellipsis.length();

        if (keep <= 0)
            return ellipsis;

        int count = 0, i = 0;

        for (; i < s.length() && count < keep; i++) {
            char ch = s.charAt(i);
            count += isWideChar(ch) ? 2 : 1;

            if (count > keep)
                break;
        }

        return s.substring(0, i - 1) + ellipsis;
    }

    /**
     * 按最大宽度自动换行，返回每行内容
     *
     * @param s             待换行的字符串
     * @param maxDisplayLen 最大显示宽度（按字符显示宽度计算，中文等宽字符占 2 个单位）
     * @return 换行后的字符串列表，每个元素为一行的内容
     */
    private static List<String> wrap(String s, int maxDisplayLen) {
        List<String> result = new ArrayList<>();

        if (s == null) {
            result.add(CommonConstant.EMPTY_STRING);
            return result;
        }

        StringBuilder line = new StringBuilder();
        int width = 0;

        for (int i = 0; i < s.length(); i++) {// 遍历字符串的每个字符，根据字符宽度进行换行处理
            char ch = s.charAt(i);
            int w = isWideChar(ch) ? 2 : 1;

            if (width + w > maxDisplayLen) { // 当前行宽度超过限制时，将已积累的内容添加到结果集并重置
                result.add(line.toString());
                line.setLength(0);
                width = 0;
            }

            line.append(ch);
            width += w;
        }

        if (line.length() > 0) // 添加最后一行内容（如果有）
            result.add(line.toString());

        if (result.isEmpty())  // 确保结果不为空
            result.add(CommonConstant.EMPTY_STRING);

        return result;
    }

    /**
     * Constant representing no color or style
     */
    public static final String NONE = "none";

    /**
     * ANSI escape code for green text
     */
    public static final String ANSI_GREEN = "\u001B[32m";

    /**
     * ANSI escape code for yellow text
     */
    public static final String ANSI_YELLOW = "\u001B[33m";

    /**
     * ANSI escape code for blue text
     */
    public static final String ANSI_BLUE = "\u001B[34m";

    /**
     * ANSI escape code for red text
     */
    public static final String ANSI_RED = "\u001B[31m";

    /**
     * ANSI escape code to reset text formatting
     */
    public static final String ANSI_RESET = "\u001B[0m";
}
