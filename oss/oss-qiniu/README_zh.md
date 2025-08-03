# oss-qiniu

#### 项目介绍
tollge项目的七牛云对象存储模块，提供七牛云OSS的完整集成方案。该模块支持文件上传、下载、管理等功能，为应用程序提供可靠的云存储服务。

#### 核心特性
- **七牛云集成**：完整集成七牛云对象存储服务
- **文件上传**：支持文件上传和分片上传
- **文件管理**：支持文件列表、删除、移动等操作
- **访问控制**：支持私有空间和公开空间
- **CDN加速**：支持CDN加速访问
- **断点续传**：支持大文件断点续传
- **Token管理**：自动管理上传Token

#### 依赖

需要Java 21及以上版本支持。
maven
```xml
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>oss-qiniu</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```groovy
implementation 'com.tollge.modules:oss-qiniu:0.1.0'
```

#### 快速开始

1. **添加依赖**
   在项目中引入oss-qiniu模块

2. **配置tollge.yml**
   ```yaml
   oss:
     qiniu:
       accessKey: your-access-key
       secretKey: your-secret-key
       bucket: your-bucket-name
       domain: your-domain.com
   ```

3. **配置参数说明**
   - `accessKey`: 七牛云Access Key
   - `secretKey`: 七牛云Secret Key
   - `bucket`: 存储空间名称
   - `domain`: 自定义域名

#### 配置详解

##### 基本配置
```yaml
oss:
  qiniu:
    accessKey: your-access-key
    secretKey: your-secret-key
    bucket: your-bucket-name
    domain: your-domain.com
```

##### 高级配置
```yaml
oss:
  qiniu:
    accessKey: your-access-key
    secretKey: your-secret-key
    bucket: your-bucket-name
    domain: your-domain.com
    region: z0  # 存储区域：z0(华东), z1(华北), z2(华南), na0(北美), as0(东南亚)
    useHttps: true
    useCdnDomains: true
```

#### 核心功能

##### 1. 上传Token获取

```java
@Biz("third://oss/qiniu")
public class QiniuBiz extends BizVerticle {
    
    @Path("/uploadToken")
    public void getUploadToken(Message<JsonObject> msg) {
        JsonObject params = msg.body();
        String key = params.getString("key");
        long expireSeconds = params.getLong("expireSeconds", 3600L);
        
        // 生成上传Token
        String token = generateUploadToken(key, expireSeconds);
        
        msg.reply(new JsonObject()
            .put("token", token)
            .put("expire", expireSeconds));
    }
    
    private String generateUploadToken(String key, long expireSeconds) {
        Auth auth = Auth.create(accessKey, secretKey);
        return auth.uploadToken(bucket, key, expireSeconds, null);
    }
}
```

##### 2. 文件上传

###### 前端上传示例
```javascript
// 获取上传Token
fetch('/third/oss/qiniu/uploadToken', {
    method: 'POST',
    body: JSON.stringify({key: 'test.jpg', expireSeconds: 3600})
})
.then(response => response.json())
.then(data => {
    const formData = new FormData();
    formData.append('token', data.token);
    formData.append('key', 'test.jpg');
    formData.append('file', fileInput.files[0]);
    
    // 上传到七牛云
    fetch('https://upload.qiniup.com', {
        method: 'POST',
        body: formData
    });
});
```

##### 3. 文件管理

###### 获取文件列表
```java
@Path("/list")
public void listFiles(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String prefix = params.getString("prefix", "");
    int limit = params.getInteger("limit", 100);
    
    // 获取文件列表
    BucketManager bucketManager = new BucketManager(auth, cfg);
    FileListing listing = bucketManager.listFiles(bucket, prefix, null, limit, null);
    
    JsonArray files = new JsonArray();
    for (FileInfo file : listing.items) {
        files.add(new JsonObject()
            .put("key", file.key)
            .put("size", file.fsize)
            .put("mimeType", file.mimeType)
            .put("putTime", file.putTime));
    }
    
    msg.reply(new JsonObject().put("files", files));
}
```

###### 删除文件
```java
@Path("/delete")
public void deleteFile(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String key = params.getString("key");
    
    try {
        BucketManager bucketManager = new BucketManager(auth, cfg);
        bucketManager.delete(bucket, key);
        msg.reply(new JsonObject().put("success", true));
    } catch (QiniuException e) {
        msg.fail(500, "删除失败: " + e.getMessage());
    }
}
```

##### 4. 文件下载

###### 获取下载URL
```java
@Path("/downloadUrl")
public void getDownloadUrl(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String key = params.getString("key");
    long expireSeconds = params.getLong("expireSeconds", 3600L);
    
    String domain = "https://your-domain.com";
    String url = domain + "/" + key;
    
    // 私有空间需要签名
    if (isPrivateSpace()) {
        url = auth.privateDownloadUrl(url, expireSeconds);
    }
    
    msg.reply(new JsonObject().put("url", url));
}
```

##### 5. 图片处理

###### 获取缩略图URL
```java
@Path("/imageUrl")
public void getImageUrl(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String key = params.getString("key");
    int width = params.getInteger("width", 200);
    int height = params.getInteger("height", 200);
    
    String baseUrl = "https://your-domain.com/" + key;
    String imageUrl = baseUrl + "?imageView2/1/w/" + width + "/h/" + height;
    
    msg.reply(new JsonObject().put("url", imageUrl));
}
```

#### 高级功能

##### 1. 断点续传

```java
@Path("/resumableUpload")
public void resumableUpload(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String key = params.getString("key");
    String localFilePath = params.getString("filePath");
    
    UploadManager uploadManager = new UploadManager(cfg);
    String token = auth.uploadToken(bucket, key);
    
    try {
        Response response = uploadManager.put(localFilePath, key, token);
        msg.reply(new JsonObject()
            .put("success", true)
            .put("hash", response.jsonToMap().get("hash")));
    } catch (QiniuException e) {
        msg.fail(500, "上传失败: " + e.getMessage());
    }
}
```

##### 2. 分片上传

```java
@Path("/multipartUpload")
public void multipartUpload(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String key = params.getString("key");
    String uploadId = params.getString("uploadId");
    
    // 分片上传逻辑
    // 实现大文件分片上传
}
```

##### 3. 回调验证

```java
@Path("/callback")
public void handleCallback(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String auth = params.getString("auth");
    
    boolean isValid = Auth.isValidCallback(auth, secretKey);
    if (isValid) {
        // 处理回调数据
        msg.reply(new JsonObject().put("success", true));
    } else {
        msg.fail(403, "回调验证失败");
    }
}
```

#### 配置详解

##### 存储区域配置
```yaml
oss:
  qiniu:
    accessKey: your-access-key
    secretKey: your-secret-key
    bucket: your-bucket-name
    domain: your-domain.com
    region: z0  # 华东
    # region: z1  # 华北
    # region: z2  # 华南
    # region: na0 # 北美
    # region: as0 # 东南亚
