package com.ajaxjs.sql;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.List;

/**
 * 处理 Order by
 *
 * @author liuzh
 * @since 2015-06-27
 */
public class OrderByParser {
	/**
	 * convert to order by sql
	 *
	 * @param sql
	 * @param orderBy
	 * @return
	 */
	public static String converToOrderBySql(String sql, String orderBy) {
		// 解析SQL
		Statement stmt = null;

		try {
			stmt = CCJSqlParserUtil.parse(sql);
			Select select = (Select) stmt;
			SelectBody selectBody = select.getSelectBody();
			// 处理body-去最外层order by
			List<OrderByElement> orderByElements = extraOrderBy(selectBody);
			String defaultOrderBy = PlainSelect.orderByToString(orderByElements);

//			if (defaultOrderBy.indexOf('?') != -1)
//				throw new PageException("原SQL[" + sql + "]中的order by包含参数，因此不能使用OrderBy插件进行修改!");

			// 新的sql
			sql = select.toString();
		} catch (Throwable e) {
//			log("处理排序失败: " + e + "，降级为直接拼接 order by 参数");
		}

		return sql + " order by " + orderBy;
	}

	/**
	 * extra order by and set default orderby to null
	 *
	 * @param selectBody
	 */
	public static List<OrderByElement> extraOrderBy(SelectBody selectBody) {
		if (selectBody != null) {
			if (selectBody instanceof PlainSelect) {
				List<OrderByElement> orderByElements = ((PlainSelect) selectBody).getOrderByElements();
				((PlainSelect) selectBody).setOrderByElements(null);

				return orderByElements;
			} else if (selectBody instanceof WithItem) {
				WithItem withItem = (WithItem) selectBody;
				if (withItem.getSubSelect() != null)
					return extraOrderBy(withItem.getSubSelect().getSelectBody());
			} else {
				SetOperationList operationList = (SetOperationList) selectBody;

				if (operationList.getSelects() != null && operationList.getSelects().size() > 0) {
					List<SelectBody> plainSelects = operationList.getSelects();
					return extraOrderBy(plainSelects.get(plainSelects.size() - 1));
				}
			}
		}

		return null;
	}
}