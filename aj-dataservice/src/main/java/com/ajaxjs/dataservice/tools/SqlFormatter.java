package com.ajaxjs.dataservice.tools;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * 格式化 SQL
 */
public class SqlFormatter {
    /**
     * 会开始一个复合 SQL 子句的关键字。
     */
    private static final Set<String> BEGIN_CLAUSES = new HashSet<>();

    /**
     * 会结束当前 SQL 子句的关键字。
     */
    private static final Set<String> END_CLAUSES = new HashSet<>();

    /**
     * SQL 逻辑关键字集合。
     */
    private static final Set<String> LOGICAL = new HashSet<>();

    /**
     * SQL 量词关键字集合。
     */
    private static final Set<String> QUANTIFIERS = new HashSet<>();

    /**
     * 数据操纵语句关键字集合。
     */
    private static final Set<String> DML = new HashSet<>();

    /**
     * 其他需要特别处理的 SQL 关键字集合。
     */
    private static final Set<String> MISC = new HashSet<>();

    /**
     * 单级缩进使用的字符串。
     */
    static final String indentString = "    ";

    /**
     * 格式化结果的初始换行和缩进。
     */
    static final String initial = "\n    ";

    /**
     * 按关键字和括号层级格式化 SQL 文本。
     *
     * @param source 原始 SQL 文本
     * @return 格式化后的 SQL 文本
     */
    public String format(String source) {
        return new FormatProcess(source).perform().trim();
    }

    static {
        BEGIN_CLAUSES.add("left");
        BEGIN_CLAUSES.add("right");
        BEGIN_CLAUSES.add("inner");
        BEGIN_CLAUSES.add("outer");
        BEGIN_CLAUSES.add("group");
        BEGIN_CLAUSES.add("order");

        END_CLAUSES.add("where");
        END_CLAUSES.add("set");
        END_CLAUSES.add("having");
//		END_CLAUSES.add("join");
        END_CLAUSES.add("from");
        END_CLAUSES.add("by");
        END_CLAUSES.add("join");
        END_CLAUSES.add("into");
        END_CLAUSES.add("union");

        LOGICAL.add("and");
        LOGICAL.add("or");
        LOGICAL.add("when");
        LOGICAL.add("else");
        LOGICAL.add("end");

        QUANTIFIERS.add("in");
        QUANTIFIERS.add("all");
        QUANTIFIERS.add("exists");
        QUANTIFIERS.add("some");
        QUANTIFIERS.add("any");

        DML.add("insert");
        DML.add("update");
        DML.add("delete");

        MISC.add("select");
        MISC.add("on");
    }

    /**
     * 保存一次 SQL 格式化过程中的词法状态和输出内容。
     */
    private static class FormatProcess {
        /** Indicates whether the next token begins a line. */
        boolean beginLine = true;

        /** Indicates that a begin clause has not yet reached its end clause. */
        boolean afterBeginBeforeEnd = false;

        /** Indicates that comma-separated items are being processed. */
        boolean afterByOrSetOrFromOrSelect = false;

        @SuppressWarnings("unused")
        /** Indicates that the current token follows a VALUES clause. */
        boolean afterValues = false;

        /** Indicates that the formatter is processing an ON clause. */
        boolean afterOn = false;

        /** Indicates that the formatter is processing a BETWEEN expression. */
        boolean afterBetween = false;

        /** Indicates that the formatter is processing an INSERT statement. */
        boolean afterInsert = false;

        /** Current nesting depth inside SQL function calls. */
        int inFunction = 0;

        /** Parenthesis depth since the latest SELECT clause. */
        int parensSinceSelect = 0;

        /** Stack of parenthesis depths for nested SELECT clauses. */
        private final LinkedList<Integer> parenCounts = new LinkedList<>();

        /** Stack of comma-formatting states for nested SELECT clauses. */
        private final LinkedList<Boolean> afterByOrFromOrSelects = new LinkedList<>();

        /** Current output indentation level. */
        int indent = 1;

        /** Accumulates the formatted SQL text. */
        StringBuffer result = new StringBuffer();

        /** Token stream of the source SQL. */
        StringTokenizer tokens;

        /** Previously processed non-whitespace token. */
        String lastToken;

        /** Current source token. */
        String token;

        /** Lowercase representation of the current token. */
        String lcToken;

        /**
         * Creates a formatter state for the supplied SQL.
         *
         * @param sql SQL text to tokenize
         */
        public FormatProcess(String sql) {
            tokens = new StringTokenizer(sql, "()+*/-=<>'`\"[], \n\r\f\t", true);
        }

        /**
         * Processes the token stream and returns formatted SQL text.
         *
         * @return formatted SQL text
         */
        public String perform() {
            result.append("\n    ");

            while (tokens.hasMoreTokens()) {
                token = tokens.nextToken();
                lcToken = token.toLowerCase();

                if ("'".equals(token)) {
                    String t;
                    do {
                        t = tokens.nextToken();
                        token += t;
                    } while ((!"'".equals(t)) && (tokens.hasMoreTokens()));
                } else if ("\"".equals(token)) {
                    String t;

                    do {
                        t = tokens.nextToken();
                        token += t;
                    } while (!"\"".equals(t));
                }

                if ((afterByOrSetOrFromOrSelect) && (",".equals(token))) {
                    commaAfterByOrFromOrSelect();
                } else if ((afterOn) && (",".equals(token))) {
                    commaAfterOn();
                } else if ("(".equals(token)) {
                    openParen();
                } else if (")".equals(token)) {
                    closeParen();
                } else if (SqlFormatter.BEGIN_CLAUSES.contains(lcToken)) {
                    beginNewClause();
                } else if (SqlFormatter.END_CLAUSES.contains(lcToken)) {
                    endNewClause();
                } else if ("select".equals(lcToken)) {
                    select();
                } else if (SqlFormatter.DML.contains(lcToken)) {
                    updateOrInsertOrDelete();
                } else if ("values".equals(lcToken)) {
                    values();
                } else if ("on".equals(lcToken)) {
                    on();
                } else if ((afterBetween) && (lcToken.equals("and"))) {
                    misc();
                    afterBetween = false;
                } else if (SqlFormatter.LOGICAL.contains(lcToken)) {
                    logical();
                } else if (isWhitespace(token)) {
                    white();
                } else {
                    misc();
                }

                if (!isWhitespace(token)) lastToken = lcToken;
            }

            return result.toString();
        }

