package dev.runtime_lab.flowit.global.web.response;

import dev.runtime_lab.flowit.docs.DocsPreviewController;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "dev.runtime_lab.flowit")
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		Class<?> declaringClass = returnType.getContainingClass();

		return !DocsPreviewController.class.isAssignableFrom(declaringClass);
	}

	@Override
	public Object beforeBodyWrite(
		Object body,
		MethodParameter returnType,
		MediaType selectedContentType,
		Class<? extends HttpMessageConverter<?>> selectedConverterType,
		ServerHttpRequest request,
		ServerHttpResponse response
	) {
		if (!isJsonResponse(selectedContentType)) {
			return body;
		}

		Map<String, Object> extensions = ResponseExtensionContext.consumeProperties();
		if (body instanceof ApiResponse<?> apiResponse) {
			return apiResponse.withExtensions(extensions);
		}

		if (isAlreadyHandled(body)) {
			return body;
		}

		if (body == null) {
			return ApiResponse.empty().withExtensions(extensions);
		}

		if (body instanceof Collection<?> collection) {
			List<?> items = List.copyOf(collection);
			return ApiResponse.success(ApiListData.of(items, items.size()), extensions);
		}

		return ApiResponse.success(body, extensions);
	}

	private boolean isAlreadyHandled(Object body) {
		return body instanceof Resource
			|| body instanceof byte[]
			|| body instanceof ProblemDetail;
	}

	private boolean isJsonResponse(MediaType mediaType) {
		if (mediaType == null) {
			return true;
		}

		return MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)
			|| mediaType.getSubtype().endsWith("+json");
	}
}
