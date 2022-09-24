package com.tollge.modules.web.swagger.generate;

import io.swagger.v3.oas.models.media.Schema;

import java.lang.reflect.Field;

public interface ConverterService {

  Schema convert(String typeName) throws ObjectConverterException;

  void convert(String typeName, Schema properties) throws ObjectConverterException;

  void addField(Field field, String typeName, Schema properties)
      throws ObjectConverterException;

  void addItems(String typeName, Schema properties) throws ObjectConverterException;
}