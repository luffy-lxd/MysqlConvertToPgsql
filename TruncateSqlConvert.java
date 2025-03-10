package com.dbappsecurity.cloudpl.dataplatform.sqlconvert;

import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTruncateStatement;
import java.util.List;

public class TruncateSqlConvert {
   public static String process(SQLTruncateStatement truncateStatement) {
      List<SQLExprTableSource> tableSourcesList = truncateStatement.getTableSources();
      String tableName = ((SQLExprTableSource)tableSourcesList.get(0)).getTableName();
      tableName = ConvertColumnAndTableName.convertName(tableName);
      return String.format("truncate table %s;\n", tableName);
   }
}
