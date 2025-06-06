package com.tollge.modules.web.swagger.generate.collection;

import com.tollge.modules.web.swagger.generate.ConverterService;
import com.tollge.modules.web.swagger.generate.ObjectConverterException;
import io.swagger.v3.oas.models.media.Schema;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class PageField implements CollectionField {

  private static final String LIST_TYPE_NAME_PATTERN = "com.tollge.common.Page<";

  private final ConverterService converterService;

  public PageField(ConverterService converterService) {
    this.converterService = converterService;
  }

  @Override
  public void addField(Field field, Schema properties)
          throws ObjectConverterException {
    Schema fieldProperties = new Schema<>();
    fieldProperties.setType("array");
    io.swagger.v3.oas.annotations.media.Schema schema = field.getDeclaredAnnotation(io.swagger.v3.oas.annotations.media.Schema.class);
    fieldProperties.setTitle(schema == null ? null : schema.title());
    fieldProperties.setDescription(schema == null ? null : schema.description());
    String fieldTypeName = field.getGenericType().getTypeName();
    if (field.getGenericType() instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
      Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
      if (actualTypeArguments.length == 1) {
        String valueType = actualTypeArguments[0].getTypeName();
        converterService.addItems(valueType, fieldProperties);
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
    properties.addProperties(field.getName(), fieldProperties);
  }

  @Override
  public void addItem(String typeName, Schema properties)
          throws ObjectConverterException {
    String fieldTypeName = typeName.substring(LIST_TYPE_NAME_PATTERN.length(), typeName.length() - 1);

    Schema fieldProperties = new Schema<>();
    properties.setType("array");
    converterService.addItems(fieldTypeName, fieldProperties);
    properties.setItems(fieldProperties);
  }

  @Override
  public void addItems(String typeName, Schema properties)
          throws ObjectConverterException {
    String fieldTypeName =
            typeName.substring(LIST_TYPE_NAME_PATTERN.length(), typeName.length() - 1);
    Schema fieldProperties = new Schema<>();
    properties.setType("array");
    converterService.addItems(fieldTypeName, fieldProperties);
    properties.setItems(fieldProperties);
  }
}
