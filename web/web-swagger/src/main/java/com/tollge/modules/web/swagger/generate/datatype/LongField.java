package com.tollge.modules.web.swagger.generate.datatype;

import io.swagger.v3.oas.models.media.Schema;

public class LongField implements StandardField {

  @Override
  public void addType(Schema properties) {
    properties.setType("integer");
    properties.setFormat("int64");
  }
}
