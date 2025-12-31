package com.example.ruleengine.integration;

import com.example.ruleengine.config.TestContainersConfig;
import com.example.ruleengine.dto.request.CreateFeatureRequest;
import com.example.ruleengine.dto.request.CreatePolicyRequest;
import com.example.ruleengine.dto.request.CreateRuleRequest;
import com.example.ruleengine.dto.response.FeatureResponse;
import com.example.ruleengine.dto.response.RuleResponse;
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
class PolicyEvaluationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String ageFeatureId;
    private static String creditScoreFeatureId;
    private static String ageRuleId;
    private static String creditScoreRuleId;
    private static String policyId;

    @Test
    @Order(1)
    @DisplayName("Setup - Create age feature")
    void createAgeFeature() throws Exception {
        Map<String, Object> extractionConfig = new HashMap<>();
        extractionConfig.put("type", "JSON_PATH");
        extractionConfig.put("path", "$.applicant.age");

        CreateFeatureRequest request = CreateFeatureRequest.builder()
                .name("applicant_age")
                .description("Age of the applicant")
                .featureType(FeatureType.NUMERIC)
                .extractionConfig(extractionConfig)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/features")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        FeatureResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), FeatureResponse.class);
        ageFeatureId = response.getId().toString();
    }

    @Test
    @Order(2)
    @DisplayName("Setup - Create credit score feature")
    void createCreditScoreFeature() throws Exception {
        Map<String, Object> extractionConfig = new HashMap<>();
        extractionConfig.put("type", "JSON_PATH");
        extractionConfig.put("path", "$.applicant.creditScore");

        CreateFeatureRequest request = CreateFeatureRequest.builder()
                .name("applicant_credit_score")
                .description("Credit score of the applicant")
                .featureType(FeatureType.NUMERIC)
                .extractionConfig(extractionConfig)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/features")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        FeatureResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), FeatureResponse.class);
        creditScoreFeatureId = response.getId().toString();
    }

    @Test
    @Order(3)
    @DisplayName("Setup - Create age rule (age >= 21)")
    void createAgeRule() throws Exception {
        CreateRuleRequest request = CreateRuleRequest.builder()
                .name("age_minimum_rule")
                .description("Applicant must be at least 21 years old")
                .featureId(UUID.fromString(ageFeatureId))
                .operatorCode("GTE")
                .operand(21)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        RuleResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), RuleResponse.class);
        ageRuleId = response.getId().toString();
    }

    @Test
    @Order(4)
    @DisplayName("Setup - Create credit score rule (score >= 650)")
    void createCreditScoreRule() throws Exception {
        CreateRuleRequest request = CreateRuleRequest.builder()
                .name("credit_score_minimum_rule")
                .description("Credit score must be at least 650")
                .featureId(UUID.fromString(creditScoreFeatureId))
                .operatorCode("GTE")
                .operand(650)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        RuleResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), RuleResponse.class);
        creditScoreRuleId = response.getId().toString();
    }

    @Test
    @Order(5)
    @DisplayName("Setup - Create Boolean policy with AND logic")
    void createBooleanPolicy() throws Exception {
        Map<String, Object> rootNode = new HashMap<>();
        rootNode.put("type", "COMPOSITE");
        rootNode.put("operator", "AND");
        
        List<Map<String, Object>> children = new ArrayList<>();
        
        Map<String, Object> ageLeaf = new HashMap<>();
        ageLeaf.put("type", "LEAF");
        ageLeaf.put("ruleId", ageRuleId);
        children.add(ageLeaf);
        
        Map<String, Object> creditLeaf = new HashMap<>();
        creditLeaf.put("type", "LEAF");
        creditLeaf.put("ruleId", creditScoreRuleId);
        children.add(creditLeaf);
        
        rootNode.put("children", children);

        CreatePolicyRequest request = CreatePolicyRequest.builder()
                .name("loan_eligibility_policy")
                .description("Check if applicant is eligible for a loan")
                .policyType(PolicyType.BOOLEAN)
                .rootNode(rootNode)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("loan_eligibility_policy"))
                .andExpect(jsonPath("$.policyType").value("BOOLEAN"))
                .andReturn();

        policyId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText();
    }

    @Test
    @Order(6)
    @DisplayName("Evaluate policy - Should APPROVE when all conditions pass")
    void testPolicyEvaluationApproved() throws Exception {
        Map<String, Object> inputData = new HashMap<>();
        Map<String, Object> applicant = new HashMap<>();
        applicant.put("age", 25);
        applicant.put("creditScore", 720);
        inputData.put("applicant", applicant);

        Map<String, Object> request = new HashMap<>();
        request.put("inputData", inputData);

        mockMvc.perform(post("/api/v1/policies/" + policyId + "/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision.status").value("APPROVED"))
                .andExpect(jsonPath("$.treeResult.result").value(true))
                .andExpect(jsonPath("$.extractedFeatures.applicant_age").value(25))
                .andExpect(jsonPath("$.extractedFeatures.applicant_credit_score").value(720));
    }

    @Test
    @Order(7)
    @DisplayName("Evaluate policy - Should REJECT when age fails")
    void testPolicyEvaluationRejectedAge() throws Exception {
        Map<String, Object> inputData = new HashMap<>();
        Map<String, Object> applicant = new HashMap<>();
        applicant.put("age", 18);
        applicant.put("creditScore", 720);
        inputData.put("applicant", applicant);

        Map<String, Object> request = new HashMap<>();
        request.put("inputData", inputData);

        mockMvc.perform(post("/api/v1/policies/" + policyId + "/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision.status").value("REJECTED"))
                .andExpect(jsonPath("$.treeResult.result").value(false))
                .andExpect(jsonPath("$.decision.reasons").isArray());
    }

    @Test
    @Order(8)
    @DisplayName("Evaluate policy - Should REJECT when credit score fails")
    void testPolicyEvaluationRejectedCreditScore() throws Exception {
        Map<String, Object> inputData = new HashMap<>();
        Map<String, Object> applicant = new HashMap<>();
        applicant.put("age", 30);
        applicant.put("creditScore", 500);
        inputData.put("applicant", applicant);

        Map<String, Object> request = new HashMap<>();
        request.put("inputData", inputData);

        mockMvc.perform(post("/api/v1/policies/" + policyId + "/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision.status").value("REJECTED"))
                .andExpect(jsonPath("$.treeResult.result").value(false));
    }

    @Test
    @Order(9)
    @DisplayName("Evaluate policy - Should REJECT when both conditions fail")
    void testPolicyEvaluationRejectedBoth() throws Exception {
        Map<String, Object> inputData = new HashMap<>();
        Map<String, Object> applicant = new HashMap<>();
        applicant.put("age", 18);
        applicant.put("creditScore", 500);
        inputData.put("applicant", applicant);

        Map<String, Object> request = new HashMap<>();
        request.put("inputData", inputData);

        mockMvc.perform(post("/api/v1/policies/" + policyId + "/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision.status").value("REJECTED"))
                .andExpect(jsonPath("$.treeResult.result").value(false));
    }

    @Test
    @Order(10)
    @DisplayName("Test rule individually")
    void testRuleIndividually() throws Exception {
        Map<String, Object> inputData = new HashMap<>();
        Map<String, Object> applicant = new HashMap<>();
        applicant.put("age", 25);
        inputData.put("applicant", applicant);

        Map<String, Object> request = new HashMap<>();
        request.put("inputData", inputData);

        mockMvc.perform(post("/api/v1/rules/" + ageRuleId + "/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true))
                .andExpect(jsonPath("$.extractedValue").value(25))
                .andExpect(jsonPath("$.operatorCode").value("GTE"));
    }
}
