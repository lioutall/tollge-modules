# data-meilisearch

#### 项目介绍
tollge项目的Meilisearch搜索引擎模块，提供高性能的全文搜索功能。该模块基于Meilisearch REST API实现，支持文档索引、搜索、过滤、排序等完整功能，为应用程序提供强大的搜索能力。

#### 核心特性
- **全文搜索**：支持高性能全文搜索
- **实时索引**：支持实时文档索引和更新
- **多字段搜索**：支持多字段联合搜索
- **过滤和排序**：支持复杂的过滤条件和排序规则
- **多索引支持**：支持多个索引的并行操作
- **联合搜索**：支持多索引联合搜索
- **RESTful API**：基于HTTP的RESTful API设计

#### 依赖

需要Java 21及以上版本支持。
maven
```xml
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>data-meilisearch</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```groovy
implementation 'com.tollge.modules:data-meilisearch:0.1.0'
```

#### 快速开始

1. **添加依赖**
   在项目中引入data-meilisearch模块

2. **配置tollge.yml**
   ```yaml
   meilisearch:
     host: http://127.0.0.1:7700
     masterKey: your-master-key
   ```

3. **配置参数说明**
   - `host`: Meilisearch服务器地址
   - `masterKey`: 主密钥，用于身份验证

#### 配置详解

##### 基本配置
```yaml
meilisearch:
  host: http://127.0.0.1:7700
  masterKey: your-master-key
```

##### HTTPS配置
```yaml
meilisearch:
  host: https://your-meilisearch.com
  masterKey: your-master-key
  isSsl: true
  port: 443
```

#### 核心功能

##### 1. 文档管理

###### 添加文档
```java
// 添加单个文档
JsonObject document = new JsonObject()
    .put("id", "1")
    .put("title", "Java编程指南")
    .put("content", "这是一本关于Java编程的完整指南")
    .put("author", "张三")
    .put("publishDate", "2024-01-01");

MyMeilisearch.getInstance().addOne("books", new JsonArray().add(document))
    .onSuccess(result -> {
        System.out.println("文档添加成功: " + result);
    });

// 批量添加文档
JsonArray documents = new JsonArray()
    .add(new JsonObject().put("id", "1").put("title", "Java编程"))
    .add(new JsonObject().put("id", "2").put("title", "Python编程"));

MyMeilisearch.getInstance().addOne("books", documents)
    .onSuccess(result -> {
        System.out.println("批量文档添加成功");
    });
```

###### 删除文档
```java
// 删除单个文档
MyMeilisearch.getInstance().deleteOne("books", "1")
    .onSuccess(result -> {
        System.out.println("文档删除成功");
    });

// 批量删除文档
JsonArray documentIds = new JsonArray().add("1").add("2").add("3");
MyMeilisearch.getInstance().deleteMany("books", documentIds)
    .onSuccess(result -> {
        System.out.println("批量文档删除成功");
    });
```

###### 获取文档
```java
// 获取单个文档
MyMeilisearch.getInstance().getOne("books", "1")
    .onSuccess(document -> {
        System.out.println("获取文档: " + document);
    });
```

##### 2. 搜索功能

###### 基本搜索
```java
// 创建搜索请求
SearchRequest request = new SearchRequest();
request.setQ("Java编程");
request.setLimit(10);
request.setOffset(0);

// 执行搜索
MyMeilisearch.getInstance().search("books", request)
    .onSuccess(response -> {
        System.out.println("搜索结果: " + response.getHits());
        System.out.println("总数量: " + response.getEstimatedTotalHits());
    });
```

###### 高级搜索
```java
// 带过滤条件的搜索
SearchRequest request = new SearchRequest();
request.setQ("编程");
request.setFilter("publishDate >= 2024-01-01");
request.setSort(new JsonArray().add("publishDate:desc"));
request.setAttributesToRetrieve(new JsonArray().add("title").add("author"));
request.setAttributesToHighlight(new JsonArray().add("title").add("content"));

MyMeilisearch.getInstance().search("books", request)
    .onSuccess(response -> {
        response.getHits().forEach(hit -> {
            System.out.println("标题: " + hit.getString("title"));
            System.out.println("高亮: " + hit.getJsonObject("_highlightResult"));
        });
    });
```

##### 3. 多索引搜索

###### 非联合多索引搜索
```java
// 创建多索引搜索请求
MultiSearchRequest request = new MultiSearchRequest();
request.setQueries(new JsonArray()
    .add(new JsonObject()
        .put("indexUid", "books")
        .put("q", "Java")
        .put("limit", 5))
    .add(new JsonObject()
        .put("indexUid", "articles")
        .put("q", "Java")
        .put("limit", 5)));

// 执行搜索
MyMeilisearch.getInstance().multiSearch(request)
    .onSuccess(response -> {
        response.getResults().forEach(result -> {
            System.out.println("索引: " + result.getIndexUid());
            System.out.println("结果: " + result.getHits());
        });
    });
```

###### 联合多索引搜索
```java
// 创建联合搜索请求
MultiSearchRequest request = new MultiSearchRequest();
request.setQueries(new JsonArray()
    .add(new JsonObject()
        .put("indexUid", "books")
        .put("q", "Java")
        .put("limit", 5))
    .add(new JsonObject()
        .put("indexUid", "articles")
        .put("q", "Java")
        .put("limit", 5)));

