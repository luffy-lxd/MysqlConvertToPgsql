package com.dbappsecurity.cloudpl.dataplatform.sqlconvert;

import java.util.HashMap;

public class DefaultValueMapping {
   public static final HashMap<String, String> MYSQL_DEFAULT_TO_POSTGRE_DEFAULT = new HashMap();

   static {
      MYSQL_DEFAULT_TO_POSTGRE_DEFAULT.put("NULL", "NULL");
      MYSQL_DEFAULT_TO_POSTGRE_DEFAULT.put("CURRENT_TIMESTAMP", "CURRENT_TIMESTAMP");
      MYSQL_DEFAULT_TO_POSTGRE_DEFAULT.put("CURRENT_DATE", "CURRENT_DATE");
      MYSQL_DEFAULT_TO_POSTGRE_DEFAULT.put("CURRENT_TIME", "CURRENT_TIME");
   }
}
