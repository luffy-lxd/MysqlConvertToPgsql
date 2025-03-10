package com.dbappsecurity.cloudpl.dataplatform.sqlconvert;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement.ValuesClause;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class InsertSqlConvert {
   public static String process(SQLInsertStatement insert) {
      String tableName = insert.getTableName().toString();
      tableName = ConvertColumnAndTableName.convertName(tableName);
      List<SQLExpr> insertColumns = insert.getColumns();
      List<ValuesClause> valuesList = insert.getValuesList();
      String insertSql = genInsertSql(tableName, insertColumns, valuesList);
      return insertSql;
   }

   private static String genInsertSql(String tableName, List<SQLExpr> insertColumns, List<ValuesClause> valuesList) {
      List<String> colList = (List)insertColumns.stream().map(String::valueOf).collect(Collectors.toList());
      colList = convertCols(colList);
      String colStr = String.join(",", colList);
      colStr = colStr.replaceAll("`", "");
      StringBuilder values = new StringBuilder();
      if (valuesList.size() == 1) {
         List<String> value = convertValues((ValuesClause)valuesList.get(0));
         String singleValue = String.join(",", (Iterable)value.stream().map(String::valueOf).collect(Collectors.toList()));
         values.append(singleValue).append("\n");
         return String.format("INSERT INTO %s (%s) VALUES (%s);\n", tableName, colStr, values);
      } else {
         Iterator var6 = valuesList.iterator();

         while(var6.hasNext()) {
            ValuesClause value = (ValuesClause)var6.next();
            List<String> valueList = convertValues(value);
            String singleValue = String.join(",", (Iterable)valueList.stream().map(String::valueOf).collect(Collectors.toList()));
            values.append("(" + singleValue + ")").append(",").append("\n");
         }

         int len = values.length();
         values.delete(len - 2, len);
         return String.format("INSERT INTO %s (%s) VALUES %s;\n", tableName, colStr, values);
      }
   }

   private static List<String> convertCols(List<String> colList) {
      for(int i = 0; i < colList.size(); ++i) {
         String col = (String)colList.get(i);
         colList.set(i, "\"" + col + "\"");
      }

      return colList;
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
}