request.setFederation(new JsonObject()
    .put("limit", 10)
    .put("offset", 0));

// 执行联合搜索
MyMeilisearch.getInstance().multiSearchFederate(request)
    .onSuccess(response -> {
        System.out.println("联合搜索结果: " + response.getHits());
        System.out.println("总数量: " + response.getEstimatedTotalHits());
    });
```

#### 搜索请求配置

##### SearchRequest参数说明
```java
SearchRequest request = new SearchRequest();
request.setQ("搜索关键词");           // 搜索关键词
request.setOffset(0);               // 偏移量，用于分页
request.setLimit(20);               // 返回结果数量限制
request.setFilter("status = active"); // 过滤条件
request.setSort(new JsonArray().add("date:desc")); // 排序规则
request.setFacets(new JsonArray().add("category")); // 分面搜索
request.setAttributesToRetrieve(new JsonArray().add("title").add("content")); // 返回字段
request.setAttributesToSearchOn(new JsonArray().add("title").add("description")); // 搜索字段
request.setAttributesToHighlight(new JsonArray().add("title")); // 高亮字段
request.setShowMatchesPosition(true); // 显示匹配位置
```

##### 响应结果处理
```java
MyMeilisearch.getInstance().search("books", request)
    .onSuccess(response -> {
        // 获取搜索结果
        JsonArray hits = response.getHits();
        
        // 获取估计总数
        Integer estimatedTotal = response.getEstimatedTotalHits();
        
        // 获取分面结果
        JsonObject facetDistribution = response.getFacetDistribution();
        
        // 处理分页
        Integer offset = response.getOffset();
        Integer limit = response.getLimit();
        
        hits.forEach(hit -> {
            JsonObject document = (JsonObject) hit;
            System.out.println("文档: " + document);
        });
    });
```

#### 索引管理

##### 创建索引
虽然索引管理通常通过Meilisearch管理界面完成，但也可以通过API进行：

```java
// 创建索引配置
JsonObject indexConfig = new JsonObject()
    .put("uid", "books")
    .put("primaryKey", "id");

// 设置可搜索属性
JsonObject settings = new JsonObject()
    .put("searchableAttributes", new JsonArray().add("title").add("content").add("author"))
    .put("filterableAttributes", new JsonArray().add("category").add("publishDate"))
    .put("sortableAttributes", new JsonArray().add("publishDate").add("price"));
```

#### 错误处理

```java
MyMeilisearch.getInstance().search("books", request)
    .onSuccess(response -> {
        // 处理成功响应
    })
    .onFailure(error -> {
        if (error instanceof TollgeException) {
            // 处理Meilisearch错误
            System.err.println("搜索错误: " + error.getMessage());
        } else {
            // 处理网络或其他错误
            System.err.println("系统错误: " + error.getMessage());
        }
    });
```

#### 性能优化建议

1. **索引设计**：合理设计索引结构，避免过多字段
2. **搜索字段**：明确指定搜索字段，提高搜索精度
3. **过滤条件**：使用过滤条件减少搜索范围
4. **分页优化**：合理设置limit和offset参数
5. **缓存策略**：对热门搜索结果实施缓存

#### 配置详解

##### 完整配置示例
```yaml
meilisearch:
  host: http://127.0.0.1
  port: 7700
  masterKey: your-master-key
  isSsl: false
```

##### 生产环境配置
```yaml
meilisearch:
  host: https://your-meilisearch.com
  port: 443
  masterKey: your-production-master-key
  isSsl: true
```

#### 完整示例

##### 图书搜索服务
```java
@Biz("biz://search")
public class SearchBiz extends BizVerticle {
    
    @Path("/books")
    public void searchBooks(Message<JsonObject> msg) {
        JsonObject params = msg.body();
        String keyword = params.getString("keyword");
        Integer page = params.getInteger("page", 1);
        Integer size = params.getInteger("size", 10);
        
        SearchRequest request = new SearchRequest();
        request.setQ(keyword);
        request.setOffset((page - 1) * size);
        request.setLimit(size);
        request.setAttributesToRetrieve(new JsonArray()
            .add("id")
            .add("title")
            .add("author")
            .add("price"));
        
        MyMeilisearch.getInstance().search("books", request)
            .onSuccess(response -> {
                msg.reply(JsonObject.mapFrom(response));
            })
            .onFailure(error -> {
                msg.fail(500, "搜索失败: " + error.getMessage());
            });
    }
}
```

#### 监控和诊断

```java
// 获取索引统计信息
MyMeilisearch.getInstance().getIndexStats("books")
    .onSuccess(stats -> {
        System.out.println("文档数量: " + stats.getInteger("numberOfDocuments"));
        System.out.println("索引大小: " + stats.getString("indexSize"));
    });
```

#### 完整示例

参考测试代码获取完整使用示例：
- [测试用例](src/test/java/test/meilisearch/) - 完整Meilisearch操作示例
- [配置示例](src/test/resources/) - 各种配置示例
