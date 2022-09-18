package com.tollge.modules.web.swagger.generate.collection;


import com.tollge.modules.web.swagger.generate.ConverterService;
import com.tollge.modules.web.swagger.generate.ObjectConverterException;
import io.swagger.v3.oas.models.media.Schema;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class MapField implements CollectionField {

  private static final String DICTIONARY_PATTERN = "java.util.Map<java.lang.String, ";

  private final ConverterService converterService;

  public MapField(ConverterService converterService) {
    this.converterService = converterService;
  }

  @Override
  public void addField(Field field, Schema properties)
      throws ObjectConverterException {
    Schema fieldProperties = new Schema<>();
    fieldProperties.setType("object");
    io.swagger.v3.oas.annotations.media.Schema schema = field.getDeclaredAnnotation(io.swagger.v3.oas.annotations.media.Schema.class);
    fieldProperties.setTitle(schema == null ? null : schema.title());
    fieldProperties.setDescription(schema == null ? null : schema.description());
    Schema additionalProperties = new Schema<>();
    String fieldTypeName = field.getGenericType().getTypeName();
    if (field.getGenericType() instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
      Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
      if (actualTypeArguments.length == 2) {
        if (!isDictionary(actualTypeArguments)) {
          throw new ObjectConverterException(
              String.format("Cannot convert object map field with type: %s", fieldTypeName));
        }
        String valueType = actualTypeArguments[1].getTypeName();
        converterService.convert(valueType, additionalProperties);
      } else if (actualTypeArguments.length == 0) {
        throw new ObjectConverterException(
            String.format("Cannot convert from nested parametrised: %s", fieldTypeName));
      } else {
        throw new ObjectConverterException(
            String.format("Cannot convert object field with type: %s", fieldTypeName));
      }
    } else {
      throw new ObjectConverterException(
          String.format("Cannot convert object field with type: %s", fieldTypeName));
    }
    fieldProperties.addProperties("additionalProperties", additionalProperties);
    properties.addProperties(field.getName(), fieldProperties);
  }

  @Override
  public void addItem(String typeName, Schema properties)
      throws ObjectConverterException {
    if (!typeName.startsWith(DICTIONARY_PATTERN)) {
      throw new ObjectConverterException(
          String.format("Cannot convert object map field with type: %s", typeName));
    }
    String valueTypeName = getValueTypeName(typeName);
    Schema additionalProperties = new Schema<>();
    converterService.convert(valueTypeName, additionalProperties);
    properties.setType("object");
    properties.setAdditionalProperties(additionalProperties);
  }

  @Override
  public void addItems(String typeName, Schema properties)
      throws ObjectConverterException {
    if (!typeName.startsWith(DICTIONARY_PATTERN)) {
      throw new ObjectConverterException(
          String.format("Cannot convert object map field with type: %s", typeName));
    }
    String valueTypeName = getValueTypeName(typeName);
    Schema additionalProperties = new Schema<>();
    converterService.convert(valueTypeName, additionalProperties);
    Schema fieldProperties = new Schema<>();
    fieldProperties.setType("object");
    fieldProperties.addProperties("additionalProperties", additionalProperties);
    properties.addProperties("items", fieldProperties);
  }

  private boolean isDictionary(Type[] actualTypeArguments) {
    return actualTypeArguments[0].getTypeName().equals("java.lang.String");
  }

  private String getValueTypeName(String typeName) {
    return typeName.substring(DICTIONARY_PATTERN.length(), typeName.length() - 1);
  }
}
