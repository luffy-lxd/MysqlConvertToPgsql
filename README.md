功能：sql转换工具，支持mysql->pgsql

统一基于alibaba.druid库
目前实现功能：
- 支持解析 createTable（支持解析联合主键和联合索引）、alterTable（add、drop、modify、change）、insert into（one、multi）、update、delete、truncate、replace into
- 实现表名、列名、字段名的引号转换
- 支持解析begin commit事务
- 支持解析replace into语句（pgSQL不支持的语法，以insert ... into ... on conflict实现）
- 支持解析复杂where子句，包含组合条件、二元运算符（and or like in..）和逻辑运算符（between、 not between）
- modify和change(pgSQL也不支持)，使用先drop后add的方式实现


工具调用入口：MysqlAnalyse.class 支持传入sql语句或者sql脚本文件
