package com.wesleyornellas.pokejava_api.domain;

import java.util.List;

public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

	public PageResult {
		content = List.copyOf(content);
	}

	public int totalPages() {
		return (int) Math.ceilDiv(totalElements, size);
	}

}
