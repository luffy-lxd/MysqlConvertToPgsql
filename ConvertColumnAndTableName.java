package com.dbappsecurity.cloudpl.dataplatform.sqlconvert;

public class ConvertColumnAndTableName {
   public static String convertName(String name) {
      return name.startsWith("`") && name.endsWith("`") ? "\"" + name.substring(1, name.length() - 1) + "\"" : "\"" + name + "\"";
   }
}
