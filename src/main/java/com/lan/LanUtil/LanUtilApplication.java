package com.lan.LanUtil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.DelegatingFilterProxy;

import com.lan.utils.MySessionFilter;

@SpringBootApplication
public class LanUtilApplication {

	public static void main(String[] args) {
		SpringApplication.run(LanUtilApplication.class, args);
	}
	
	@Bean
	public FilterRegistrationBean testFilterRegistration() {
		FilterRegistrationBean registration = new FilterRegistrationBean(new MySessionFilter());
		registration.addUrlPatterns("/test"); //
		registration.addInitParameter("paramName", "paramValue"); //
		registration.setName("MySessionFilter");
		registration.setOrder(1);
		return registration;
	}
}
