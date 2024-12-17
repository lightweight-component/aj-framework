
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

# 各种Java JDK的镜像分发
https://www.injdk.cn/

# 免费 Javadoc 寄存
https://www.javadoc.io/

# Clean Maven

clean-maven.bat
```shell
rem 这里填你的本地仓库路径如(F:\maven\repository)
set REPOSITORY_PATH=C:\sp42\profile\dev\maven\resp
rem Searching now...
for /f "delims=" %%i in ('dir /b /s "%REPOSITORY_PATH%\*lastUpdated*"') do (
    del /s /q %%i
)
for /f "delims=" %%i in ('dir /b /s "%REPOSITORY_PATH%\*unknown*"') do (
    rmdir /s /q %%i
)
rem Clean success
pause
```
find-small.bat
```shell
@echo off
setlocal enabledelayedexpansion

REM 设置要搜索的目录
set "searchDir=C:\sp42\profile\dev\maven\resp"

REM 遍历指定目录及其子目录中的所有 .jar 文件
for /r "%searchDir%" %%f in (*.jar) do (
    REM 获取文件大小（以字节为单位）
    for %%s in (%%~zf) do set "fileSize=%%s"
    
    REM 将文件大小从字节转换为 KB
    set /a fileSizeKB=fileSize/1024
    
    REM 检查文件大小是否小于 3KB
    if !fileSizeKB! lss 4 (
        REM 获取文件所在目录
        set "dirPath=%%~dpf"
        
        REM 删除目录及其内容
        echo Deleting directory: !dirPath!
        rmdir /s /q "!dirPath!"
    )
)

endlocal
pause
```

# 让 IDEA 忽略单词拼写检查的注解

`@SuppressWarnings("SpellCheckingInspection")`