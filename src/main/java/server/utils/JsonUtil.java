package server.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        //当map的value为空时，不参与序列化
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        //反序列化时，出现不存在字段的序列化内容时不抛出异常
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static <T> T readValue(String content, Class<T> valueType) {
        try {
            if (StringUtils.hasText(content)) {
                return JsonUtil.mapper().readValue(content, valueType);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readValue(String content, TypeReference<T> valueTypeRef) {
        try {
            if (StringUtils.hasText(content)) {
                return JsonUtil.mapper().readValue(content, valueTypeRef);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String writeValueAsString(Object object) {
        try {
            return JsonUtil.mapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
