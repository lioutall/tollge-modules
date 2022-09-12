package com.tollge.modules.web.swagger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tollge.common.ResultFormat;
import com.tollge.common.TollgeException;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.annotation.mark.request.*;
import com.tollge.common.util.Properties;
import com.tollge.common.util.ReflectionUtil;
import com.tollge.modules.web.http.Http;
import com.tollge.modules.web.swagger.generate.ConverterFactory;
import com.tollge.modules.web.swagger.generate.ObjectConverterException;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.vertx.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class SwaggerVerticle extends AbstractVerticle {

    private final IpUtil ipUtil = new IpUtil();


    @Override
    @SuppressWarnings("unchecked")
    public void start() {


        // swagger开关, 默认关闭
        Boolean swaggerEnable = Properties.getBoolean("swagger", "enable");
        if (!Boolean.TRUE.equals(swaggerEnable)) {
            return;
        }

        log.info("tollge-swagger starting...");

        //todo swagger基本配置


        io.vertx.ext.web.Router router = io.vertx.ext.web.Router.router(vertx);
        int httpPort = Properties.getInteger("application", "http.port");
        int swaggerPort = Properties.getInteger("application", "swagger.port");

        OpenAPI openAPIDoc = new OpenAPI();
        Info info = new Info();
        info.setTitle("Swagger");
        info.setVersion("1.0.0");
        openAPIDoc.setInfo(info);
        List<Server> servers = Lists.newArrayList(new Server().url("http://" + ipUtil.getInetAddress().getHostAddress() + ":" + httpPort + "/"));
        openAPIDoc.setServers(servers);

        // 定义PathItem组
        Map<String, PathItem> pathItemMap = Maps.newHashMap();

        // 定义Model组
        Map<String, Schema> modelMap = Maps.newHashMap();

        // 找到所有http router
        Set<Class<?>> set = ReflectionUtil.getClassesWithAnnotated(Http.class);
        for (Class<?> c : set) {
            Http http = c.getAnnotation(Http.class);
            if (http == null) {
                continue;
            }

            // 扫描 @Path
            Method[] ms = c.getMethods();
            for (Method method : ms) {
                Path p = method.getAnnotation(Path.class);
                if (p == null) {
                    continue;
                }

                Operation operation = fetchOperation(openAPIDoc, pathItemMap, http, p);

                // 设置参数
                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                Class<?>[] parameterTypes = method.getParameterTypes();
                java.lang.reflect.Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameterAnnotations.length; i++) {
                    Annotation[] parameterAnnotation = parameterAnnotations[i];
                    for (Annotation annotation : parameterAnnotation) {
                        boolean hasSet = false;
                        if (annotation.annotationType() == PathParam.class) {
                            PathParam param = (PathParam)annotation;
                            Parameter parameter = new Parameter();
                            parameter.setName(param.value());
                            parameter.setDescription(param.description());

                            Schema schema = new Schema<>();
                            schema.setType(parameterTypes[i].getSimpleName());
                            schema.setMinLength(param.minLength());
                            schema.setMaxLength(param.maxLength());
                            schema.setPattern(param.regex());
                            parameter.setSchema(schema);
                            parameter.setRequired(param.required());
                            operation.addParametersItem(parameter);
                            hasSet = true;
                        }
                        if (annotation.annotationType() == QueryParam.class) {
                            checkHasSet(hasSet, c, method, parameters[i]);
                            QueryParam param = (QueryParam)annotation;
                            Parameter parameter = new Parameter();
                            parameter.setName(param.value());
                            parameter.setDescription(param.description());

                            Schema schema = new Schema<>();
                            schema.setType(parameterTypes[i].getSimpleName());
                            schema.setMinLength(param.minLength());
                            schema.setMaxLength(param.maxLength());
                            schema.setPattern(param.regex());
                            parameter.setSchema(schema);
                            parameter.setRequired(param.required());
                            operation.addParametersItem(parameter);
                        }
                        if (annotation.annotationType() == HeaderParam.class) {
                            checkHasSet(hasSet, c, method, parameters[i]);
                            HeaderParam param = (HeaderParam)annotation;
                            Parameter parameter = new Parameter();
                            parameter.setName(param.value());
                            parameter.setDescription(param.description());

                            Schema schema = new Schema<>();
                            schema.setType(parameterTypes[i].getSimpleName());
                            schema.setMinLength(param.minLength());
                            schema.setMaxLength(param.maxLength());
                            schema.setPattern(param.regex());
                            parameter.setSchema(schema);
                            parameter.setRequired(param.required());
                            operation.addParametersItem(parameter);
                        }
                        if (annotation.annotationType() == FormParam.class) {
                            checkHasSet(hasSet, c, method, parameters[i]);
                            FormParam param = (FormParam)annotation;
                            Parameter parameter = new Parameter();
                            parameter.setName(param.value());
                            parameter.setDescription(param.description());

                            Schema schema = new Schema<>();
                            schema.setType(parameterTypes[i].getSimpleName());
                            schema.setMinLength(param.minLength());
                            schema.setMaxLength(param.maxLength());
                            schema.setPattern(param.regex());
                            parameter.setSchema(schema);
                            parameter.setRequired(param.required());
                            operation.addParametersItem(parameter);
                        }
                        if (annotation.annotationType() == CookieParam.class) {
                            checkHasSet(hasSet, c, method, parameters[i]);
                            CookieParam param = (CookieParam)annotation;
                            Parameter parameter = new Parameter();
                            parameter.setName(param.value());
                            parameter.setDescription(param.description());

                            Schema schema = new Schema<>();
                            schema.setType(parameterTypes[i].getSimpleName());
                            schema.setMinLength(param.minLength());
                            schema.setMaxLength(param.maxLength());
                            schema.setPattern(param.regex());
                            parameter.setSchema(schema);
                            parameter.setRequired(param.required());
                            operation.addParametersItem(parameter);
                        }
                        if (annotation.annotationType() == FileParam.class) {
                            checkHasSet(hasSet, c, method, parameters[i]);
                            FileParam param = (FileParam)annotation;
                            Parameter parameter = new Parameter();
                            parameter.setName(param.value());
                            parameter.setDescription(param.description());

                            Schema schema = new Schema<>();
                            schema.setType(parameterTypes[i].getSimpleName());
                            parameter.setSchema(schema);
                            parameter.setRequired(param.required());
                            operation.addParametersItem(parameter);
                        }

                        // 解析body
                        if(annotation.annotationType() == Body.class) {
                            checkHasSet(hasSet, c, method, parameters[i]);

                            RequestBody requestBody = new RequestBody();
                            Content requestContext = new Content();
                            MediaType mediaType = new MediaType();

                            if (!modelMap.containsKey(parameterTypes[i].getName())) {
                                try {
                                    Schema model = ConverterFactory.getInstance().convert(parameterTypes[i].getName());
                                    modelMap.put(parameterTypes[i].getName(), model);
                                } catch (ObjectConverterException e) {
                                    throw new TollgeException("swagger生成schema失败:" + c.getName() + "." + method.getName() + "." + parameters[i].getName());
                                }
                            }
                            Schema refModel = new Schema<>();
                            refModel.$ref("#/components/schemas/" + parameterTypes[i].getName());
                            mediaType.schema(refModel);

                            requestContext.addMediaType("application/json", mediaType);
                            requestBody.setContent(requestContext);
                            operation.setRequestBody(requestBody);
                        }
                    }
                }

                // 设置返回值
                ApiResponses responses = new ApiResponses();
                ApiResponse response = new ApiResponse();
                Content responseContent = new Content();
                MediaType mediaType = new MediaType();
                Schema responseModel = new Schema<>();

                responseModel.addProperties(ResultFormat.CODE, new Schema().type("integer").description("返回code 200-成功"));
                responseModel.addProperties(ResultFormat.SUCCESS, new Schema().type("boolean").description("返回是否成功"));
                responseModel.addProperties(ResultFormat.MESSAGE, new Schema().type("string").description("返回信息"));

                // 实际返回的对象
                String returnTypeName = method.getGenericReturnType().getTypeName();
                returnTypeName = returnTypeName.substring(returnTypeName.indexOf("<") + 1, returnTypeName.length() - 1);
                if (!ConverterFactory.getInstance().isStandardDataType(returnTypeName) && !modelMap.containsKey(returnTypeName)) {
                    try {
                        Schema model = ConverterFactory.getInstance().convert(returnTypeName);
                        modelMap.put(returnTypeName, model);
                    } catch (ObjectConverterException e) {
                        throw new TollgeException("swagger生成schema失败:" + c.getName() + "." + method.getName() + "." + returnTypeName);
                    }
                }
                Schema refModel = new Schema<>();
                refModel.$ref("#/components/schemas/" + returnTypeName);
                responseModel.addProperties(ResultFormat.DATA, refModel);

                mediaType.schema(responseModel);
                responseContent.addMediaType("application/json", mediaType);
                response.content(responseContent);
                responses.addApiResponse("200", response);
                operation.setResponses(responses);


            }
        }

        // 设置components
        Components components = new Components();
        components.setSchemas(modelMap);
        openAPIDoc.components(components);

        // Serve the Swagger JSON spec out on /swagger
        router.get("/swagger").handler(res ->  res.response().setStatusCode(200).end(Json.pretty(openAPIDoc)));
        log.info("swagger服务监听端口:{}", swaggerPort);
        vertx.createHttpServer().requestHandler(router).listen(swaggerPort);
    }

    private void checkHasSet(boolean hasSet, Class<?> c, Method method, java.lang.reflect.Parameter parameter) {
        if (hasSet) {
            throw new TollgeException("解析swagger失败, 注解冲突:" + c.getName() + "." + method.getName() + "." + parameter.getName());
        }
    }

    private Operation fetchOperation(OpenAPI openAPI, Map<String, PathItem> pathItemMap, Http http, Path path) {
        PathItem pathItem = null;
        // 同一个url, 对应一个PathItem
        if (pathItemMap.containsKey(path.description())) {
            pathItem = pathItemMap.get(path.description());
        } else {
            pathItem = new PathItem();
            openAPI.path(http.value() + path.value(), pathItem);
            pathItemMap.put(path.description(), pathItem);
        }
        Operation operation = new Operation();
        operation.setDescription(path.description());
        switch (path.method()) {
            case GET: pathItem.setGet(operation); break;
            case POST: pathItem.setPost(operation); break;
            case PUT: pathItem.setPut(operation); break;
            case TRACE: pathItem.setTrace(operation); break;
            case DELETE: pathItem.setDelete(operation); break;
            case OPTIONS: pathItem.setOptions(operation);break;
            case ROUTE: pathItem.setPost(operation); pathItem.setGet(operation); break;
            default: break;
        }

        return operation;
    }

}
