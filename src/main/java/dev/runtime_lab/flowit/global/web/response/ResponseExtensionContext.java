package dev.runtime_lab.flowit.global.web.response;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseExtensionContext {

	private static final ThreadLocal<Map<String, Object>> EXTENSIONS =
		ThreadLocal.withInitial(LinkedHashMap::new);

	public static void addProperty(String key, Object value) {
		if (key == null || key.isBlank()) {
			throw new IllegalArgumentException("Response extension key must not be blank.");
		}

		EXTENSIONS.get().put(key, value);
	}

	static Map<String, Object> consumeProperties() {
		try {
			Map<String, Object> extensions = EXTENSIONS.get();
			if (extensions.isEmpty()) {
				return Map.of();
			}

			return Collections.unmodifiableMap(new LinkedHashMap<>(extensions));
		}
		finally {
			clear();
		}
	}

	public static void clear() {
		EXTENSIONS.remove();
	}
}
