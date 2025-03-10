package com.dbappsecurity.cloudpl.dataplatform.sqlconvert;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.statement.SQLReplaceStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement.ValuesClause;
import java.util.List;
import java.util.stream.Collectors;

public class ReplaceSqlConvert {
   public static String process(SQLReplaceStatement replaceStatement) {
      String tableName = replaceStatement.getTableName().toString();
      if (tableName.startsWith("`") && tableName.endsWith("`")) {
         tableName = tableName.replaceAll("`", "");
      }

      List<SQLExpr> columns = replaceStatement.getColumns();
      List<String> columnStringList = (List)columns.stream().map(String::valueOf).collect(Collectors.toList());
      List<ValuesClause> values = replaceStatement.getValuesList();
      List<String> valueStringList = convertValues((ValuesClause)values.get(0));
      String fullSql = genFullSql(tableName, columnStringList, valueStringList);
      return fullSql;
   }

   private static String genFullSql(String tableName, List<String> columnStringList, List<String> valueStringList) {
      columnStringList = convertCols(columnStringList);
      String columnString = String.join(",", columnStringList);
      columnString = columnString.replaceAll("`", "");
      String valueString = String.join(",", valueStringList);
      String preSql = String.format("insert into %s (%s)\nvalues (%s)\non conflict(id)\ndo update set ", tableName, columnString, valueString);
      StringBuilder postSql = new StringBuilder();

      for(int i = 0; i < columnStringList.size(); ++i) {
         String colName = (String)columnStringList.get(i);
         colName = colName.replaceAll("`", "");
         if (!((String)columnStringList.get(i)).equalsIgnoreCase("`id`")) {
            String sql = String.format("%s = excluded.%s, ", colName, colName);
            postSql.append(sql);
         }
      }

      postSql.deleteCharAt(postSql.length() - 1);
      postSql.deleteCharAt(postSql.length() - 1);
      postSql.append(";\n");
      String fullSql = preSql + postSql;
      return fullSql;
   }

   private static List<String> convertValues(ValuesClause valuesClause) {
      List<SQLExpr> values = valuesClause.getValues();
      List<String> valuesString = (List)values.stream().map(String::valueOf).collect(Collectors.toList());

      for(int i = 0; i < valuesString.size(); ++i) {
         String curVal = (String)valuesString.get(i);
         if (curVal.startsWith("\"") && curVal.endsWith("\"")) {
            curVal = "'" + curVal.substring(1, curVal.length() - 1) + "'";
            valuesString.set(i, curVal);
         }
      }

      return valuesString;
   }

   private static List<String> convertCols(List<String> colList) {
      for(int i = 0; i < colList.size(); ++i) {
         String col = (String)colList.get(i);
         colList.set(i, "\"" + col + "\"");
      }

      return colList;
   }
}
