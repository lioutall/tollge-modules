# oss-tencent

#### 项目介绍
tollge项目的腾讯云对象存储模块，提供腾讯云COS的完整集成方案。该模块支持文件上传、下载、管理等功能，为应用程序提供可靠的云存储服务。

#### 核心特性
- **腾讯云集成**：完整集成腾讯云COS对象存储服务
- **文件上传**：支持文件上传和分片上传
- **文件管理**：支持文件列表、删除、移动等操作
- **访问控制**：支持私有和公有访问权限
- **CDN加速**：支持CDN加速访问
- **断点续传**：支持大文件断点续传
- **生命周期管理**：支持文件生命周期管理

#### 依赖

需要Java 21及以上版本支持。
maven
```xml
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>oss-tencent</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```groovy
implementation 'com.tollge.modules:oss-tencent:0.1.0'
```

#### 快速开始

1. **添加依赖**
   在项目中引入oss-tencent模块

2. **配置tollge.yml**
   ```yaml
   oss:
     tencent:
       secretId: your-secret-id
       secretKey: your-secret-key
       bucket: your-bucket-name
       region: ap-beijing
   ```

3. **配置参数说明**
   - `secretId`: 腾讯云SecretId
   - `secretKey`: 腾讯云SecretKey
   - `bucket`: 存储桶名称
   - `region`: 存储区域

#### 配置详解

##### 基本配置
```yaml
oss:
  tencent:
    secretId: your-secret-id
    secretKey: your-secret-key
    bucket: your-bucket-name
    region: ap-beijing
```

##### 高级配置
```yaml
oss:
  tencent:
    secretId: your-secret-id
    secretKey: your-secret-key
    bucket: your-bucket-name
    region: ap-beijing
    useHttps: true
    useCdn: true
    timeout: 30000
```

#### 存储区域配置

| 区域 | 配置值 |
|------|--------|
| 北京 | ap-beijing |
| 上海 | ap-shanghai |
| 广州 | ap-guangzhou |
| 成都 | ap-chengdu |
| 重庆 | ap-chongqing |
| 香港 | ap-hongkong |
| 新加坡 | ap-singapore |
| 东京 | ap-tokyo |
| 首尔 | ap-seoul |
| 孟买 | ap-mumbai |
| 硅谷 | na-siliconvalley |
| 弗吉尼亚 | na-ashburn |

#### 核心功能

##### 1. 文件上传

###### 简单上传
```java
@Biz("third://oss/tencent")
public class TencentOssBiz extends BizVerticle {
    
    @Path("/upload")
    public void uploadFile(Message<JsonObject> msg) {
        JsonObject params = msg.body();
        String key = params.getString("key");
        String filePath = params.getString("filePath");
        
        COSClient cosClient = createCosClient();
        try {
            PutObjectRequest putRequest = new PutObjectRequest(bucket, key, new File(filePath));
            PutObjectResult result = cosClient.putObject(putRequest);
            
            msg.reply(new JsonObject()
                .put("success", true)
                .put("etag", result.getETag()));
        } catch (CosClientException e) {
            msg.fail(500, "上传失败: " + e.getMessage());
        } finally {
            cosClient.shutdown();
        }
    }
    
