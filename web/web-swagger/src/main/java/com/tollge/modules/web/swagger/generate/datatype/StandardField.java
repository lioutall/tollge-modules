package com.tollge.modules.web.swagger.generate.datatype;

import io.swagger.v3.oas.models.media.Schema;

import java.lang.reflect.Field;

public interface StandardField {

  default void addField(Field field, Schema properties) {
    Schema fieldProperties = new Schema<>();
    addType(fieldProperties);
    io.swagger.v3.oas.annotations.media.Schema schema = field.getDeclaredAnnotation(io.swagger.v3.oas.annotations.media.Schema.class);
    fieldProperties.setTitle(schema == null ? null : schema.title());
    fieldProperties.setDescription(schema == null ? null : schema.description());
    properties.addProperties(field.getName(), fieldProperties);
  }

  default void addField(String fieldName, Schema properties) {
    Schema fieldProperties = new Schema<>();
    addType(fieldProperties);
    properties.addProperties(fieldName, fieldProperties);
  }

  void addType(Schema properties);
}
