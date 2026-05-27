package dev.runtime_lab.flowit.global.web.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiListData<T> {

	private final List<T> items;
	private final long totalCount;

	public static <T> ApiListData<T> of(List<T> items, long totalCount) {
		return new ApiListData<>(List.copyOf(items), totalCount);
	}
}
