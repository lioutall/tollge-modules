package com.tollge.modules.web.swagger.generate.collection;

public enum CollectionDataType {
  PAGE("com.tollge.common.Page"),
  LIST("java.util.List"),
  MAP("java.util.Map");

  private final String typeName;

  CollectionDataType(String typeName) {
    this.typeName = typeName;
  }

  public String getTypeName() {
    return typeName;
  }

  public static CollectionDataType fromTypeName(String typeName)
      throws CollectionDataTypeNotFoundException {
    for (CollectionDataType collectionDataType : values()) {
      if (typeName.startsWith(collectionDataType.getTypeName())) {
        return collectionDataType;
      }
    }
    throw new CollectionDataTypeNotFoundException(typeName);
  }

  public static boolean isCollectionDataType(String typeName) {
    for (CollectionDataType collectionDataType : values()) {
      if (typeName.startsWith(collectionDataType.getTypeName())) {
        return true;
      }
    }
    return false;
  }
}
