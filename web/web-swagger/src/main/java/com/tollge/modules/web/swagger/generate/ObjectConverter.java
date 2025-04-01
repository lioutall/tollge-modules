package com.tollge.modules.web.swagger.generate;

import io.swagger.v3.oas.models.media.Schema;

import java.lang.reflect.Field;
import java.util.Map;

public class ObjectConverter {

  private final ConverterService converterService;

  ObjectConverter(ConverterService converterService) {
    this.converterService = converterService;
  }

  public void convertObject(Map<String, Schema> modelMap, String typeName, Schema refModel)
          throws ObjectConverterException {
    try {
      // type已经构造过
      if(modelMap.containsKey(typeName)) {
        refModel.$ref("#/components/schemas/" + typeName);
      } else {
        Schema model = new Schema<>();
        Class<?> clazz = Class.forName(typeName);
        io.swagger.v3.oas.annotations.media.Schema schema = clazz.getDeclaredAnnotation(io.swagger.v3.oas.annotations.media.Schema.class);
        model.setTitle(schema == null ? null : schema.title());
        model.setDescription(schema == null ? null : schema.description());
        for (Field field : clazz.getDeclaredFields()) {
          String fieldTypeName = field.getType().getCanonicalName();
          converterService.addField(field, fieldTypeName, model);
        }
        Class<?> superclass = clazz.getSuperclass();
        if(superclass != null) {
          if(superclass.getCanonicalName().startsWith("com.tollge")) {
            for (Field field : superclass.getDeclaredFields()) {
              String fieldTypeName = field.getType().getCanonicalName();
              converterService.addField(field, fieldTypeName, model);
            }
          }
        }
        refModel.$ref("#/components/schemas/" + typeName);
        modelMap.put(typeName, model);
      }

    } catch (ClassNotFoundException e) {
      throw new ObjectConverterException(
              String.format("Cannot convert class with name: %s", typeName), e);
    }
  }

}
