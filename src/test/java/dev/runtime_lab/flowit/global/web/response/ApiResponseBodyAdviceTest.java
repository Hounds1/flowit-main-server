package dev.runtime_lab.flowit.global.web.response;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiResponseBodyAdviceTest {

	private final ApiResponseBodyAdvice advice = new ApiResponseBodyAdvice();

	@AfterEach
	void tearDown() {
		ResponseExtensionContext.clear();
	}

	@Test
	void wrapsObjectResponse() {
		SampleData sampleData = new SampleData(1001L, "Sample Asset");

		Object wrapped = advice.beforeBodyWrite(
			sampleData,
			null,
			MediaType.APPLICATION_JSON,
			null,
			null,
			null
		);

		ApiResponse<?> response = assertInstanceOf(ApiResponse.class, wrapped);
		assertTrue(response.isSuccess());
		assertSame(sampleData, response.getData());
		assertNull(response.getError());
		assertTrue(response.getExtensions().isEmpty());
	}

	@Test
	void wrapsObjectResponseWithExtensionContextProperties() {
		SampleData sampleData = new SampleData(1001L, "Sample Asset");
		ResponseExtensionContext.addProperty("traceId", "trace-1001");

		Object wrapped = advice.beforeBodyWrite(
			sampleData,
			null,
			MediaType.APPLICATION_JSON,
			null,
			null,
			null
		);

		ApiResponse<?> response = assertInstanceOf(ApiResponse.class, wrapped);

		assertEquals("trace-1001", response.getExtensions().get("traceId"));
	}

	@Test
	void wrapsCollectionResponseWithListData() {
		List<SampleData> samples = List.of(
			new SampleData(1001L, "Sample Asset"),
			new SampleData(1002L, "Another Asset")
		);

		Object wrapped = advice.beforeBodyWrite(
			samples,
			null,
			MediaType.APPLICATION_JSON,
			null,
			null,
			null
		);

		ApiResponse<?> response = assertInstanceOf(ApiResponse.class, wrapped);
		ApiListData<?> listData = assertInstanceOf(ApiListData.class, response.getData());

		assertEquals(samples, listData.getItems());
		assertEquals(2L, listData.getTotalCount());
	}

	@Test
	void wrapsNullResponseWithEmptyData() {
		Object wrapped = advice.beforeBodyWrite(
			null,
			null,
			MediaType.APPLICATION_JSON,
			null,
			null,
			null
		);

		ApiResponse<?> response = assertInstanceOf(ApiResponse.class, wrapped);

		assertTrue(response.isSuccess());
		assertInstanceOf(ApiEmptyData.class, response.getData());
	}

	@Test
	void doesNotWrapApiResponseAgain() {
		ApiResponse<SampleData> original = ApiResponse.success(new SampleData(1001L, "Sample Asset"));

		Object wrapped = advice.beforeBodyWrite(
			original,
			null,
			MediaType.APPLICATION_JSON,
			null,
			null,
			null
		);

		assertSame(original, wrapped);
	}

	@Test
	void addsExtensionContextPropertiesToExistingApiResponse() {
		ApiResponse<SampleData> original = ApiResponse.success(new SampleData(1001L, "Sample Asset"));
		ResponseExtensionContext.addProperty("traceId", "trace-1001");

		Object wrapped = advice.beforeBodyWrite(
			original,
			null,
			MediaType.APPLICATION_JSON,
			null,
			null,
			null
		);

		ApiResponse<?> response = assertInstanceOf(ApiResponse.class, wrapped);

		assertEquals("trace-1001", response.getExtensions().get("traceId"));
	}

	@Test
	void clearsExtensionContextAfterResponseIsWrapped() {
		ResponseExtensionContext.addProperty("traceId", "trace-1001");

		advice.beforeBodyWrite(
			new SampleData(1001L, "Sample Asset"),
			null,
			MediaType.APPLICATION_JSON,
			null,
			null,
			null
		);

		Object wrapped = advice.beforeBodyWrite(
			new SampleData(1002L, "Another Asset"),
			null,
			MediaType.APPLICATION_JSON,
			null,
			null,
			null
		);

		ApiResponse<?> response = assertInstanceOf(ApiResponse.class, wrapped);

		assertTrue(response.getExtensions().isEmpty());
	}

	private record SampleData(Long id, String name) {
	}
}
