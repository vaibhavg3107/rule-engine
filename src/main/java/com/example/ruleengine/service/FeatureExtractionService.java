package com.example.ruleengine.service;

import com.example.ruleengine.entity.Feature;
import com.example.ruleengine.entity.enums.FeatureType;
import com.example.ruleengine.exception.ValidationException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureExtractionService {

    public Map<String, Object> extractFeatures(List<Feature> features, Map<String, Object> inputData) {
        Map<String, Object> extractedFeatures = new HashMap<>();
        List<String> missingFeatures = new java.util.ArrayList<>();
        
        for (Feature feature : features) {
            try {
                Object value = extractFeatureValue(feature, inputData);
                if (value == null && feature.getDefaultValue() == null) {
                    missingFeatures.add(feature.getName());
                } else {
                    extractedFeatures.put(feature.getName(), value);
                }
            } catch (ValidationException e) {
                if (feature.getDefaultValue() != null) {
                    extractedFeatures.put(feature.getName(), feature.getDefaultValue());
                } else {
                    missingFeatures.add(feature.getName());
                }
            }
        }
        
        if (!missingFeatures.isEmpty()) {
            throw new ValidationException("Missing required input for feature(s): " + String.join(", ", missingFeatures));
        }
        
        return extractedFeatures;
    }

    public Object extractFeatureValue(Feature feature, Map<String, Object> inputData) {
        Map<String, Object> extractionConfig = feature.getExtractionConfig();
        String type = (String) extractionConfig.get("type");
        
        if ("JSON_PATH".equals(type)) {
            return extractJsonPath(feature, inputData, extractionConfig);
        } else if ("DIRECT".equals(type)) {
            return extractDirect(feature, inputData, extractionConfig);
        } else {
            throw new ValidationException("Unknown extraction type: " + type);
        }
    }

    private Object extractJsonPath(Feature feature, Map<String, Object> inputData, Map<String, Object> config) {
        String path = (String) config.get("path");
        if (path == null) {
            throw new ValidationException("JSON_PATH extraction requires 'path' in extractionConfig");
        }
        
        try {
            Object rawValue = JsonPath.read(inputData, path);
            return convertToFeatureType(rawValue, feature.getFeatureType());
        } catch (PathNotFoundException e) {
            if (feature.getDefaultValue() != null) {
                return feature.getDefaultValue();
            }
            throw new ValidationException("Path not found: " + path);
        }
    }

    private Object extractDirect(Feature feature, Map<String, Object> inputData, Map<String, Object> config) {
        String field = (String) config.get("field");
        if (field == null) {
            throw new ValidationException("DIRECT extraction requires 'field' in extractionConfig");
        }
        
        Object rawValue = inputData.get(field);
        if (rawValue == null && feature.getDefaultValue() != null) {
            return feature.getDefaultValue();
        }
        
        return convertToFeatureType(rawValue, feature.getFeatureType());
    }

    private Object convertToFeatureType(Object rawValue, FeatureType featureType) {
        if (rawValue == null) {
            return null;
        }
        
        switch (featureType) {
            case NUMERIC:
                return convertToNumeric(rawValue);
            case STRING:
                return rawValue.toString();
            case BOOLEAN:
                return convertToBoolean(rawValue);
            case DATE:
                return convertToDate(rawValue);
            case LIST:
                if (rawValue instanceof List) {
                    return rawValue;
                }
                throw new ValidationException("Expected list but got: " + rawValue.getClass().getSimpleName());
            default:
                return rawValue;
        }
    }

    private Number convertToNumeric(Object value) {
        if (value instanceof Number) {
            return (Number) value;
        }
        if (value instanceof String) {
            try {
                String strValue = (String) value;
                if (strValue.contains(".")) {
                    return Double.parseDouble(strValue);
                }
                return Long.parseLong(strValue);
            } catch (NumberFormatException e) {
                throw new ValidationException("Cannot convert to number: " + value);
            }
        }
        throw new ValidationException("Cannot convert to number: " + value);
    }

    private Boolean convertToBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        throw new ValidationException("Cannot convert to boolean: " + value);
    }

    private LocalDate convertToDate(Object value) {
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof String) {
            try {
                return LocalDate.parse((String) value);
            } catch (DateTimeParseException e) {
                throw new ValidationException("Cannot parse date: " + value);
            }
        }
        throw new ValidationException("Cannot convert to date: " + value);
    }
}
