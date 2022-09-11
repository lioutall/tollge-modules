package com.tollge.modules.web.swagger.generate.datatype;

import io.swagger.v3.oas.models.media.Schema;

import java.lang.reflect.Field;

public interface StandardField {

  default void addField(Field field, Schema properties) {
    addField(field.getName(), properties);
  }

  default void addField(String fieldName, Schema properties) {
    Schema fieldProperties = new Schema<>();
    addType(fieldProperties);
    properties.addProperties(fieldName, fieldProperties);
  }

  void addType(Schema properties);
}
