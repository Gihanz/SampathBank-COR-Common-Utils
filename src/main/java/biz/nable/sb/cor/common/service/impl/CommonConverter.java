package biz.nable.sb.cor.common.service.impl;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import biz.nable.sb.cor.common.exception.SystemException;
import biz.nable.sb.cor.common.utility.ErrorCode;

@Component
public class CommonConverter {

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	MessageSource messageSource;

	@SuppressWarnings("unchecked")
	public Map<String, Object> pojoToMap(Object object) {
		return objectMapper.convertValue(object, Map.class);
	}

	public <T> T mapToPojo(Map<String, Object> map, Class<T> toValueType) {
		return objectMapper.convertValue(map, toValueType);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> jsonToMap(String json) {
		try {
			return objectMapper.readValue(json, Map.class);
		} catch (IOException e) {
			throw new SystemException(
					messageSource.getMessage(ErrorCode.STRING_TO_MAP_ERROR, null, LocaleContextHolder.getLocale()), e,
					ErrorCode.STRING_TO_MAP_ERROR);
		}
	}
}
