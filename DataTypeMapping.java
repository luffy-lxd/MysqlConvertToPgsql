package com.dbappsecurity.cloudpl.dataplatform.sqlconvert;

import java.util.HashMap;

public class DataTypeMapping {
   public static final HashMap<String, String> typeMapping = new HashMap();

   static {
      typeMapping.put("tinyint", "smallint");
      typeMapping.put("mediumint", "integer");
      typeMapping.put("unsigned", "bigint");
      typeMapping.put("float", "real");
      typeMapping.put("double", "double precision");
      typeMapping.put("decimal", "numeric");
      typeMapping.put("tinytext", "text");
      typeMapping.put("mediumtext", "text");
      typeMapping.put("longtext", "text");
      typeMapping.put("datetime", "timestamp");
      typeMapping.put("year", "integer");
      typeMapping.put("unix_timestamp", "timestamp");
      typeMapping.put("tinyint(1)", "boolean");
      typeMapping.put("blob", "bytea");
      typeMapping.put("longblob", "bytea");
      typeMapping.put("varbinary", "bytea");
      typeMapping.put("enum", "text");
      typeMapping.put("set", "text[]");
      typeMapping.put("auto_increment", "serial");
      typeMapping.put("bigint auto_increment", "bigserial");
      typeMapping.put("json", "json");
      typeMapping.put("jsonb", "jsonb");
      typeMapping.put("geometry", "geometry");
   }
}
