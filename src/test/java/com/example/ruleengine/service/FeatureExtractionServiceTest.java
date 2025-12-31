package com.example.ruleengine.service;

import com.example.ruleengine.entity.Feature;
import com.example.ruleengine.entity.enums.FeatureType;
import com.example.ruleengine.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FeatureExtractionServiceTest {

    private FeatureExtractionService featureExtractionService;

    @BeforeEach
    void setUp() {
        featureExtractionService = new FeatureExtractionService();
    }

    private Feature createFeature(String name, FeatureType type, String extractionType, String path) {
        Map<String, Object> extractionConfig = new HashMap<>();
        extractionConfig.put("type", extractionType);
        if ("JSON_PATH".equals(extractionType)) {
            extractionConfig.put("path", path);
        } else if ("DIRECT".equals(extractionType)) {
            extractionConfig.put("field", path);
        }

        return Feature.builder()
                .id(UUID.randomUUID())
                .name(name)
                .featureType(type)
                .extractionConfig(extractionConfig)
                .build();
    }

    @Nested
    @DisplayName("JSON Path Extraction")
    class JsonPathExtraction {

        @Test
        @DisplayName("Should extract numeric value from nested JSON")
        void testExtractNumericFromNestedJson() {
            Feature feature = createFeature("age", FeatureType.NUMERIC, "JSON_PATH", "$.user.age");
            
            Map<String, Object> inputData = new HashMap<>();
            Map<String, Object> user = new HashMap<>();
            user.put("age", 25);
            user.put("name", "John");
            inputData.put("user", user);

            Object result = featureExtractionService.extractFeatureValue(feature, inputData);
            
            assertNotNull(result);
            assertEquals(25, ((Number) result).intValue());
        }

        @Test
        @DisplayName("Should extract string value from JSON")
        void testExtractStringFromJson() {
            Feature feature = createFeature("name", FeatureType.STRING, "JSON_PATH", "$.user.name");
            
            Map<String, Object> inputData = new HashMap<>();
            Map<String, Object> user = new HashMap<>();
            user.put("name", "John Doe");
            inputData.put("user", user);

            Object result = featureExtractionService.extractFeatureValue(feature, inputData);
            
            assertEquals("John Doe", result);
        }

        @Test
        @DisplayName("Should extract boolean value from JSON")
        void testExtractBooleanFromJson() {
            Feature feature = createFeature("active", FeatureType.BOOLEAN, "JSON_PATH", "$.user.active");
            
            Map<String, Object> inputData = new HashMap<>();
            Map<String, Object> user = new HashMap<>();
            user.put("active", true);
            inputData.put("user", user);

            Object result = featureExtractionService.extractFeatureValue(feature, inputData);
            
            assertEquals(true, result);
        }

        @Test
        @DisplayName("Should extract list value from JSON")
        void testExtractListFromJson() {
            Feature feature = createFeature("tags", FeatureType.LIST, "JSON_PATH", "$.user.tags");
            
            Map<String, Object> inputData = new HashMap<>();
            Map<String, Object> user = new HashMap<>();
            user.put("tags", Arrays.asList("premium", "verified"));
            inputData.put("user", user);

            Object result = featureExtractionService.extractFeatureValue(feature, inputData);
            
            assertInstanceOf(List.class, result);
            assertEquals(2, ((List<?>) result).size());
        }

        @Test
        @DisplayName("Should use default value when path not found")
        void testDefaultValueWhenPathNotFound() {
            Feature feature = createFeature("score", FeatureType.NUMERIC, "JSON_PATH", "$.user.score");
            feature.setDefaultValue(100);
            
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("user", new HashMap<>());

            Object result = featureExtractionService.extractFeatureValue(feature, inputData);
            
            assertEquals(100, result);
        }
    }

    @Nested
    @DisplayName("Direct Field Extraction")
    class DirectFieldExtraction {

        @Test
        @DisplayName("Should extract value directly from top-level field")
        void testExtractDirectField() {
            Feature feature = createFeature("amount", FeatureType.NUMERIC, "DIRECT", "amount");
            
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("amount", 1000.50);

            Object result = featureExtractionService.extractFeatureValue(feature, inputData);
            
            assertEquals(1000.50, ((Number) result).doubleValue());
        }

        @Test
        @DisplayName("Should use default value when field not found")
        void testDefaultValueForDirectField() {
            Feature feature = createFeature("amount", FeatureType.NUMERIC, "DIRECT", "amount");
            feature.setDefaultValue(0);
            
            Map<String, Object> inputData = new HashMap<>();

            Object result = featureExtractionService.extractFeatureValue(feature, inputData);
            
            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("Type Conversion")
    class TypeConversion {

        @Test
        @DisplayName("Should convert string to numeric")
        void testStringToNumeric() {
            Feature feature = createFeature("amount", FeatureType.NUMERIC, "JSON_PATH", "$.amount");
            
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("amount", "123.45");

            Object result = featureExtractionService.extractFeatureValue(feature, inputData);
            
            assertEquals(123.45, ((Number) result).doubleValue());
        }

        @Test
        @DisplayName("Should convert string to boolean")
        void testStringToBoolean() {
            Feature feature = createFeature("active", FeatureType.BOOLEAN, "JSON_PATH", "$.active");
            
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("active", "true");

            Object result = featureExtractionService.extractFeatureValue(feature, inputData);
            
            assertEquals(true, result);
        }

        @Test
        @DisplayName("Should convert number to boolean (non-zero = true)")
        void testNumberToBoolean() {
            Feature feature = createFeature("flag", FeatureType.BOOLEAN, "JSON_PATH", "$.flag");
            
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("flag", 1);

            Object result = featureExtractionService.extractFeatureValue(feature, inputData);
            
            assertEquals(true, result);
        }
    }

    @Nested
    @DisplayName("Multiple Feature Extraction")
    class MultipleFeatureExtraction {

        @Test
        @DisplayName("Should extract multiple features from input data")
        void testExtractMultipleFeatures() {
            Feature ageFeature = createFeature("age", FeatureType.NUMERIC, "JSON_PATH", "$.user.age");
            Feature nameFeature = createFeature("name", FeatureType.STRING, "JSON_PATH", "$.user.name");
            Feature scoreFeature = createFeature("score", FeatureType.NUMERIC, "JSON_PATH", "$.score");
            
            Map<String, Object> inputData = new HashMap<>();
            Map<String, Object> user = new HashMap<>();
            user.put("age", 30);
            user.put("name", "Jane");
            inputData.put("user", user);
            inputData.put("score", 85);

            Map<String, Object> result = featureExtractionService.extractFeatures(
                    Arrays.asList(ageFeature, nameFeature, scoreFeature), inputData);
            
            assertEquals(3, result.size());
            assertEquals(30, ((Number) result.get("age")).intValue());
            assertEquals("Jane", result.get("name"));
            assertEquals(85, ((Number) result.get("score")).intValue());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should throw exception for unknown extraction type")
        void testUnknownExtractionType() {
            Map<String, Object> extractionConfig = new HashMap<>();
            extractionConfig.put("type", "UNKNOWN");
            
            Feature feature = Feature.builder()
                    .id(UUID.randomUUID())
                    .name("test")
                    .featureType(FeatureType.STRING)
                    .extractionConfig(extractionConfig)
                    .build();

            Map<String, Object> inputData = new HashMap<>();
            inputData.put("test", "value");

            assertThrows(ValidationException.class, () -> 
                    featureExtractionService.extractFeatureValue(feature, inputData));
        }
    }
}