        /** Handles a comma following an ON clause. */
        private void commaAfterOn() {
            out();
            indent -= 1;
            newline();
            afterOn = false;
            afterByOrSetOrFromOrSelect = true;
        }

        /** Handles a comma in BY, FROM or SELECT item lists. */
        private void commaAfterByOrFromOrSelect() {
            out();
            newline();
        }

        /** Formats a logical keyword. */
        private void logical() {
            if ("end".equals(lcToken)) indent -= 1;

            newline();
            out();
            beginLine = false;
        }

        /** Starts formatting an ON clause. */
        private void on() {
            indent += 1;
            afterOn = true;
            newline();
            out();
            beginLine = false;
        }

        /** Formats a regular non-whitespace token. */
        private void misc() {
            out();
            if ("between".equals(lcToken)) afterBetween = true;

            if (afterInsert) {
                newline();
                afterInsert = false;
            } else {
                beginLine = false;

                if ("case".equals(lcToken)) indent += 1;
            }
        }

        /** Emits whitespace when the current output line already has content. */
        private void white() {
            if (!beginLine) result.append(" ");
        }

        /** Formats an UPDATE, INSERT or DELETE statement keyword. */
        private void updateOrInsertOrDelete() {
            out();
            indent += 1;
            beginLine = false;

            if ("update".equals(lcToken)) newline();

            if ("insert".equals(lcToken)) afterInsert = true;
        }

        /** Starts a SELECT clause and saves nested formatting state. */
        private void select() {
            out();
            indent += 1;
            newline();
            parenCounts.addLast(parensSinceSelect);
            afterByOrFromOrSelects.addLast(afterByOrSetOrFromOrSelect);
            parensSinceSelect = 0;
            afterByOrSetOrFromOrSelect = true;
        }

        /** Appends the current token to the formatted output. */
        private void out() {
            result.append(token);
        }

        /** Formats a keyword that starts a new terminating clause. */
        private void endNewClause() {
            if (!afterBeginBeforeEnd) {
                indent -= 1;

                if (afterOn) {
                    indent -= 1;
                    afterOn = false;
                }

                newline();
            }
            out();
            if (!"union".equals(lcToken))
                indent += 1;

            newline();
            afterBeginBeforeEnd = false;
            afterByOrSetOrFromOrSelect = (("by".equals(lcToken)) || ("set".equals(lcToken)) || ("from".equals(lcToken)));
        }

        /** Formats a keyword that starts a composite clause. */
        private void beginNewClause() {
            if (!afterBeginBeforeEnd) {
                if (afterOn) {
                    indent -= 1;
                    afterOn = false;
                }

                indent -= 1;
                newline();
            }

            out();
            beginLine = false;
            afterBeginBeforeEnd = true;
        }

        /** Formats a VALUES clause. */
        private void values() {
            indent -= 1;
            newline();
            out();
            indent += 1;
            newline();
            afterValues = true;
        }

        /** Formats a closing parenthesis and restores nesting state. */
        private void closeParen() {
            parensSinceSelect -= 1;
            if (parensSinceSelect < 0) {
                indent -= 1;
                parensSinceSelect = parenCounts.removeLast();
                afterByOrSetOrFromOrSelect = afterByOrFromOrSelects.removeLast();
            }

            if (inFunction > 0) {
                inFunction -= 1;
                out();
            } else {
                if (!afterByOrSetOrFromOrSelect) {
                    indent -= 1;
                    newline();
                }

                out();
            }

            beginLine = false;
        }

        /** Formats an opening parenthesis and updates nesting state. */
        private void openParen() {
            if ((isFunctionName(lastToken)) || (inFunction > 0)) inFunction += 1;

            beginLine = false;

            if (inFunction > 0)
                out();
            else {
                out();
                if (!afterByOrSetOrFromOrSelect) {
                    indent += 1;
                    newline();
                    beginLine = true;
                }
            }

            parensSinceSelect += 1;
        }

        /**
         * Determines whether a token can be treated as a SQL function name.
         *
         * @param token token preceding an opening parenthesis
         * @return {@code true} when the token represents a function name
         */
        private static boolean isFunctionName(String token) {
            char begin = token.charAt(0);
            boolean isIdentifier = (Character.isJavaIdentifierStart(begin)) || ('"' == begin);

            return (isIdentifier) && (!SqlFormatter.LOGICAL.contains(token)) && (!SqlFormatter.END_CLAUSES.contains(token))
                    && (!SqlFormatter.QUANTIFIERS.contains(token)) && (!SqlFormatter.DML.contains(token)) && (!SqlFormatter.MISC.contains(token));
        }

        /**
         * Determines whether a token contains SQL whitespace.
         *
         * @param token token to inspect
         * @return {@code true} when the token is whitespace
         */
        private static boolean isWhitespace(String token) {
            return " \n\r\f\t".contains(token);
        }

        /** Appends a line break followed by the current indentation. */
        private void newline() {
            result.append("\n");

            for (int i = 0; i < indent; i++)
                result.append(indentString);

            beginLine = true;
        }
    }
}