    private COSClient createCosClient() {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        return new COSClient(cred, clientConfig);
    }
}
```

###### 流式上传
```java
@Path("/uploadStream")
public void uploadStream(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String key = params.getString("key");
    byte[] data = params.getBinary("data");
    
    COSClient cosClient = createCosClient();
    try {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(data.length);
        
        PutObjectRequest putRequest = new PutObjectRequest(
            bucket, key, new ByteArrayInputStream(data), metadata);
        PutObjectResult result = cosClient.putObject(putRequest);
        
        msg.reply(new JsonObject()
            .put("success", true)
            .put("etag", result.getETag()));
    } catch (CosClientException e) {
        msg.fail(500, "上传失败: " + e.getMessage());
    } finally {
        cosClient.shutdown();
    }
}
```

##### 2. 文件下载

###### 获取下载URL
```java
@Path("/downloadUrl")
public void getDownloadUrl(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String key = params.getString("key");
    long expireSeconds = params.getLong("expireSeconds", 3600L);
    
    COSClient cosClient = createCosClient();
    try {
        Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000);
        URL url = cosClient.generatePresignedUrl(bucket, key, expiration);
        
        msg.reply(new JsonObject()
            .put("url", url.toString())
            .put("expire", expireSeconds));
    } finally {
        cosClient.shutdown();
    }
}
```

###### 下载文件
```java
@Path("/download")
public void downloadFile(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String key = params.getString("key");
    String localPath = params.getString("localPath");
    
    COSClient cosClient = createCosClient();
    try {
        GetObjectRequest getRequest = new GetObjectRequest(bucket, key);
        ObjectMetadata metadata = cosClient.getObject(getRequest, new File(localPath));
        
        msg.reply(new JsonObject()
            .put("success", true)
            .put("size", metadata.getContentLength()));
    } catch (CosClientException e) {
        msg.fail(500, "下载失败: " + e.getMessage());
    } finally {
        cosClient.shutdown();
    }
}
```

##### 3. 文件管理

###### 获取文件列表
```java
@Path("/list")
public void listFiles(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String prefix = params.getString("prefix", "");
    int maxKeys = params.getInteger("maxKeys", 100);
    
    COSClient cosClient = createCosClient();
    try {
        ListObjectsRequest listRequest = new ListObjectsRequest();
        listRequest.setBucketName(bucket);
        listRequest.setPrefix(prefix);
        listRequest.setMaxKeys(maxKeys);
        
        ObjectListing listing = cosClient.listObjects(listRequest);
        
        JsonArray files = new JsonArray();
        for (COSObjectSummary summary : listing.getObjectSummaries()) {
            files.add(new JsonObject()
                .put("key", summary.getKey())
                .put("size", summary.getSize())
                .put("etag", summary.getETag())
                .put("lastModified", summary.getLastModified().getTime()));
        }
        
        msg.reply(new JsonObject()
            .put("files", files)
            .put("truncated", listing.isTruncated()));
    } finally {
        cosClient.shutdown();
    }
}
```

###### 删除文件
```java
@Path("/delete")
public void deleteFile(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String key = params.getString("key");
    
    COSClient cosClient = createCosClient();
    try {
        cosClient.deleteObject(bucket, key);
        msg.reply(new JsonObject().put("success", true));
    } catch (CosClientException e) {
        msg.fail(500, "删除失败: " + e.getMessage());
    } finally {
        cosClient.shutdown();
    }
}
```

##### 4. 分片上传

###### 初始化分片上传
```java
@Path("/initMultipartUpload")
public void initMultipartUpload(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String key = params.getString("key");
    
    COSClient cosClient = createCosClient();
    try {
        InitiateMultipartUploadRequest initRequest = 
            new InitiateMultipartUploadRequest(bucket, key);
        InitiateMultipartUploadResult initResult = 
            cosClient.initiateMultipartUpload(initRequest);
        
        msg.reply(new JsonObject()
            .put("uploadId", initResult.getUploadId()));
    } finally {
        cosClient.shutdown();
    }
}
```

###### 上传分片
```java
@Path("/uploadPart")
public void uploadPart(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String key = params.getString("key");
    String uploadId = params.getString("uploadId");
    int partNumber = params.getInteger("partNumber");
    byte[] data = params.getBinary("data");
    
    COSClient cosClient = createCosClient();
    try {
        UploadPartRequest uploadRequest = new UploadPartRequest();
        uploadRequest.setBucketName(bucket);
        uploadRequest.setKey(key);
        uploadRequest.setUploadId(uploadId);
        uploadRequest.setPartNumber(partNumber);
        uploadRequest.setInputStream(new ByteArrayInputStream(data));
        uploadRequest.setPartSize(data.length);
        
        UploadPartResult result = cosClient.uploadPart(uploadRequest);
        
        msg.reply(new JsonObject()
            .put("etag", result.getETag())
            .put("partNumber", partNumber));
    } finally {
        cosClient.shutdown();
    }
}
```

###### 完成分片上传
```java
@Path("/completeMultipartUpload")
public void completeMultipartUpload(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String key = params.getString("key");
    String uploadId = params.getString("uploadId");
    JsonArray parts = params.getJsonArray("parts");
    
    COSClient cosClient = createCosClient();
    try {
        CompleteMultipartUploadRequest compRequest = 
            new CompleteMultipartUploadRequest(bucket, key, uploadId, null);
        
        List<PartETag> partETags = new ArrayList<>();
        parts.forEach(part -> {
            JsonObject partObj = (JsonObject) part;
            partETags.add(new PartETag(
                partObj.getInteger("partNumber"), 
                partObj.getString("etag")));
        });
        
        compRequest.setPartETags(partETags);
        CompleteMultipartUploadResult compResult = 
            cosClient.completeMultipartUpload(compRequest);
        
        msg.reply(new JsonObject()
            .put("success", true)
            .put("etag", compResult.getETag()));
    } finally {
        cosClient.shutdown();
    }
}
```

#### 高级功能

##### 1. 生命周期管理

```java
@Path("/setLifecycle")
public void setLifecycle(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String prefix = params.getString("prefix");
    int days = params.getInteger("days");
    
    COSClient cosClient = createCosClient();
    try {
        BucketLifecycleConfiguration.Rule rule = 
            new BucketLifecycleConfiguration.Rule()
                .withId("delete-old-files")
                .withStatus(BucketLifecycleConfiguration.ENABLED)
                .withPrefix(prefix)
                .withExpirationInDays(days);
        
        BucketLifecycleConfiguration configuration = 
            new BucketLifecycleConfiguration().withRules(Arrays.asList(rule));
        
        cosClient.setBucketLifecycleConfiguration(bucket, configuration);
        msg.reply(new JsonObject().put("success", true));
    } finally {
        cosClient.shutdown();
    }
}
```

##### 2. 访问权限管理

```java
@Path("/setAcl")
public void setObjectAcl(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String key = params.getString("key");
    String acl = params.getString("acl"); // "public-read" or "private"
    
    COSClient cosClient = createCosClient();
    try {
        CannedAccessControlList cannedAcl = 
            "public-read".equals(acl) ? 
            CannedAccessControlList.PublicRead : 
            CannedAccessControlList.Private;
        
        cosClient.setObjectAcl(bucket, key, cannedAcl);
        msg.reply(new JsonObject().put("success", true));
    } finally {
        cosClient.shutdown();
    }
}
```

#### 配置详解

##### 完整配置示例
```yaml
oss:
  tencent:
    secretId: your-secret-id
    secretKey: your-secret-key
    bucket: your-bucket-name
    region: ap-beijing
    useHttps: true
    useCdn: true
    timeout: 30000
    maxConnections: 100
