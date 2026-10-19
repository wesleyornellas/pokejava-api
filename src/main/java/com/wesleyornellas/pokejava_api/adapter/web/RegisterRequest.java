package com.wesleyornellas.pokejava_api.adapter.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record RegisterRequest(
		@NotBlank @Size(min = 3, max = 50) String username,
		@NotBlank @Size(min = 8, max = 72) String password,
		@NotBlank @Size(min = 1, max = 100) String displayName) {
}
