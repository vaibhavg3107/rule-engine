package com.example.ruleengine.integration;

import com.example.ruleengine.config.TestContainersConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
class OperatorApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/operators - Should return all seeded operators")
    void testGetAllOperators() throws Exception {
        mockMvc.perform(get("/api/v1/operators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(18));
    }

    @Test
    @DisplayName("GET /api/v1/operators/{code} - Should return operator by code")
    void testGetOperatorByCode() throws Exception {
        mockMvc.perform(get("/api/v1/operators/EQ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("EQ"))
                .andExpect(jsonPath("$.name").value("Equals"))
                .andExpect(jsonPath("$.operandType").value("SINGLE"));
    }

    @Test
    @DisplayName("GET /api/v1/operators/{code} - Should return 404 for non-existent operator")
    void testGetNonExistentOperator() throws Exception {
        mockMvc.perform(get("/api/v1/operators/INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/operators?featureType=NUMERIC - Should filter by feature type")
    void testGetOperatorsByNumericFeatureType() throws Exception {
        mockMvc.perform(get("/api/v1/operators")
                        .param("featureType", "NUMERIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.code == 'EQ')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'LT')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'GT')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'BETWEEN')]").exists());
    }

    @Test
    @DisplayName("GET /api/v1/operators?featureType=STRING - Should filter by STRING type")
    void testGetOperatorsByStringFeatureType() throws Exception {
        mockMvc.perform(get("/api/v1/operators")
                        .param("featureType", "STRING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.code == 'CONTAINS')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'STARTS_WITH')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'REGEX')]").exists());
    }

    @Test
    @DisplayName("GET /api/v1/operators?featureType=LIST - Should filter by LIST type")
    void testGetOperatorsByListFeatureType() throws Exception {
        mockMvc.perform(get("/api/v1/operators")
                        .param("featureType", "LIST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.code == 'CONTAINS_ALL')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'CONTAINS_ANY')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'SIZE_EQ')]").exists());
    }

    @Test
    @DisplayName("Verify comparison operators have correct operand types")
    void testComparisonOperatorsOperandType() throws Exception {
        mockMvc.perform(get("/api/v1/operators/LT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operandType").value("SINGLE"));

        mockMvc.perform(get("/api/v1/operators/BETWEEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operandType").value("RANGE"));

        mockMvc.perform(get("/api/v1/operators/IN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operandType").value("LIST"));

        mockMvc.perform(get("/api/v1/operators/IS_EMPTY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operandType").value("NONE"));
    }
}
