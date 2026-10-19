package com.wesleyornellas.pokejava_api.adapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.GET, "/api/v1/pokemon", "/api/v1/pokemon/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/local/pokemon", "/api/v1/local/pokemon/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/local/pokemon/*/sync").permitAll()
						.requestMatchers(HttpMethod.PUT, "/api/v1/local/pokemon/*").permitAll()
						.anyRequest().authenticated())
				.httpBasic(Customizer.withDefaults())
				.build();
	}

}
