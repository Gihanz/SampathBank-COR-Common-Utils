//package biz.nable.sb.cor.common.config;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.support.ReloadableResourceBundleMessageSource;
//import org.springframework.http.client.BufferingClientHttpRequestFactory;
//import org.springframework.http.client.ClientHttpRequestInterceptor;
//import org.springframework.http.client.SimpleClientHttpRequestFactory;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.filter.CommonsRequestLoggingFilter;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import biz.nable.sb.cor.common.filter.RequestResponseLoggingInterceptor;
//
//@Configuration
//public class CommonConfig {
//
//	@Bean
//	public RestTemplate restTemplate() {
//
//		RestTemplate restTemplate = new RestTemplate(
//				new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
//		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
//		interceptors.add(new RequestResponseLoggingInterceptor());
//		restTemplate.setInterceptors(interceptors);
//
//		return restTemplate;
//	}
//
//	@Bean
//	public ObjectMapper objectMapper() {
//		return new ObjectMapper();
//	}
//
//	@Bean
//	public ReloadableResourceBundleMessageSource messageSource() {
//		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
//		messageSource.setBasenames("classpath:messages");
//		return messageSource;
//	}
//
//	@Bean
//	public CommonsRequestLoggingFilter logFilter() {
//		CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
//		filter.setIncludeQueryString(true);
//		filter.setIncludeQueryString(true);
//		filter.setIncludePayload(true);
//		filter.setMaxPayloadLength(10000);
//		filter.setIncludeHeaders(true);
//		filter.setAfterMessagePrefix("REQUEST DATA : ");
//		return filter;
//	}
//}
