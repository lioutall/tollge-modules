package com.tollge.modules.web.swagger.generate.datatype;

import io.swagger.v3.oas.models.media.Schema;

public class DateTimeField implements StandardField {

  @Override
  public void addType(Schema properties) {
    properties.setType("string");
    properties.setFormat("date-time");
  }
}
