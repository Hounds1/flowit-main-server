package dev.runtime_lab.flowit.global.web.response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResponseExtensionContextCleanupFilterTest {

	private final ResponseExtensionContextCleanupFilter filter = new ResponseExtensionContextCleanupFilter();

	@AfterEach
	void tearDown() {
		ResponseExtensionContext.clear();
	}

	@Test
	void clearsExtensionContextAfterRequest() throws Exception {
		ResponseExtensionContext.addProperty("traceId", "trace-1001");

		filter.doFilter(
			new MockHttpServletRequest(),
			new MockHttpServletResponse(),
			(request, response) -> {
			}
		);

		assertTrue(ResponseExtensionContext.consumeProperties().isEmpty());
	}
}
