package com.example.ruleengine.integration;

import com.example.ruleengine.config.TestContainersConfig;
import com.example.ruleengine.dto.request.CreateFeatureRequest;
import com.example.ruleengine.dto.request.CreatePolicyRequest;
import com.example.ruleengine.dto.request.CreatePolicySetRequest;
import com.example.ruleengine.dto.request.CreateRuleRequest;
import com.example.ruleengine.entity.enums.EvaluationStrategy;
import com.example.ruleengine.entity.enums.FeatureType;
import com.example.ruleengine.entity.enums.PolicyType;
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

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PolicySetApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String featureId;
    private static String ruleId;
    private static String booleanPolicyId;
    private static String offerPolicyId;
    private static String policySetId;

    @Test
    @Order(1)
    @DisplayName("Setup - Create feature")
    void createFeature() throws Exception {
        Map<String, Object> extractionConfig = new HashMap<>();
        extractionConfig.put("type", "JSON_PATH");
        extractionConfig.put("path", "$.user.score");

        CreateFeatureRequest request = CreateFeatureRequest.builder()
                .name("user_score")
                .description("User score")
                .featureType(FeatureType.NUMERIC)
                .extractionConfig(extractionConfig)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/features")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        featureId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText();
    }

    @Test
    @Order(2)
    @DisplayName("Setup - Create rule")
    void createRule() throws Exception {
        CreateRuleRequest request = CreateRuleRequest.builder()
                .name("score_rule")
                .description("Score must be >= 50")
                .featureId(UUID.fromString(featureId))
                .operatorCode("GTE")
                .operand(50)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        ruleId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText();
    }

    @Test
    @Order(3)
    @DisplayName("Setup - Create Boolean policy")
    void createBooleanPolicy() throws Exception {
        Map<String, Object> rootNode = new HashMap<>();
        rootNode.put("type", "LEAF");
        rootNode.put("ruleId", ruleId);

        CreatePolicyRequest request = CreatePolicyRequest.builder()
                .name("boolean_test_policy")
                .description("Boolean test policy")
                .policyType(PolicyType.BOOLEAN)
                .rootNode(rootNode)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        booleanPolicyId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText();
    }

    @Test
    @Order(4)
    @DisplayName("Setup - Create Offer policy")
    void createOfferPolicy() throws Exception {
        Map<String, Object> rootNode = new HashMap<>();
        rootNode.put("type", "LEAF");
        rootNode.put("ruleId", ruleId);

        Map<String, Object> outputMapping = new HashMap<>();
        Map<String, Object> defaultOutput = new HashMap<>();
        defaultOutput.put("loanAmount", 10000);
        defaultOutput.put("rateOfInterest", 15.0);
        outputMapping.put("defaultOutput", defaultOutput);

        CreatePolicyRequest request = CreatePolicyRequest.builder()
                .name("offer_test_policy")
                .description("Offer test policy")
                .policyType(PolicyType.OFFER)
                .rootNode(rootNode)
                .outputMapping(outputMapping)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        offerPolicyId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText();
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/v1/policy-sets - Create policy set")
    void createPolicySet() throws Exception {
        List<CreatePolicySetRequest.OfferPolicyWithPriority> offerPolicies = List.of(
                CreatePolicySetRequest.OfferPolicyWithPriority.builder()
                        .policyId(UUID.fromString(offerPolicyId))
                        .priority(1)
                        .enabled(true)
                        .build()
        );

        CreatePolicySetRequest request = CreatePolicySetRequest.builder()
                .name("test_policy_set")
                .description("Test policy set")
                .booleanPolicyId(UUID.fromString(booleanPolicyId))
                .offerPolicies(offerPolicies)
                .evaluationStrategy(EvaluationStrategy.BOOLEAN_FIRST)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/policy-sets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("test_policy_set"))
                .andExpect(jsonPath("$.evaluationStrategy").value("BOOLEAN_FIRST"))
                .andExpect(jsonPath("$.booleanPolicyId").value(booleanPolicyId))
                .andExpect(jsonPath("$.offerPolicies[0].policyId").value(offerPolicyId))
                .andReturn();

        policySetId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText();
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/v1/policy-sets/{id} - Get policy set by ID")
    void getPolicySetById() throws Exception {
        mockMvc.perform(get("/api/v1/policy-sets/" + policySetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test_policy_set"))
                .andExpect(jsonPath("$.booleanPolicyName").value("boolean_test_policy"))
                .andExpect(jsonPath("$.offerPolicies[0].policyName").value("offer_test_policy"));
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/v1/policy-sets - Get all policy sets")
    void getAllPolicySets() throws Exception {
        mockMvc.perform(get("/api/v1/policy-sets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name == 'test_policy_set')]").exists());
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/v1/policy-sets/{id}/evaluate - Evaluate with APPROVED result")
    void evaluatePolicySetApproved() throws Exception {
        Map<String, Object> inputData = new HashMap<>();
        Map<String, Object> user = new HashMap<>();
        user.put("score", 75);
        inputData.put("user", user);

        Map<String, Object> request = new HashMap<>();
        request.put("inputData", inputData);

        mockMvc.perform(post("/api/v1/policy-sets/" + policySetId + "/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision.status").value("APPROVED"))
                .andExpect(jsonPath("$.offer.loanAmount").value(10000.0))
                .andExpect(jsonPath("$.booleanResult").exists())
                .andExpect(jsonPath("$.offerResult").exists());
    }

    @Test
    @Order(9)
    @DisplayName("POST /api/v1/policy-sets/{id}/evaluate - Evaluate with REJECTED result")
    void evaluatePolicySetRejected() throws Exception {
        Map<String, Object> inputData = new HashMap<>();
        Map<String, Object> user = new HashMap<>();
        user.put("score", 30);
        inputData.put("user", user);

        Map<String, Object> request = new HashMap<>();
        request.put("inputData", inputData);

        mockMvc.perform(post("/api/v1/policy-sets/" + policySetId + "/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision.status").value("REJECTED"))
                .andExpect(jsonPath("$.offer").doesNotExist())
                .andExpect(jsonPath("$.offerResult").doesNotExist());
    }

    @Test
    @Order(10)
    @DisplayName("PUT /api/v1/policy-sets/{id} - Update policy set")
    void updatePolicySet() throws Exception {
        CreatePolicySetRequest request = CreatePolicySetRequest.builder()
                .name("test_policy_set")
                .description("Updated description")
                .evaluationStrategy(EvaluationStrategy.PARALLEL)
                .build();

        mockMvc.perform(put("/api/v1/policy-sets/" + policySetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.evaluationStrategy").value("PARALLEL"));
    }

    @Test
    @Order(100)
    @DisplayName("DELETE /api/v1/policy-sets/{id} - Delete policy set")
    void deletePolicySet() throws Exception {
        mockMvc.perform(delete("/api/v1/policy-sets/" + policySetId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/policy-sets/" + policySetId))
                .andExpect(status().isNotFound());
    }
}
