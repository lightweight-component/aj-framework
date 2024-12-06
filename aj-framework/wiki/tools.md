
# 将 mysql 表结构导出表设计文档

```sql
SELECT
  TABLE_NAME 表名,
  COLUMN_NAME 列名,
  COLUMN_COMMENT 注释,
  COLUMN_TYPE 数据类型,
CASE
    
    WHEN COLUMN_DEFAULT IS NULL THEN
    'null' 
    WHEN COLUMN_DEFAULT = '' THEN
    '''''' ELSE COLUMN_DEFAULT 
  END 默认值,
  COLUMN_KEY AS 键,
  IS_NULLABLE AS 是否为NULL,
  EXTRA AS 额外 
FROM
  INFORMATION_SCHEMA.COLUMNS 
  WHERE-- office 为数据库名称，到时候只需要修改成你要导出表结构的数据库即可
  table_schema = 'aj_base' 
  AND (-- 查表名
  table_name IN ('article', 'feedback', 'budget_year_total', 'employee_budget', 'employee_budget_transaction', 'user', 'permission', 'role') OR table_name LIKE ('t_%')) 
ORDER BY
  table_name
```
