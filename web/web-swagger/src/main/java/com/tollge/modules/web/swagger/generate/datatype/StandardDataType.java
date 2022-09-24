package com.tollge.modules.web.swagger.generate.datatype;

public enum StandardDataType {
  STRING("java.lang.String"),
  INTEGER_WRAPPER("java.lang.Integer"),
  INTEGER_PRIMITIVE("int"),
  LONG_WRAPPER("java.lang.Long"),
  LONG_PRIMITIVE("long"),
  FLOAT_WRAPPER("java.lang.Float"),
  FLOAT_PRIMITIVE("float"),
  DOUBLE_WRAPPER("java.lang.Double"),
  DOUBLE_PRIMITIVE("double"),
  BYTE_WRAPPER("java.lang.Byte"),
  BYTE_PRIMITIVE("byte"),
  BOOLEAN_WRAPPER("java.lang.Boolean"),
  BOOLEAN_PRIMITIVE("boolean"),
  LOCAL_DATE("java.time.LocalDate"),
  LOCAL_DATE_TIME("java.time.LocalDateTime"),
  DATE("java.util.Date"),
  OFFSET_DATE_TIME("java.time.OffsetDateTime");

  private final String typeName;

  StandardDataType(String typeName) {
    this.typeName = typeName;
  }

  public String getTypeName() {
    return typeName;
  }

  public static StandardDataType fromTypeName(String typeName)
      throws StandardDataTypeNotFoundException {
    for (StandardDataType standardDataType : values()) {
      if (standardDataType.getTypeName().equals(typeName)) {
        return standardDataType;
      }
    }
    throw new StandardDataTypeNotFoundException(typeName);
  }

  public static boolean isStandardDataType(String typeName) {
    for (StandardDataType standardDataType : values()) {
      if (standardDataType.getTypeName().equals(typeName)) {
        return true;
      }
    }
    return false;
  }
}
