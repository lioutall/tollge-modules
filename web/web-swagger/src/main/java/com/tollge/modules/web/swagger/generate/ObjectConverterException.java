package com.tollge.modules.web.swagger.generate;

public class ObjectConverterException extends Exception {

  private static final long serialVersionUID = 1L;

  public ObjectConverterException(String message) {
    super(message);
  }

  public ObjectConverterException(String message, Throwable cause) {
    super(message, cause);
  }
}