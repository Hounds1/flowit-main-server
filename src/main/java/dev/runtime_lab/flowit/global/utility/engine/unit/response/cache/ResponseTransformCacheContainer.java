package dev.runtime_lab.flowit.global.utility.engine.unit.response.cache;

import dev.runtime_lab.flowit.global.utility.engine.unit.response.annotation.ResponseTransform;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ResponseTransformCacheContainer {

    private static final Map<Class<?>, Map<String, Field>> CLASS_FIELD_CACHE = new ConcurrentHashMap<>();

    private ResponseTransformCacheContainer() {
    }

    public static Map<String, Field> getClassFields(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz cannot be null");
        }

        return CLASS_FIELD_CACHE.computeIfAbsent(clazz, key -> {
            Map<String, Field> fieldMap = new HashMap<>();
            collectFields(key, fieldMap);
            return Collections.unmodifiableMap(fieldMap);
        });
    }

    public static boolean isContainedClass(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz cannot be null");
        }

        return CLASS_FIELD_CACHE.containsKey(clazz);
    }

    public static int containedSize() {
        return CLASS_FIELD_CACHE.size();
    }

    private static void collectFields(Class<?> clazz, Map<String, Field> fieldMap) {
        if (shouldIncludeInheritedFields(clazz)) {
            Class<?> current = clazz;

            while (current != null && current != Object.class) {
                putDeclaredFields(current, fieldMap);
                current = current.getSuperclass();
            }

            return;
        }

        putDeclaredFields(clazz, fieldMap);
    }

    private static boolean shouldIncludeInheritedFields(Class<?> clazz) {
        ResponseTransform responseTransform = clazz.getAnnotation(ResponseTransform.class);
        return responseTransform != null && responseTransform.includeInheritedFields();
    }

    private static void putDeclaredFields(Class<?> clazz, Map<String, Field> fieldMap) {
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            fieldMap.putIfAbsent(field.getName(), field);
        }
    }
}
