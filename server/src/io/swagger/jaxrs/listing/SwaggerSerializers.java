package io.swagger.jaxrs.listing;

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import io.swagger.util.Yaml;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Singleton
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "application/yaml" })
public class SwaggerSerializers implements MessageBodyWriter<Swagger> {
    static boolean prettyPrint = false;
    Logger LOGGER = LoggerFactory.getLogger(SwaggerSerializers.class);

    public static void setPrettyPrint(boolean shouldPrettyPrint) {
        SwaggerSerializers.prettyPrint = shouldPrettyPrint;
    }

    @Override
    public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Swagger.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Swagger data, Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Swagger data, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> headers, OutputStream out) throws IOException {
        /**
         * Swagger UI does not support multiple operations from different paths yet the same
         * operation ID under a single tag. To get around this, we build up a map of all operations
         * under each tag, and those with the same operation ID get updated.
         */
        Map<String, Map<String, List<Operation>>> tagMap = new HashMap<String, Map<String, List<Operation>>>();

        for (Path path : data.getPaths().values()) {
            for (Operation operation : path.getOperations()) {
                for (String tag : operation.getTags()) {
                    Map<String, List<Operation>> operationMap = tagMap.get(tag);
                    if (operationMap == null) {
                        operationMap = new HashMap<String, List<Operation>>();
                        tagMap.put(tag, operationMap);
                    }

                    List<Operation> list = operationMap.get(operation.getOperationId());
                    if (list == null) {
                        list = new ArrayList<Operation>();
                        operationMap.put(operation.getOperationId(), list);
                    }

                    list.add(operation);
                }
            }
        }

        for (Map<String, List<Operation>> operationMap : tagMap.values()) {
            for (Entry<String, List<Operation>> entry : operationMap.entrySet()) {
                String operationId = entry.getKey();
                List<Operation> operations = entry.getValue();

                if (operations.size() > 1) {
                    for (int i = 0; i < operations.size(); i++) {
                        operations.get(i).setOperationId(operationId + "_" + i);
                    }
                }
            }
        }

        if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
            if (prettyPrint) {
                out.write(Json.pretty().writeValueAsString(data).getBytes("utf-8"));
            } else {
                out.write(Json.mapper().writeValueAsString(data).getBytes("utf-8"));
            }
        } else if (mediaType.toString().startsWith("application/yaml")) {
            headers.remove("Content-Type");
            headers.add("Content-Type", "application/yaml");
            out.write(Yaml.mapper().writeValueAsString(data).getBytes("utf-8"));
        } else if (mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE)) {
            headers.remove("Content-Type");
            headers.add("Content-Type", "application/json");
            out.write(Json.mapper().writeValueAsString(data).getBytes("utf-8"));
        }
    }
}
