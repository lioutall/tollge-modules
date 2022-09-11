package com.tollge.modules.web.swagger.generate.datatype;

public class StandardDataTypeNotFoundException extends Exception {

  private static final long serialVersionUID = 1020093943819574168L;

  public StandardDataTypeNotFoundException(String typeName) {
    super(String.format("Cannot found standard data type from type name: %s", typeName));
  }
}
