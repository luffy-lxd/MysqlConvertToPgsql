package com.dbappsecurity.cloudpl.dataplatform.sqlconvert;

import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddColumn;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropColumnItem;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableItem;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLColumnConstraint;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableChangeColumn;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableModifyColumn;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class AlterSqlConvert {
   public static String process(SQLAlterTableStatement alter) {
      StringBuilder alterSql = new StringBuilder();
      String tableName = ConvertColumnAndTableName.convertName(alter.getTableName());
      String firstLine = String.format("ALTER TABLE %s \n", tableName);
      List<String> commentList = new ArrayList();
      Iterator var5 = alter.getItems().iterator();

      while(true) {
         while(var5.hasNext()) {
            SQLAlterTableItem item = (SQLAlterTableItem)var5.next();
            List columnDefinitionList;
            if (item instanceof SQLAlterTableAddColumn) {
               columnDefinitionList = ((SQLAlterTableAddColumn)item).getColumns();
               Iterator var15 = columnDefinitionList.iterator();

               while(var15.hasNext()) {
                  SQLColumnDefinition columnDefinition = (SQLColumnDefinition)var15.next();
                  alterSql.append(firstLine);
                  alterSql.append(genAddSql(columnDefinition, tableName, commentList));
               }
            } else if (item instanceof SQLAlterTableDropColumnItem) {
               alterSql.append(firstLine);
               columnDefinitionList = ((SQLAlterTableDropColumnItem)item).getColumns();
               SQLName column = (SQLName)columnDefinitionList.get(0);
               String columnName = ConvertColumnAndTableName.convertName(column.toString());
               alterSql.append(genDropSql(columnName));
            } else if (item instanceof MySqlAlterTableModifyColumn) {
               alterSql.append(firstLine);
               SQLColumnDefinition column = ((MySqlAlterTableModifyColumn)item).getNewColumnDefinition();
               String columnName = ConvertColumnAndTableName.convertName(column.getColumnName());
               alterSql.append(genDropSql(columnName));
               alterSql.append(firstLine);
               alterSql.append(genAddSql(column, tableName, commentList));
            } else if (item instanceof MySqlAlterTableChangeColumn) {
               alterSql.append(firstLine);
               String oldColName = ConvertColumnAndTableName.convertName(((MySqlAlterTableChangeColumn)item).getColumnName().toString());
               alterSql.append(genDropSql(oldColName));
               alterSql.append(firstLine);
               SQLColumnDefinition newColumn = ((MySqlAlterTableChangeColumn)item).getNewColumnDefinition();
               alterSql.append(genAddSql(newColumn, tableName, commentList));
            }
         }

         String fullCommentSql = String.join("\n", commentList);
         if (!commentList.isEmpty()) {
            alterSql.append(fullCommentSql);
         }

         return alterSql.toString();
      }
   }

   private static String genDropSql(String columnName) {
      String dropSql = String.format("DROP COLUMN %s;\n", columnName);
      return dropSql;
   }

   private static String genAddSql(SQLColumnDefinition columnDefinition, String tableName, List<String> commentList) {
      String colName = ConvertColumnAndTableName.convertName(columnDefinition.getColumnName());
      SQLDataType type = columnDefinition.getDataType();
      String typeName = type.getName();
      List<SQLExpr> arguments = type.getArguments();
      String dataTypeSql = genDataTypeSql(typeName, arguments);
      List<SQLColumnConstraint> columnSpecs = columnDefinition.getConstraints();
      String constraintsSql = genConstrainsSql(columnSpecs);
      String defultSql = null;
      if (columnDefinition.getDefaultExpr() != null) {
         String defultStr = columnDefinition.getDefaultExpr().toString();
         defultSql = genDefultSql(defultStr);
      }

      StringBuilder addSql = new StringBuilder();
      addSql.append(genFullAddSql(colName, dataTypeSql, constraintsSql, defultSql));
      SQLExpr comment = columnDefinition.getComment();
      String commentValue = "";
      String commentSql = "";
      if (comment != null) {
         commentValue = comment.toString();
         commentSql = genCommentSql(tableName, colName, commentValue);
         commentList.add(commentSql);
      }

      return addSql.toString();
   }

   private static String genCommentSql(String tableName, String colName, String commentValue) {
      return String.format("COMMENT ON COLUMN %s.%s IS %s;", tableName, colName, commentValue);
   }

   private static String genFullAddSql(String columnName, String dataTypeSql, String constraintsSql, String defultSql) {
      String fullAddSql = null;
      if (defultSql != null) {
         if (constraintsSql != null && !constraintsSql.equalsIgnoreCase("null")) {
            fullAddSql = String.format("ADD %s %s %s %s;\n", columnName, dataTypeSql, constraintsSql, defultSql);
         } else {
            fullAddSql = String.format("ADD %s %s %s;\n", columnName, dataTypeSql, defultSql);
         }
      } else if (constraintsSql != null && !constraintsSql.equalsIgnoreCase("null")) {
         fullAddSql = String.format("ADD %s %s %s;\n", columnName, dataTypeSql, constraintsSql);
      } else {
         fullAddSql = String.format("ADD %s %s;\n", columnName, dataTypeSql);
      }

      return fullAddSql;
   }

   private static String genDefultSql(String defultStr) {
      StringBuilder sql = new StringBuilder();
      return defultStr.equals("NULL") ? new String() : sql.append("DEFAULT ").append(defultStr).toString();
   }

   private static String genConstrainsSql(List<SQLColumnConstraint> columnSpecs) {
      String sql = null;
      List<String> specsList = (List)columnSpecs.stream().map(String::valueOf).collect(Collectors.toList());
      sql = String.join(" ", specsList);
      return sql;
   }

   private static String genDataTypeSql(String typeName, List<SQLExpr> arguments) {
      String pgType = typeName;
      String argument = null;
      if (DataTypeMapping.typeMapping.containsKey(typeName)) {
         pgType = (String)DataTypeMapping.typeMapping.get(typeName);
      }

      if (arguments != null && arguments.size() != 0) {
         if (arguments.size() == 1) {
            argument = ((SQLExpr)arguments.get(0)).toString();
         } else if (arguments.size() == 2) {
            argument = arguments.get(0) + "," + arguments.get(1);
         }
      }

      if (argument != null && argument.trim().length() != 0) {
         if (!pgType.equalsIgnoreCase("bigint") && !pgType.equalsIgnoreCase("smallint") && !pgType.equalsIgnoreCase("int")) {
            pgType = pgType + "(" + argument + ")";
         } else {
            pgType = pgType;
         }
      }

      return pgType;
   }
}