```

##### CDN配置
```yaml
oss:
  qiniu:
    accessKey: your-access-key
    secretKey: your-secret-key
    bucket: your-bucket-name
    domain: your-cdn-domain.com
    useCdnDomains: true
    useHttps: true
```

#### 错误处理

```java
try {
    // 七牛云操作
} catch (QiniuException e) {
    Response response = e.response;
    if (response != null) {
        int statusCode = response.statusCode;
        String error = response.error;
        // 根据错误码处理
    }
}
```

#### 最佳实践

1. **Token缓存**：上传Token应该缓存，避免频繁生成
2. **文件命名**：使用有意义的文件命名规则
3. **权限控制**：合理设置空间权限
4. **CDN加速**：使用CDN加速文件访问
5. **错误重试**：实现合理的重试机制

#### 完整示例

##### 文件上传服务
```java
@Biz("third://oss/qiniu")
public class QiniuFileService extends BizVerticle {
    
    @Path("/upload")
    public void uploadFile(Message<JsonObject> msg) {
        JsonObject params = msg.body();
        String fileName = params.getString("fileName");
        String fileData = params.getString("fileData"); // base64
        
        // 生成唯一文件名
        String key = UUID.randomUUID().toString() + "-" + fileName;
        
        // 上传文件
        byte[] data = Base64.getDecoder().decode(fileData);
        String token = generateUploadToken(key, 3600);
        
        UploadManager uploadManager = new UploadManager(cfg);
        try {
            Response response = uploadManager.put(data, key, token);
            JsonObject result = new JsonObject()
                .put("success", true)
                .put("key", key)
                .put("url", "https://your-domain.com/" + key);
            msg.reply(result);
        } catch (QiniuException e) {
            msg.fail(500, "上传失败: " + e.getMessage());
        }
    }
}
```

#### 监控和诊断

```java
// 获取空间统计信息
@Path("/stats")
public void getBucketStats(Message<JsonObject> msg) {
    try {
        BucketManager bucketManager = new BucketManager(auth, cfg);
        BucketInfo info = bucketManager.getBucketInfo(bucket);
        
        JsonObject stats = new JsonObject()
            .put("name", info.name)
            .put("region", info.region)
            .put("private", info.private);
        
        msg.reply(stats);
    } catch (QiniuException e) {
        msg.fail(500, "获取统计失败: " + e.getMessage());
    }
}
```

#### 完整示例

参考测试代码获取完整使用示例：
- [测试用例](src/test/java/test/qiniu/) - 完整七牛云操作示例
- [配置示例](src/test/resources/) - 各种配置示例