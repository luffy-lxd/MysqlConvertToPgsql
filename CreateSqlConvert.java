package com.dbappsecurity.cloudpl.dataplatform.sqlconvert;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLIndexDefinition;
import com.alibaba.druid.sql.ast.statement.SQLColumnConstraint;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLPrimaryKey;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class CreateSqlConvert {
   public static String process(SQLCreateTableStatement createTableStatement) {
      String tableName = ConvertColumnAndTableName.convertName(createTableStatement.getTableName());
      List<SQLColumnDefinition> columnDefinitionList = createTableStatement.getColumnDefinitions();
      String tableCommentString = null;
      SQLExpr tableComment = createTableStatement.getComment();
      String tableCommentSql = null;
      if (tableComment != null) {
         tableCommentString = tableComment.toString();
         tableCommentSql = String.format("COMMENT ON TABLE %s IS %s;", tableName, tableCommentString);
      }

      List<String> columnCommentsSqlList = genColumnCommentSql(tableName, columnDefinitionList);
      List<SQLTableElement> tableElementList = createTableStatement.getTableElementList();
      List<String> primaryKeys = getPrimaryKey(columnDefinitionList, tableElementList);
      String primaryKeySQL = genPrimaryKeySQL(primaryKeys);
      List<String> createIndexSqlList = genIndexSql(tableName, tableElementList);
      List<String> columnSqlList = genColumnSql(columnDefinitionList);
      String fullSql = genFullSql(tableName, columnSqlList, primaryKeySQL, createIndexSqlList, columnCommentsSqlList, tableCommentSql);
      return fullSql;
   }

   private static String genFullSql(String tableName, List<String> columnSqlList, String primaryKeySQL, List<String> createIndexSqlList, List<String> columnCommentsSqlList, String tableCommentSql) {
      StringBuilder fullSql = new StringBuilder("create table ");
      fullSql.append(tableName + "(\n");
      Iterator var7 = columnSqlList.iterator();

      String columnComment;
      while(var7.hasNext()) {
         columnComment = (String)var7.next();
         fullSql.append(columnComment + ",\n");
      }

      if (primaryKeySQL != null) {
         fullSql.append(primaryKeySQL + "\n);\n");
      }

      var7 = createIndexSqlList.iterator();

      while(var7.hasNext()) {
         columnComment = (String)var7.next();
         fullSql.append(columnComment + ";\n");
      }

      if (tableCommentSql != null) {
         fullSql.append(tableCommentSql + "\n");
      }

      var7 = columnCommentsSqlList.iterator();

      while(var7.hasNext()) {
         columnComment = (String)var7.next();
         fullSql.append(columnComment + "\n");
      }

      return fullSql.toString();
   }

   private static List<String> genColumnSql(List<SQLColumnDefinition> columnDefinitionList) {
      List<String> columnSqlList = new ArrayList();

      String value;
      for(Iterator var2 = columnDefinitionList.iterator(); var2.hasNext(); columnSqlList.add(value)) {
         SQLColumnDefinition columnDefinition = (SQLColumnDefinition)var2.next();
         String colName = columnDefinition.getColumnName();
         colName = ConvertColumnAndTableName.convertName(colName);
         String dataType = columnDefinition.getDataType().getName();
         List<SQLExpr> argumentList = columnDefinition.getDataType().getArguments();
         String postgreDataType = dataType;
         if (DataTypeMapping.typeMapping.containsKey(dataType)) {
            postgreDataType = (String)DataTypeMapping.typeMapping.get(dataType);
         }

         if (postgreDataType == null) {
            throw new UnsupportedOperationException("mysql dataType not supported yet. " + dataType);
         }

         String arguments = null;
         if (argumentList != null) {
            if (argumentList.size() == 1) {
               arguments = ((SQLExpr)argumentList.get(0)).toString();
            } else if (argumentList.size() == 2) {
               arguments = argumentList.get(0) + "," + argumentList.get(1);
            }
         }

         if (arguments != null && !arguments.trim().isEmpty()) {
            if (!postgreDataType.equalsIgnoreCase("bigint") && !postgreDataType.equalsIgnoreCase("smallint") && !postgreDataType.equalsIgnoreCase("int")) {
               postgreDataType = postgreDataType + "(" + arguments + ")";
            } else {
               postgreDataType = postgreDataType;
            }
         }

         List<SQLColumnConstraint> constraintList = columnDefinition.getConstraints();
         String constrantsString = "";
         String constrants;
         if (constraintList != null) {
            List<String> constrainStringList = (List)constraintList.stream().map(String::valueOf).collect(Collectors.toList());
            constrants = String.join(" ", constrainStringList);
            constrants = constrants.toLowerCase();
            if (constrants.contains("unsigned")) {
               constrants = constrants.replaceAll("unsigned", "");
            }

            if (constrants.contains("on update current_timestamp")) {
               constrants = constrants.replaceAll("on update current_timestamp", "");
            }

            if (constrants.equalsIgnoreCase("null")) {
               constrants = "";
            }

            if (constrants.contains("primary key")) {
               constrants = constrants.replaceAll("primary key", "");
            }

            constrantsString = constrants;
         }

         SQLExpr defultValue = columnDefinition.getDefaultExpr();
         constrants = "";
         if (defultValue != null) {
            value = defultValue.toString();
            if (value.startsWith("\"") && value.endsWith("\"")) {
               value = value.replaceAll("\"", "'");
            } else if (value.equalsIgnoreCase("null")) {
               value = "";
            } else if (DefaultValueMapping.MYSQL_DEFAULT_TO_POSTGRE_DEFAULT.containsKey(value)) {
               value = (String)DefaultValueMapping.MYSQL_DEFAULT_TO_POSTGRE_DEFAULT.get(value);
            }

            constrants = value;
         }

         value = null;
         if (constrants == "") {
            value = String.format("%s %s %s", colName, postgreDataType, constrantsString);
         } else {
            value = String.format("%s %s %s default %s", colName, postgreDataType, constrantsString, constrants);
         }
      }

      return columnSqlList;
   }

   private static List<String> genIndexSql(String tableName, List<SQLTableElement> tableElementList) {
      List<String> createIndexSql = new ArrayList();
      Iterator var3 = tableElementList.iterator();

      while(true) {
         SQLIndexDefinition index;
         do {
            SQLTableElement tableElement;
            do {
               if (!var3.hasNext()) {
                  return createIndexSql;
               }

               tableElement = (SQLTableElement)var3.next();
            } while(!(tableElement instanceof SQLIndexDefinition));

            new ArrayList();
            index = (SQLIndexDefinition)tableElement;
         } while(index.getType() != null && !"INDEX".equalsIgnoreCase(index.getType()));

         List<SQLSelectOrderByItem> colNames = index.getColumns();
         String indexSql = genCurrentIndexSql(tableName, colNames, index.getName().toString());
         createIndexSql.add(indexSql);
      }
   }

   private static String genCurrentIndexSql(String tableName, List<SQLSelectOrderByItem> colNames, String indexName) {
      List<String> colNameList = (List)colNames.stream().map(String::valueOf).collect(Collectors.toList());

      String colName;
      for(int i = 0; i < colNameList.size(); ++i) {
         colName = (String)colNameList.get(i);
         if (colName.startsWith("`") && colName.endsWith("`")) {
            colNameList.set(i, ConvertColumnAndTableName.convertName(colName));
         }
      }

      String colNamesString = String.join(",", colNameList);
      colName = String.format("create index %s on %s (%s)", indexName, tableName, colNamesString);
      return colName;
   }

   private static String genPrimaryKeySQL(List<String> primaryKeys) {
      if (primaryKeys.isEmpty()) {
         return null;
      } else {
         String curString;
         for(int i = 0; i < primaryKeys.size(); ++i) {
            curString = (String)primaryKeys.get(i);
            if (curString.startsWith("`") && curString.endsWith("`")) {
               primaryKeys.set(i, curString.substring(1, curString.length() - 1));
            }
         }

         String Keys = String.join(",", primaryKeys);
         curString = String.format("primary key (%s)", Keys);
         return curString;
      }
   }

   private static List<String> getPrimaryKey(List<SQLColumnDefinition> columnDefinitionList, List<SQLTableElement> tableElementList) {
      List<String> primaryKeys = new ArrayList();
      Iterator var3 = tableElementList.iterator();

      while(true) {
         SQLTableElement tableElement;
         List constraints;
         Iterator var6;
         do {
            if (!var3.hasNext()) {
               if (primaryKeys.isEmpty()) {
                  var3 = columnDefinitionList.iterator();

                  while(true) {
                     SQLColumnDefinition columnDefinition;
                     do {
                        if (!var3.hasNext()) {
                           return primaryKeys;
                        }

                        columnDefinition = (SQLColumnDefinition)var3.next();
                        constraints = columnDefinition.getConstraints();
                     } while(constraints.isEmpty());

                     var6 = constraints.iterator();

                     while(var6.hasNext()) {
                        SQLColumnConstraint constraint = (SQLColumnConstraint)var6.next();
                        if (constraint.toString().equalsIgnoreCase("primary key")) {
                           primaryKeys.add(columnDefinition.getColumnName());
                           return primaryKeys;
                        }
                     }
                  }
               }

               return primaryKeys;
            }

            tableElement = (SQLTableElement)var3.next();
         } while(!(tableElement instanceof SQLPrimaryKey));

         constraints = ((SQLPrimaryKey)tableElement).getColumns();
         var6 = constraints.iterator();

         while(var6.hasNext()) {
            SQLSelectOrderByItem key = (SQLSelectOrderByItem)var6.next();
            primaryKeys.add(key.toString());
         }
      }
   }

   private static List<String> genColumnCommentSql(String tableName, List<SQLColumnDefinition> columnDefinitionList) {
      List<String> columnCommentStrings = new ArrayList();
      Iterator var3 = columnDefinitionList.iterator();

      while(var3.hasNext()) {
         SQLColumnDefinition columnDefinition = (SQLColumnDefinition)var3.next();
         SQLExpr comment = columnDefinition.getComment();
         if (comment != null) {
            String commentSql = genCommentSql(tableName, columnDefinition.getColumnName().toString(), comment.toString());
            columnCommentStrings.add(commentSql);
         }
      }

      return columnCommentStrings;
   }

   private static String genCommentSql(String tableName, String columnName, String comment) {
      columnName = ConvertColumnAndTableName.convertName(columnName);
      return String.format("COMMENT ON COLUMN %s.%s IS %s;", tableName, columnName, comment);
   }
}
