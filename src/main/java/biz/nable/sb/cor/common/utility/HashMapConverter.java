package biz.nable.sb.cor.common.utility;

import java.io.IOException;
import java.util.Map;

import javax.persistence.AttributeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HashMapConverter implements AttributeConverter<Map<String, Object>, String> {
	@Autowired
	ObjectMapper objectMapper;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public String convertToDatabaseColumn(Map<String, Object> attribute) {
		String attributeJson = null;
		try {
			attributeJson = objectMapper.writeValueAsString(attribute);
		} catch (final JsonProcessingException e) {
			logger.error("JSON writing error", e);
		}

		return attributeJson;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> convertToEntityAttribute(String dbData) {
		Map<String, Object> attribute = null;
		try {
			attribute = objectMapper.readValue(dbData, Map.class);
		} catch (final IOException e) {
			logger.error("JSON reading error", e);
		}

		return attribute;
	}

}