```

#### 错误处理

```java
try {
    // 腾讯云COS操作
} catch (CosServiceException e) {
    // 服务端错误
    System.err.println("错误码: " + e.getErrorCode());
    System.err.println("错误信息: " + e.getErrorMessage());
} catch (CosClientException e) {
    // 客户端错误
    System.err.println("客户端错误: " + e.getMessage());
}
```

#### 最佳实践

1. **连接复用**：使用连接池复用连接
2. **异常重试**：实现合理的重试机制
3. **分片上传**：大文件使用分片上传
4. **权限控制**：合理设置文件权限
5. **生命周期**：设置合理的文件生命周期

#### 完整示例

##### 文件上传服务
```java
@Biz("third://oss/tencent")
public class TencentOssService extends BizVerticle {
    
    @Path("/upload")
    public void uploadFile(Message<JsonObject> msg) {
        JsonObject params = msg.body();
        String fileName = params.getString("fileName");
        String fileData = params.getString("fileData"); // base64
        
        String key = "uploads/" + UUID.randomUUID().toString() + "-" + fileName;
        
        COSClient cosClient = createCosClient();
        try {
            byte[] data = Base64.getDecoder().decode(fileData);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(data.length);
            metadata.setContentType("application/octet-stream");
            
            PutObjectRequest putRequest = new PutObjectRequest(
                bucket, key, new ByteArrayInputStream(data), metadata);
            PutObjectResult result = cosClient.putObject(putRequest);
            
            String url = "https://" + bucket + ".cos." + region + ".myqcloud.com/" + key;
            
            msg.reply(new JsonObject()
                .put("success", true)
                .put("key", key)
                .put("url", url)
                .put("etag", result.getETag()));
        } catch (CosClientException e) {
            msg.fail(500, "上传失败: " + e.getMessage());
        } finally {
            cosClient.shutdown();
        }
    }
}
```

#### 监控和诊断

```java
// 获取存储桶统计信息
@Path("/stats")
public void getBucketStats(Message<JsonObject> msg) {
    COSClient cosClient = createCosClient();
    try {
        HeadBucketResult result = cosClient.headBucket(bucket);
        
        JsonObject stats = new JsonObject()
            .put("bucket", bucket)
            .put("region", region)
            .put("exists", true);
        
        msg.reply(stats);
    } catch (CosClientException e) {
        msg.fail(500, "获取统计失败: " + e.getMessage());
    } finally {
        cosClient.shutdown();
    }
}
```

#### 完整示例

参考测试代码获取完整使用示例：
- [测试用例](src/test/java/test/tencent/) - 完整腾讯云COS操作示例
- [配置示例](src/test/resources/) - 各种配置示例