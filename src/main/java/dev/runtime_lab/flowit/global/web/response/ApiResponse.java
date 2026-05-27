package dev.runtime_lab.flowit.global.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"success", "data", "error", "extensions"})
public final class ApiResponse<T> {

	private final boolean success;
	private final T data;
	private final ApiError error;
	private final Map<String, Object> extensions;

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null, Map.of());
	}

	public static <T> ApiResponse<T> success(T data, Map<String, Object> extensions) {
		return new ApiResponse<>(true, data, null, copyExtensions(extensions));
	}

	public static ApiResponse<ApiEmptyData> empty() {
		return new ApiResponse<>(true, ApiEmptyData.empty(), null, Map.of());
	}

	public static ApiResponse<Object> error(ApiError error) {
		return new ApiResponse<>(false, null, error, Map.of());
	}

	public static ApiResponse<Object> error(ApiError error, Map<String, Object> extensions) {
		return new ApiResponse<>(false, null, error, copyExtensions(extensions));
	}

	ApiResponse<T> withExtensions(Map<String, Object> extensions) {
		if (extensions == null || extensions.isEmpty()) {
			return this;
		}

		Map<String, Object> mergedExtensions = new LinkedHashMap<>(this.extensions);
		mergedExtensions.putAll(extensions);

		return new ApiResponse<>(success, data, error, copyExtensions(mergedExtensions));
	}

	private static Map<String, Object> copyExtensions(Map<String, Object> extensions) {
		if (extensions == null || extensions.isEmpty()) {
			return Map.of();
		}

		return Collections.unmodifiableMap(new LinkedHashMap<>(extensions));
	}
}
