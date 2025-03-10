package com.dbappsecurity.cloudpl.dataplatform.sqlconvert;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLBlockStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLReplaceStatement;
import com.alibaba.druid.sql.ast.statement.SQLTruncateStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MysqlAnalyse {
   private static final Logger log = LoggerFactory.getLogger(MysqlAnalyse.class);

   public static String convertToPgsql(File curFile) throws IOException {
      String sqlContent = null;
      if (curFile.exists() && curFile.isFile()) {
         sqlContent = new String(Files.readAllBytes(curFile.toPath()));
      }

      return convertToPgsql(sqlContent);
   }

   public static String convertToPgsql(String sqlContent) throws IOException {
      SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sqlContent, DbType.mysql);
      List<SQLStatement> ali_statements = parser.parseStatementList();
      if (ali_statements.isEmpty()) {
         return null;
      } else {
         StringBuilder sql = new StringBuilder();
         Iterator var4 = ali_statements.iterator();

         while(true) {
            while(var4.hasNext()) {
               SQLStatement statement = (SQLStatement)var4.next();
               if (statement instanceof SQLBlockStatement) {
                  List<SQLStatement> statementList = ((SQLBlockStatement)statement).getStatementList();
                  Iterator var7 = statementList.iterator();

                  while(var7.hasNext()) {
                     SQLStatement transaction_statement = (SQLStatement)var7.next();
                     String curSql = null;
                     curSql = process(transaction_statement);
                     if (curSql != null) {
                        sql.append(curSql);
                     }
                  }
               } else {
                  String curSql = null;
                  curSql = process(statement);
                  if (curSql != null) {
                     sql.append(curSql);
                  }
               }
            }

            return sql.toString();
         }
      }
   }

   private static String process(SQLStatement statement) {
      String pgSql = null;
      if (statement instanceof SQLCreateTableStatement) {
         pgSql = CreateSqlConvert.process((SQLCreateTableStatement)statement);
      } else if (statement instanceof SQLAlterTableStatement) {
         pgSql = AlterSqlConvert.process((SQLAlterTableStatement)statement);
      } else if (statement instanceof SQLInsertStatement) {
         pgSql = InsertSqlConvert.process((SQLInsertStatement)statement);
      } else if (statement instanceof SQLDeleteStatement) {
         pgSql = DeleteSqlConvert.process((SQLDeleteStatement)statement);
      } else if (statement instanceof SQLUpdateStatement) {
         pgSql = UpdateSqlConvert.process((SQLUpdateStatement)statement);
         if (pgSql.isEmpty()) {
            log.error("update解析错误");
            return null;
         }
      } else if (statement instanceof SQLTruncateStatement) {
         pgSql = TruncateSqlConvert.process((SQLTruncateStatement)statement);
      } else if (statement instanceof SQLReplaceStatement) {
         pgSql = ReplaceSqlConvert.process((SQLReplaceStatement)statement);
      }

      return pgSql;
   }
}
