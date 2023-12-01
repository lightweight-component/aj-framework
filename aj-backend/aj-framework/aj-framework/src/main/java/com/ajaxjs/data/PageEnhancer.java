package com.ajaxjs.data;

import com.ajaxjs.data.jdbc_helper.JdbcReader;
import com.ajaxjs.framework.PageResult;
import com.ajaxjs.framework.spring.DiContextUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * SQL 增强器
 *
 * @author Frank Cheung sp42@qq.com
 */
@Data
@Slf4j
public class PageEnhancer {
    /**
     * 统计总数的 SQL
     */
    public String countTotal;

    /**
     * 分页 SQL
     */
    public String pageSql;

    private int start;

    private int limit;

    private static final String[] PAGE_SIZE = new String[]{"pageSize", "rows", "limit"};

    private static final String[] PAGE_NO = new String[]{"pageNo", "page"};

    /**
     * 获取分页参数
     */
    public void getParams() {
        /* 判断分页参数，兼容 MySQL or 页面两者。最后统一使用 start/limit */
        HttpServletRequest req = DiContextUtil.getRequest();

        if (req == null) {
            // 可能在测试
            start = 0;
            limit = PageResult.DEFAULT_PAGE_SIZE;

            return;
        }

        Integer pageSize = get(req, PAGE_SIZE);
        limit = pageSize == null ? PageResult.DEFAULT_PAGE_SIZE : pageSize;
        Integer pageNo = get(req, PAGE_NO);

        if (pageNo != null)
            start = pageNo2start(pageNo, limit);
        else if (req.getParameter("start") != null)
            start = Integer.parseInt(req.getParameter("start"));
        else
            start = 0;
    }

    /**
     * 根据 HttpServletRequest 和字符串数组返回一个整数。
     *
     * @param req   请求对象
     * @param maybe 字符串数组，包含可能的参数名
     * @return 返回一个整数，如果参数存在且为整数，则返回对应的整数值；否则返回 null
     */
    private static Integer get(HttpServletRequest req, String[] maybe) {
        for (String m : maybe) {
            if (req.getParameter(m) != null)
                return Integer.parseInt(req.getParameter(m));
        }

        return null;
    }

    /**
     * 初始化分页器
     *
     * @param sql SQL语句
     * @return 初始化后的分页器
     */
    public PageEnhancer initSql(String sql) {
        getParams();

        return initSql(sql, start, limit);
    }

    /**
     * 分页
     *
     * @param sql   普通 SELECT 语句
     * @param start 起始数
     * @param limit 读取数量
     * @return 该实例
     */
    public PageEnhancer initSql(String sql, int start, int limit) {
        Select selectStatement = null;

        try {
            selectStatement = (Select) CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            log.warn("ERROR>>", e);
        }

        assert selectStatement != null;
        SelectBody selectBody = selectStatement.getSelectBody();

        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;

            // 设置分页语句
//            Limit limitObj = new Limit();
//            limitObj.setRowCount(new LongValue(limit));
//            limitObj.setOffset(new LongValue(start));
//            plainSelect.setLimit(limitObj);

//            pageSql = selectStatement.toString();
            pageSql = sql + " LIMIT " + start + ", " + limit;

            // 移除 排序 语句
            if (sql.toUpperCase().contains("ORDER BY")) {
                List<OrderByElement> orderBy = plainSelect.getOrderByElements();

                if (orderBy != null) plainSelect.setOrderByElements(null);
            }

            // 创建一个 count 函数的表达式
            Function countFunc = new Function();
            countFunc.setName("COUNT");
            countFunc.setParameters(new ExpressionList(new AllColumns()));

            // 替换所有的 Select Item
            List<SelectItem> selectItems = plainSelect.getSelectItems();
            selectItems.clear();
            selectItems.add(new SelectExpressionItem(countFunc));

            countTotal = selectStatement.toString();
        } else if (selectBody instanceof SetOperationList) {
            SetOperationList setOperationList = (SetOperationList) selectBody;
            List<SelectBody> selectBodies = setOperationList.getSelects();

            /*
             * 我们还考虑了 SQL 查询语句中使用了 SetOperationList 的情况，这时需要对每个 SELECT 子查询都进行分页，同时修改 FROM
             * 部分的表名，以避免语法错误。
             */
            selectBodies.forEach(selectItem -> {
                if (selectItem instanceof PlainSelect) {
                    PlainSelect plainSelect = (PlainSelect) selectItem;
                    Limit limitObj = new Limit();
                    limitObj.setRowCount(new LongValue(limit));
                    limitObj.setOffset(new LongValue(start));
                    plainSelect.setLimit(limitObj);

//                    if (plainSelect.getFromItem() != null) {
                        // modify the original table by adding an alias
//						plainSelect.getFromItem().setAlias(new Table("original_table_alias"));
//                    }
                }
            });

            countTotal = selectStatement.toString();
        }

        return this;
    }

    /**
     * 分页查询方法
     *
     * @param beanCls 实体类类型
     * @return 分页结果
     */
    @SuppressWarnings("unchecked")
    public <T> PageResult<T> page(Class<T> beanCls) {
        PageResult<T> result = new PageResult<>();
        JdbcReader jdbcReader = CRUD.jdbcReaderFactory();
        Long total = jdbcReader.queryOne(countTotal, Long.class);

        if (total != null && total > 0) {
            List<T> list;

            // 如果 beanCls 为 null，则将查询结果作为 Map 列表返回
            // 否则将查询结果转换为指定实体类的列表
            if (beanCls == null) list = (List<T>) jdbcReader.queryAsMapList(pageSql);
            else list = jdbcReader.queryAsBeanList(beanCls, pageSql);

            if (list != null) {
                result.setTotalCount(total.intValue());
                result.addAll(list);

                return result;
            }
        }

        result.setTotalCount(0);
        result.setZero(true);

        return result;
    }

    /**
     * 分页方法
     *
     * @param sql     数据库查询语句
     * @param beanClz 分页结果对应的实体类
     * @param <T>     实体类类型
     * @return 分页结果
     */
    public static <T> PageResult<T> page(String sql, Class<T> beanClz) {
        PageEnhancer p = new PageEnhancer();
        p.initSql(sql);

        return p.page(beanClz);
    }

    /**
     * 将页码和每页数量转换为起始位置
     * pageSize 转换为 MySQL 的 start 分页
     *
     * @param pageNo 页码
     * @param limit  每页数量
     * @return 起始位置
     */
    public static int pageNo2start(int pageNo, int limit) {
        int start = (pageNo - 1) * limit;

        return (start < 0) ? 0 : start;
    }
}
