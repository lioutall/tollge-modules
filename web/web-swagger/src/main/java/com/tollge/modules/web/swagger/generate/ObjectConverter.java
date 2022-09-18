package com.tollge.modules.web.swagger.generate;

import io.swagger.v3.oas.models.media.Schema;

import java.lang.reflect.Field;

public class ObjectConverter {

  private final ConverterService converterService;

  ObjectConverter(ConverterService converterService) {
    this.converterService = converterService;
  }

  public void convertObject(String typeName, Schema objectProperties)
      throws ObjectConverterException {
    try {
      Class<?> clazz = Class.forName(typeName);
      objectProperties.setType("object");
      io.swagger.v3.oas.annotations.media.Schema schema = clazz.getDeclaredAnnotation(io.swagger.v3.oas.annotations.media.Schema.class);
      objectProperties.setTitle(schema == null ? null : schema.title());
      objectProperties.setDescription(schema == null ? null : schema.description());
      for (Field field : clazz.getDeclaredFields()) {
        String fieldTypeName = field.getType().getCanonicalName();
        converterService.addField(field, fieldTypeName, objectProperties);
      }
    } catch (ClassNotFoundException e) {
      throw new ObjectConverterException(
          String.format("Cannot convert class with name: %s", typeName), e);
    }
  }

  public void addItems(String typeName, Schema properties)
      throws ObjectConverterException {
    Schema objectProperties = new Schema<>();
    convertObject(typeName, objectProperties);
    properties.addProperties("items", objectProperties);
  }
}
