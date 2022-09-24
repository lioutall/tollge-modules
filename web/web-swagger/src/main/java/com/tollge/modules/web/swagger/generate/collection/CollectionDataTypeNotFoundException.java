package com.tollge.modules.web.swagger.generate.collection;

public class CollectionDataTypeNotFoundException extends Exception {

  private static final long serialVersionUID = -6437712561016952006L;

  public CollectionDataTypeNotFoundException(String typeName) {
    super(String.format("Cannot found collection data type from type name: %s", typeName));
  }
}
