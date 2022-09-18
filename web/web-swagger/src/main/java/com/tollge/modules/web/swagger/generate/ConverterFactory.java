package com.tollge.modules.web.swagger.generate;


import com.tollge.modules.web.swagger.generate.collection.CollectionDataTypes;
import com.tollge.modules.web.swagger.generate.collection.CollectionField;
import com.tollge.modules.web.swagger.generate.datatype.StandardDataTypes;
import com.tollge.modules.web.swagger.generate.datatype.StandardField;
import io.swagger.v3.oas.models.media.Schema;

import java.lang.reflect.Field;

/**
 * 源码地址: https://github.com/sitMCella/openapi-3-object-converter
 */
public class ConverterFactory implements ConverterService {

  private static ConverterFactory converterFactory;

  private final ObjectConverter objectConverter;
  private final CollectionDataTypes collectionDataTypes;
  private final StandardDataTypes standardDataTypes;

  private ConverterFactory() {
    if (converterFactory != null) {
      throw new RuntimeException("Use getInstance() method to get the single instance of this class");
    }
    objectConverter = new ObjectConverter(this);
    collectionDataTypes = new CollectionDataTypes(this);
    standardDataTypes = new StandardDataTypes();
  }

  ConverterFactory(
      ObjectConverter objectConverter,
      CollectionDataTypes collectionDataTypes,
      StandardDataTypes standardDataTypes) {
    this.objectConverter = objectConverter;
    this.collectionDataTypes = collectionDataTypes;
    this.standardDataTypes = standardDataTypes;
  }

  public static ConverterFactory getInstance() {
    if (converterFactory == null) {
      converterFactory = new ConverterFactory();
    }
    return converterFactory;
  }

  @Override
  public Schema convert(String typeName) throws ObjectConverterException {
    Schema schemaDefinition = new Schema<>();
    getInstance().convert(typeName, schemaDefinition);
    return schemaDefinition;
  }

  public boolean isStandardDataType(String typeName) {
    return standardDataTypes.isStandardDataType(typeName);
  }

  @Override
  public void convert(String typeName, Schema properties) throws ObjectConverterException {
    if (standardDataTypes.isStandardDataType(typeName)) {
      StandardField standardField = standardDataTypes.getStandardField(typeName);
      standardField.addType(properties);
      return;
    }
    if (collectionDataTypes.isCollectionDataType(typeName)) {
      CollectionField collectionField = collectionDataTypes.getCollectionField(typeName);
      collectionField.addItem(typeName, properties);
      return;
    }
    objectConverter.convertObject(typeName, properties);
  }

  @Override
  public void addField(Field field, String typeName, Schema properties) throws ObjectConverterException {
    if (standardDataTypes.isStandardDataType(typeName)) {
      StandardField standardField = standardDataTypes.getStandardField(typeName);
      standardField.addField(field, properties);
      return;
    }
    if (collectionDataTypes.isCollectionDataType(typeName)) {
      CollectionField collectionField = collectionDataTypes.getCollectionField(typeName);
      collectionField.addField(field, properties);
    }
  }

  @Override
  public void addItems(String typeName, Schema properties) throws ObjectConverterException {
    if (collectionDataTypes.isCollectionDataType(typeName)) {
      CollectionField collectionField = collectionDataTypes.getCollectionField(typeName);
      collectionField.addItems(typeName, properties);
      return;
    }
    if (standardDataTypes.isStandardDataType(typeName)) {
      standardDataTypes.addItems(typeName, properties);
      return;
    }
    objectConverter.addItems(typeName, properties);
  }
}