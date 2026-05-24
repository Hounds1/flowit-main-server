package dev.runtime_lab.flowit.global.utility.engine.unit.response;

import dev.runtime_lab.flowit.global.utility.engine.unit.response.cache.ResponseTransformCacheContainer;
import dev.runtime_lab.flowit.global.utility.engine.unit.response.executor.ResponseTransformer;

public class ResponseUnit {

    protected ResponseUnit() {
    }

    public boolean isCachedClass(Class<?> clazz) {
        return ResponseTransformCacheContainer.isContainedClass(clazz);
    }

    public <T, R> R transform(T origin, Class<R> returnClass) {
        return this.<T, R>transformerFor(returnClass)
            .withOrigin(origin)
            .transform();
    }

    public <T, R> ResponseTransformer<T, R> transformerFor(Class<R> returnClass) {
        return ResponseTransformerDelegate.getInstance(returnClass);
    }

    private static class ResponseTransformerDelegate<T, R> extends ResponseTransformer<T, R> {
        private ResponseTransformerDelegate(Class<R> returnClass) {
            super(returnClass);
        }

        private static <T, R> ResponseTransformerDelegate<T, R> getInstance(Class<R> returnClass) {
            return new ResponseTransformerDelegate<>(returnClass);
        }
    }
}
