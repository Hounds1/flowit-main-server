package dev.runtime_lab.flowit.global.utility.engine.unit.response;

import dev.runtime_lab.flowit.global.utility.engine.unit.response.annotation.IgnoreTransform;
import dev.runtime_lab.flowit.global.utility.engine.unit.response.annotation.ResponseTransform;
import dev.runtime_lab.flowit.global.utility.engine.unit.response.annotation.TransformOverride;
import dev.runtime_lab.flowit.global.utility.engine.unit.response.executor.ResponseTransformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResponseUnitTest {

    private final ResponseUnit responseUnit = new TestResponseUnit();

    @Test
    void transformCopiesMatchingFields() {
        BasicResponse response = responseUnit.transform(new BasicOrigin("홍길동", 7), BasicResponse.class);

        assertEquals("홍길동", response.name);
        assertEquals(7, response.age);
    }

    @Test
    void transformerForSupportsFluentTransform() {
        BasicResponse response = responseUnit.<BasicOrigin, BasicResponse>transformerFor(BasicResponse.class)
            .withOrigin(new BasicOrigin("김철수", 9))
            .transform();

        assertEquals("김철수", response.name);
        assertEquals(9, response.age);
    }

    @Test
    void transformOverrideUsesOriginFieldName() {
        OverrideResponse response = responseUnit.transform(
            new OverrideOrigin("이영희"),
            OverrideResponse.class
        );

        assertEquals("이영희", response.name);
    }

    @Test
    void ignoreTransformSkipsField() {
        IgnoreResponse response = responseUnit.transform(
            new IgnoreOrigin("홍길동", "secret"),
            IgnoreResponse.class
        );

        assertEquals("홍길동", response.name);
        assertNull(response.hidden);
    }

    @Test
    void includeInheritedFieldsCopiesParentFields() {
        ChildResponse response = responseUnit.transform(
            new ChildOrigin("parent", "child"),
            ChildResponse.class
        );

        assertEquals("parent", response.parentValue());
        assertEquals("child", response.childValue);
    }

    @Test
    void isCachedClassReflectsFieldCacheState() {
        assertFalse(responseUnit.isCachedClass(CacheProbeResponse.class));

        responseUnit.transform(new CacheProbeOrigin("cached"), CacheProbeResponse.class);

        assertTrue(responseUnit.isCachedClass(CacheProbeResponse.class));
        assertTrue(responseUnit.isCachedClass(CacheProbeOrigin.class));
    }

    @Test
    void rejectsUnannotatedReturnClass() {
        assertThrows(
            IllegalArgumentException.class,
            () -> responseUnit.transformerFor(UnannotatedResponse.class)
        );
    }

    @Test
    void rejectsNullOrigin() {
        ResponseTransformer<BasicOrigin, BasicResponse> transformer = responseUnit.transformerFor(BasicResponse.class);

        assertThrows(IllegalArgumentException.class, () -> transformer.withOrigin(null));
    }

    @Test
    void rejectsTransformWithoutOrigin() {
        ResponseTransformer<BasicOrigin, BasicResponse> transformer = responseUnit.transformerFor(BasicResponse.class);

        assertThrows(IllegalStateException.class, transformer::transform);
    }

    @Test
    void transformerCannotBeReusedAfterTransform() {
        ResponseTransformer<BasicOrigin, BasicResponse> transformer = responseUnit.transformerFor(BasicResponse.class);

        transformer.withOrigin(new BasicOrigin("홍길동", 7)).transform();

        assertThrows(IllegalStateException.class, () -> transformer.withOrigin(new BasicOrigin("김철수", 9)));
        assertThrows(IllegalStateException.class, transformer::transform);
    }

    @Test
    void rejectsNotAssignableField() {
        assertThrows(
            IllegalArgumentException.class,
            () -> responseUnit.transform(new MismatchOrigin("not-number"), MismatchResponse.class)
        );
    }

    static class BasicOrigin {
        private final String name;
        private final int age;

        private BasicOrigin(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    @ResponseTransform
    static class BasicResponse {
        private String name;
        private int age;

        private BasicResponse() {
        }
    }

    static class OverrideOrigin {
        private final String sourceName;

        private OverrideOrigin(String sourceName) {
            this.sourceName = sourceName;
        }
    }

    @ResponseTransform
    static class OverrideResponse {
        @TransformOverride("sourceName")
        private String name;

        private OverrideResponse() {
        }
    }

    static class IgnoreOrigin {
        private final String name;
        private final String hidden;

        private IgnoreOrigin(String name, String hidden) {
            this.name = name;
            this.hidden = hidden;
        }
    }

    @ResponseTransform
    static class IgnoreResponse {
        private String name;

        @IgnoreTransform
        private String hidden;

        private IgnoreResponse() {
        }
    }

    static class ChildOrigin {
        private final String parentValue;
        private final String childValue;

        private ChildOrigin(String parentValue, String childValue) {
            this.parentValue = parentValue;
            this.childValue = childValue;
        }
    }

    static class ParentResponse {
        private String parentValue;

        String parentValue() {
            return parentValue;
        }
    }

    @ResponseTransform(includeInheritedFields = true)
    static class ChildResponse extends ParentResponse {
        private String childValue;

        private ChildResponse() {
        }
    }

    static class CacheProbeOrigin {
        private final String value;

        private CacheProbeOrigin(String value) {
            this.value = value;
        }
    }

    @ResponseTransform
    static class CacheProbeResponse {
        private String value;

        private CacheProbeResponse() {
        }
    }

    static class UnannotatedResponse {
    }

    static class MismatchOrigin {
        private final String count;

        private MismatchOrigin(String count) {
            this.count = count;
        }
    }

    @ResponseTransform
    static class MismatchResponse {
        private int count;

        private MismatchResponse() {
        }
    }

    private static class TestResponseUnit extends ResponseUnit {
        protected TestResponseUnit() {
        }
    }
}
