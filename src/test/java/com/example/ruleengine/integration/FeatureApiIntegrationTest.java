package com.example.ruleengine.integration;

import com.example.ruleengine.config.TestContainersConfig;
import com.example.ruleengine.dto.request.CreateFeatureRequest;
import com.example.ruleengine.dto.response.FeatureResponse;
import com.example.ruleengine.entity.enums.FeatureType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FeatureApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String createdFeatureId;

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/features - Should create a new feature")
    void testCreateFeature() throws Exception {
        Map<String, Object> extractionConfig = new HashMap<>();
        extractionConfig.put("type", "JSON_PATH");
        extractionConfig.put("path", "$.user.income");

        CreateFeatureRequest request = CreateFeatureRequest.builder()
                .name("monthly_income")
                .description("Monthly income of the user")
                .featureType(FeatureType.NUMERIC)
                .extractionConfig(extractionConfig)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/features")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("monthly_income"))
                .andExpect(jsonPath("$.featureType").value("NUMERIC"))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        FeatureResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), FeatureResponse.class);
        createdFeatureId = response.getId().toString();
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/features/{id} - Should get feature by ID")
    void testGetFeatureById() throws Exception {
        mockMvc.perform(get("/api/v1/features/" + createdFeatureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("monthly_income"))
                .andExpect(jsonPath("$.featureType").value("NUMERIC"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/features - Should get all features")
    void testGetAllFeatures() throws Exception {
        mockMvc.perform(get("/api/v1/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/features?featureType=NUMERIC - Should filter by feature type")
    void testGetFeaturesByType() throws Exception {
        mockMvc.perform(get("/api/v1/features")
                        .param("featureType", "NUMERIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(5)
    @DisplayName("PUT /api/v1/features/{id} - Should update a feature")
    void testUpdateFeature() throws Exception {
        Map<String, Object> extractionConfig = new HashMap<>();
        extractionConfig.put("type", "JSON_PATH");
        extractionConfig.put("path", "$.user.annual_income");

        CreateFeatureRequest request = CreateFeatureRequest.builder()
                .name("monthly_income")
                .description("Updated description")
                .featureType(FeatureType.NUMERIC)
                .extractionConfig(extractionConfig)
                .build();

        mockMvc.perform(put("/api/v1/features/" + createdFeatureId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/v1/features - Should fail with duplicate name")
    void testCreateFeatureDuplicateName() throws Exception {
        Map<String, Object> extractionConfig = new HashMap<>();
        extractionConfig.put("type", "JSON_PATH");
        extractionConfig.put("path", "$.test");

        CreateFeatureRequest request = CreateFeatureRequest.builder()
                .name("monthly_income")
                .description("Duplicate")
                .featureType(FeatureType.NUMERIC)
                .extractionConfig(extractionConfig)
                .build();

        mockMvc.perform(post("/api/v1/features")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/v1/features - Should fail with missing required fields")
    void testCreateFeatureMissingFields() throws Exception {
        CreateFeatureRequest request = CreateFeatureRequest.builder()
                .description("Missing name and type")
                .build();

        mockMvc.perform(post("/api/v1/features")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/v1/features/{id} - Should return 404 for non-existent feature")
    void testGetNonExistentFeature() throws Exception {
        mockMvc.perform(get("/api/v1/features/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(100)
    @DisplayName("DELETE /api/v1/features/{id} - Should delete a feature")
    void testDeleteFeature() throws Exception {
        mockMvc.perform(delete("/api/v1/features/" + createdFeatureId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/features/" + createdFeatureId))
                .andExpect(status().isNotFound());
    }
}
