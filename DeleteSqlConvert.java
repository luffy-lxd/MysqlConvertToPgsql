package com.dbappsecurity.cloudpl.dataplatform.sqlconvert;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLValuableExpr;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import java.util.Iterator;

public class DeleteSqlConvert {
   public static String process(SQLDeleteStatement deleteStatement) {
      String tableName = deleteStatement.getTableName().toString();
      tableName = ConvertColumnAndTableName.convertName(tableName);
      SQLExpr where = deleteStatement.getWhere();
      StringBuilder sql = new StringBuilder();
      sql.append(String.format("delete from %s ", tableName));
      String pgSQLWhere = null;
      if (where != null) {
         pgSQLWhere = convertWhereToPGSQL(where);
      }

      if (pgSQLWhere != null) {
         sql.append(" where " + pgSQLWhere);
      }

      sql.append(";\n");
      return sql.toString();
   }

   private static String convertWhereToPGSQL(SQLExpr expr) {
      if (expr instanceof SQLIdentifierExpr) {
         return ((SQLIdentifierExpr)expr).getName().replace("`", "");
      } else if (expr instanceof SQLPropertyExpr) {
         return ((SQLPropertyExpr)expr).getName().replace("`", "");
      } else {
         String left;
         if (expr instanceof SQLValuableExpr) {
            SQLValuableExpr valuable = (SQLValuableExpr)expr;
            left = valuable.toString();
            left = left.replace("\"", "'");
            return left;
         } else {
            String leftValue;
            if (expr instanceof SQLBinaryOpExpr) {
               SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr)expr;
               left = convertWhereToPGSQL(binaryOpExpr.getLeft());
               leftValue = convertWhereToPGSQL(binaryOpExpr.getRight());
               return left + " " + binaryOpExpr.getOperator().getName() + " " + leftValue;
            } else {
               String conditionString;
               if (!(expr instanceof SQLInListExpr)) {
                  if (expr instanceof SQLBetweenExpr) {
                     SQLBetweenExpr betweenExpr = (SQLBetweenExpr)expr;
                     left = convertWhereToPGSQL(betweenExpr.getTestExpr());
                     leftValue = convertWhereToPGSQL(betweenExpr.getBeginExpr());
                     conditionString = convertWhereToPGSQL(betweenExpr.getEndExpr());
                     String conditionString = expr.toString();
                     return conditionString.toLowerCase().contains("not between") ? left + " NOT BETWEEN " + leftValue + " AND " + conditionString : left + " BETWEEN " + leftValue + " AND " + conditionString;
                  } else {
                     return expr.toString();
                  }
               } else {
                  SQLInListExpr inExpr = (SQLInListExpr)expr;
                  left = convertWhereToPGSQL(inExpr.getExpr());
                  StringBuilder values = new StringBuilder();
                  Iterator var4 = inExpr.getTargetList().iterator();

                  while(var4.hasNext()) {
                     SQLExpr value = (SQLExpr)var4.next();
                     values.append(convertWhereToPGSQL(value)).append(", ");
                  }

                  if (values.length() > 0) {
                     values.setLength(values.length() - 2);
                  }

                  conditionString = inExpr.toString();
                  return conditionString.toLowerCase().contains("not in") ? left + " NOT IN (" + values.toString() + ")" : left + " IN (" + values.toString() + ")";
               }
            }
         }
      }
   }
}
