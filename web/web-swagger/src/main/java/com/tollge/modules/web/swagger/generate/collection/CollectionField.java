package com.tollge.modules.web.swagger.generate.collection;


import com.tollge.modules.web.swagger.generate.ObjectConverterException;
import io.swagger.v3.oas.models.media.Schema;

import java.lang.reflect.Field;

public interface CollectionField {

  void addField(Field field, Schema properties) throws ObjectConverterException;

  void addItem(String typeName, Schema properties) throws ObjectConverterException;

  void addItems(String typeName, Schema properties) throws ObjectConverterException;
}
