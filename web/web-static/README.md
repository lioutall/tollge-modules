# web-static

Static web server module based on tollge framework.

## Introduction

This module provides a simple static file web server. It can serve static files such as HTML, CSS, JavaScript, images, etc.

## Configuration

In `tollge.yml`:

```yaml
web.static:
  web.root: webroot     # Static file root directory, default is webroot
  uri.prefix: /         # URI prefix, default is /

application:
  static.port: 8080       # HTTP port
```

## Usage

Enable this module by adding it to your project's dependencies. The static files should be placed in the directory specified by `web.root`.
