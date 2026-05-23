package dev.runtime_lab.flowit.global.utility.engine.unit.response.executor;

import dev.runtime_lab.flowit.global.utility.engine.unit.response.annotation.IgnoreTransform;
import dev.runtime_lab.flowit.global.utility.engine.unit.response.annotation.ResponseTransform;
import dev.runtime_lab.flowit.global.utility.engine.unit.response.annotation.TransformOverride;
import dev.runtime_lab.flowit.global.utility.engine.unit.response.cache.ResponseTransformCacheContainer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResponseTransformer<T, R> {

    private final Class<R> returnClass;
    private final AtomicBoolean executed = new AtomicBoolean(false);
    private T origin;

    protected ResponseTransformer(Class<R> returnClass) {
        if (returnClass == null) {
            throw new IllegalArgumentException("returnClass cannot be null");
        }

        if (!returnClass.isAnnotationPresent(ResponseTransform.class)) {
            throw new IllegalArgumentException("returnClass must be annotated with ResponseTransform");
        }

        this.returnClass = returnClass;
    }

    public ResponseTransformer<T, R> withOrigin(T origin) {
        ensureNotExecuted();

        if (origin == null) {
            throw new IllegalArgumentException("origin cannot be null");
        }

        this.origin = origin;
        return this;
    }

    public R transform() {
        markExecuted();

        try {
            if (origin == null) {
                throw new IllegalStateException("origin cannot be null");
            }

            Map<String, Field> returnClassFields = ResponseTransformCacheContainer.getClassFields(returnClass);
            Map<String, Field> originClassFields = ResponseTransformCacheContainer.getClassFields(origin.getClass());
            Constructor<R> declaredConstructor = this.returnClass.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);

            R response = declaredConstructor.newInstance();

            for (Map.Entry<String, Field> entry : returnClassFields.entrySet()) {
                String returnFieldName = entry.getKey();
                Field returnField = entry.getValue();

                if (returnField.isAnnotationPresent(IgnoreTransform.class)) {
                    continue;
                }

                String originFieldName = returnField.isAnnotationPresent(TransformOverride.class)
                    ? returnField.getAnnotation(TransformOverride.class).value()
                    : returnFieldName;

                mapField(response, returnField, originClassFields.get(originFieldName));
            }

            return response;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                "An error has occurred while transforming response. originType="
                    + origin.getClass().getName()
                    + ", returnType="
                    + returnClass.getName(),
                e
            );
        } finally {
            clearState();
        }
    }

    private void mapField(R response, Field returnField, Field originField) throws IllegalAccessException {
        if (originField == null) {
            return;
        }

        Object value = originField.get(this.origin);

        if (value == null) {
            return;
        }

        validateAssignable(returnField, originField, value);
        returnField.set(response, value);
    }

    private void validateAssignable(Field returnField, Field originField, Object value) {
        Class<?> returnFieldType = returnField.getType();

        if (returnFieldType.isPrimitive() && isPrimitiveWrapper(returnFieldType, value.getClass())) {
            return;
        }

        if (!returnFieldType.isPrimitive() && returnFieldType.isAssignableFrom(value.getClass())) {
            return;
        }

        throw new IllegalArgumentException(
            "Not assignable response field detected. returnField="
                + returnField.getName()
                + ", returnType="
                + returnFieldType.getName()
                + ", originField="
                + originField.getName()
                + ", originType="
                + value.getClass().getName()
        );
    }

    private boolean isPrimitiveWrapper(Class<?> primitiveType, Class<?> valueType) {
        return (primitiveType == boolean.class && valueType == Boolean.class)
            || (primitiveType == byte.class && valueType == Byte.class)
            || (primitiveType == short.class && valueType == Short.class)
            || (primitiveType == int.class && valueType == Integer.class)
            || (primitiveType == long.class && valueType == Long.class)
            || (primitiveType == float.class && valueType == Float.class)
            || (primitiveType == double.class && valueType == Double.class)
            || (primitiveType == char.class && valueType == Character.class);
    }

    private void markExecuted() {
        if (!this.executed.compareAndSet(false, true)) {
            throw new IllegalStateException("This transformer cannot be reused.");
        }
    }

    private void ensureNotExecuted() {
        if (this.executed.get()) {
            throw new IllegalStateException("This transformer has already been executed.");
        }
    }

    private void clearState() {
        this.origin = null;
    }
}
