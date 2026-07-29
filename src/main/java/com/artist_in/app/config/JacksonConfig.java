package com.artist_in.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;

@Configuration
public class JacksonConfig {
	@Bean
	public Hibernate6Module hibernate6Module() {
		Hibernate6Module module = new Hibernate6Module();
		// Force-load lazy proxies instead of just skipping them as null
		module.enable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
		return module;
	}
}
